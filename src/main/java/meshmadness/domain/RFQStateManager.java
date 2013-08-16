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
    private String fillerName;
    private SBP whoLocked = null;

    public RFQStateManager(final SBP sbp, final RFQ rfq) {
        this.sbp = sbp;
        this.rfq = rfq;

        logger.debug(String.format("%d (%s) RFQStateManager Constructor for %s RFQ%s %s %s", System.nanoTime(), sbp.getName(),
                rfq.getUsername(), rfq.getRFQId(), currentState, currentStateTime));
    }

    public String getFillerName() {
        return fillerName;
    }

    public synchronized void NextState(final Payload payload) {
        if (currentState == RFQState.StartRFQ) {
            // Send to all sales people

            currentState = RFQState.SendToDI;

            sbp.sendToAllSales(rfq, currentState);
        } else if (payload instanceof LocalPayload) {
            processLocalPayload((LocalPayload)payload);
        } else if (payload instanceof MeshPayload) {
            processMeshPayload((MeshPayload) payload);
        }
    }

    private void processMeshPayload(MeshPayload payload) {
        final MeshPayload meshPayload = (MeshPayload)payload;
        if (meshPayload.getState() == RFQState.Locked) {
            changeToState(RFQState.Locked);
            whoLocked = meshPayload.getSource();

            logger.debug(String.format("%d (%s) RFQ%s %s %s", System.nanoTime(), sbp.getName(),
                    rfq.getRFQId(), currentState, currentStateTime));
        } else if (meshPayload.getTime() > currentStateTime && currentState != RFQState.Locked) {
            // Cancel this back to the source
            logger.debug(String.format("%d (%s) Telling %s to Lock RFQ%s %s %s ", System.nanoTime(), sbp.getName(),
                    meshPayload.getSource().getName(), meshPayload.getRFQId(), meshPayload.getTime(), currentStateTime));

            sbp.notifyRegion(meshPayload.getSource(), meshPayload.getRFQ(), RFQState.Locked, currentStateTime);
        } else {
            logger.debug(String.format("%d (%s) RFQ%s LOCKED by %s %s %s %s", System.nanoTime(), sbp.getName(),
                    meshPayload.getRFQId(), meshPayload.getSource().getName(), meshPayload.getState(), meshPayload.getTime(), meshPayload.getSource().getName()));

            currentState = RFQState.Locked;
            currentStateTime = meshPayload.getTime();
            whoLocked = meshPayload.getSource();
        }
    }

    private void processLocalPayload(LocalPayload salesPersonModification) {
        if (currentState == RFQState.Locked) {
            if (rfq.getSource() == whoLocked && salesPersonModification.getState() == RFQState.Putback) {
                logger.debug(String.format("%d (%s) %s RFQ%s Unlock %s %s", System.nanoTime(), sbp.getName(),
                        rfq.getUsername(), rfq.getRFQId(), salesPersonModification.getState(), rfq.getSource().getName()));
                changeToState(RFQState.SendToDI);
                whoLocked = null;
                sbp.sendToAllSales(rfq, currentState);
            } else if (rfq.getSource() == whoLocked && salesPersonModification.getState() == RFQState.Quote) {
                changeToState(RFQState.Complete);
                fillerName = salesPersonModification.getFillerName();
                whoLocked = null;
                sbp.sendToAllSales(rfq, currentState);
            } else {
                logger.debug(String.format("%d (%s) %s RFQ%s IGNORED(Locked) %s %s", System.nanoTime(), sbp.getName(),
                        rfq.getUsername(), rfq.getRFQId(), salesPersonModification.getState(), rfq.getSource().getName()));
            }
        } else {
            switch (salesPersonModification.getState()) {
                case Pickup:
                    changeToState(RFQState.Pickup);
                    logger.debug(String.format("%d (%s) RFQ%s %s %s", System.nanoTime(), sbp.getName(),
                            rfq.getRFQId(), currentState, currentStateTime));

                    sbp.notifyMesh(rfq, currentState, currentStateTime);
                    break;
                case Putback:
                    changeToState(RFQState.SendToDI);
                    fillerName = null;
                    logger.debug(String.format("%d (%s) RFQ%s %s %s", System.nanoTime(), sbp.getName(),
                            rfq.getRFQId(), currentState, currentStateTime));

                    sbp.sendToAllSales(rfq, currentState);

                    if (whoLocked!= null) {
                        sbp.notifyRegion(whoLocked, rfq, RFQState.Putback, currentStateTime);
                        whoLocked = null;
                    }
                    break;
                case Quote:
                    changeToState(RFQState.Quote);
                    fillerName = salesPersonModification.getFillerName();
                    logger.debug(String.format("%d (%s) RFQ%s %s %s", System.nanoTime(), sbp.getName(),
                            rfq.getRFQId(), currentState, currentStateTime));

                    sbp.notifyMesh(rfq, currentState, currentStateTime);
                    rfq.getUser().price(salesPersonModification.getPrice());
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
