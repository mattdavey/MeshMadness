package meshmadness.domain;

import meshmadness.messaging.LocalPayload;
import meshmadness.messaging.MeshPayload;
import meshmadness.messaging.Payload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RFQStateManager {
    public enum RFQState {StartRFQ, SendToDI, Pickup, Locked, Quote, Putback, Complete};

    final Logger logger = LoggerFactory.getLogger(RFQStateManager.class);

    private final SBP sbp;
    private final RFQ rfq;
    private RFQState currentState=RFQState.StartRFQ;
    private long currentStateTime =System.nanoTime();
    private String quoterName;
    private SBP sbpThatLockedRFQ = null;

    public RFQStateManager(final SBP sbp, final RFQ rfq) {
        this.sbp = sbp;
        this.rfq = rfq;

        logger.debug(String.format("%d (%s) RFQStateManager Constructor for %s RFQ%s (%s,%s)", System.nanoTime(), sbp.getName(),
                rfq.getUsername(), rfq.getRFQId(), currentState, currentStateTime));
    }

    public String getQuoterName() {
        return quoterName;
    }

    synchronized public void NextState(final Payload payload) {
        if (currentState == RFQState.StartRFQ) {
            // Send to all sales people

            currentState = RFQState.SendToDI;
            sbp.notifyLocalSales(rfq, currentState);
        } else if (payload instanceof LocalPayload) {
            processLocalSalesDIMessage((LocalPayload) payload);
        } else if (payload instanceof MeshPayload) {
            processMeshPayload((MeshPayload) payload);
        }
    }

    private void processMeshPayload(final MeshPayload meshPayload) {
        // if somebody tells us to lock, we need to confirm the time
        if (meshPayload.getState() == RFQState.Locked) {
            // If the mesh payload is earlier than us, then another region has the RFQ
            if (meshPayload.getTime() < currentStateTime) {
                changeToState(RFQState.Locked);
                sbpThatLockedRFQ = meshPayload.getSource();

                logger.debug(String.format("%d (%s) RFQ%s %s %s", System.nanoTime(), sbp.getName(), rfq.getRFQId(), currentState, currentStateTime));
            } else {
                // Ignore since we are earlier in time

                logger.debug(String.format("%d (%s) Telling %s to Lock RFQ%s %s %s ", System.nanoTime(), sbp.getName(), meshPayload.getSource().getName(), meshPayload.getRFQId(), meshPayload.getTime(), currentStateTime));
                sbp.notifyOneRegion(meshPayload.getSource(), meshPayload.getRFQ(), RFQState.Locked, currentStateTime);
            }
        }
    }

//    private void OLD_processMeshPayload(final MeshPayload meshPayload) {
//        if (meshPayload.getState() == RFQState.Locked) {
//            changeToState(RFQState.Locked);
//            sbpThatLockedRFQ = meshPayload.getSource();
//
//            logger.debug(String.format("%d (%s) RFQ%s %s %s", System.nanoTime(), sbp.getName(),
//                    rfq.getRFQId(), currentState, currentStateTime));
//        } else if (meshPayload.getTime() > currentStateTime && currentState != RFQState.Locked) {
//            // Cancel this back to the source
//            logger.debug(String.format("%d (%s) Telling %s to Lock RFQ%s %s %s ", System.nanoTime(), sbp.getName(),
//                    meshPayload.getSource().getName(), meshPayload.getRFQId(), meshPayload.getTime(), currentStateTime));
//
//            sbp.notifyOneRegion(meshPayload.getSource(), meshPayload.getRFQ(), RFQState.Locked, currentStateTime);
//        }
//        else {
//            logger.debug(String.format("%d (%s) RFQ%s LOCKED by %s %s %s %s", System.nanoTime(), sbp.getName(),
//                    meshPayload.getRFQId(), meshPayload.getSource().getName(), meshPayload.getState(), meshPayload.getTime(), meshPayload.getSource().getName()));
//
//            currentState = RFQState.Locked;
//            currentStateTime = meshPayload.getTime();
//            sbpThatLockedRFQ = meshPayload.getSource();
//        }
//    }

    private void processLocalSalesDIMessage(final LocalPayload salesDIPayload) {
        if (currentState == RFQState.Locked) {
            if (rfq.getOriginatingSBP() == sbpThatLockedRFQ && salesDIPayload.getState() == RFQState.Putback) {
                logger.debug(String.format("%d (%s) %s RFQ%s Unlock %s %s", System.nanoTime(), sbp.getName(),
                        rfq.getUsername(), rfq.getRFQId(), salesDIPayload.getState(), rfq.getOriginatingSBP().getName()));

                changeToState(RFQState.SendToDI);
                sbpThatLockedRFQ = null;
                sbp.notifyLocalSales(rfq, currentState);
            } else if (rfq.getOriginatingSBP() == sbpThatLockedRFQ && salesDIPayload.getState() == RFQState.Quote) {
                changeToState(RFQState.Complete);
                quoterName = salesDIPayload.getQuoterName();
                sbpThatLockedRFQ = null;
                sbp.notifyLocalSales(rfq, currentState);
            }
//            else {
//                logger.debug(String.format("%d (%s) %s RFQ%s IGNORED(Locked) %s %s", System.nanoTime(), sbp.getName(),
//                        rfq.getUsername(), rfq.getRFQId(), salesDIPayload.getState(), rfq.getOriginatingSBP().getName()));
//            }
        } else {
            switch (salesDIPayload.getState()) {
                case Pickup:
                    changeToState(RFQState.Pickup);
                    logger.debug(String.format("%d (%s) Pickup RFQ%s %s %s", System.nanoTime(), sbp.getName(),
                            rfq.getRFQId(), currentState, currentStateTime));

                    sbp.notifyAllMesh(rfq, currentState, currentStateTime);
                    break;
                case Putback:
                    changeToState(RFQState.SendToDI);
                    quoterName = null;
                    logger.debug(String.format("%d (%s) Putback RFQ%s %s %s", System.nanoTime(), sbp.getName(),
                            rfq.getRFQId(), currentState, currentStateTime));

                    sbp.notifyLocalSales(rfq, currentState);

                    if (sbpThatLockedRFQ != null) {
                        sbp.notifyOneRegion(sbpThatLockedRFQ, rfq, RFQState.Putback, currentStateTime);
                        sbpThatLockedRFQ = null;
                    }
                    break;
                case Quote:
                    changeToState(RFQState.Quote);
                    quoterName = salesDIPayload.getQuoterName();
                    logger.debug(String.format("%d (%s) Quote RFQ%s %s %s", System.nanoTime(), sbp.getName(),
                            rfq.getRFQId(), currentState, currentStateTime));

                    sbp.notifyAllMesh(rfq, currentState, currentStateTime);

                    rfq.getOriginatingUser().notifyPrice(salesDIPayload.getPrice());
                    break;
                default:
                    throw new RuntimeException("Unknown State");
            }
        }
    }

    private void changeToState(final RFQState newState) {
        currentState = newState;
        currentStateTime = System.nanoTime();
    }

    public RFQState getCurrentState() {
        return currentState;
    }

    public long getCurrentStateTime() {
        return currentStateTime;
    }
}
