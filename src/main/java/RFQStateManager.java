
public class RFQStateManager {
    public enum RFQState {InitialRequest, SendToSales, Pickup, Locked, SendPrice, Putback, Complete};

    private final SBP sbp;
    private final RFQ rfq;
    private RFQState currentState=RFQState.InitialRequest;
    private long currentStateTime =System.nanoTime();
    private SBP whoLocked = null;

    public RFQStateManager(final SBP sbp, final RFQ rfq) {
        this.sbp = sbp;
        this.rfq = rfq;

        System.out.println(String.format("%d (%s) RFQStateManager Constructor for %s RFQ%s %s %s", System.nanoTime(), sbp.getName(),
                rfq.getUsername(), rfq.getRFQId(), currentState, currentStateTime));
    }

    public synchronized void NextState(final Payload payload) {
        if (currentState == RFQState.InitialRequest) {
            // Send to all sales people

            currentState = RFQState.SendToSales;

            sbp.sendToAllSales(rfq, currentState);
            return;
        } else if (payload instanceof LocalPayload) {
            final LocalPayload salesPersonModification = (LocalPayload) payload;

            if (currentState == RFQState.Locked) {
                if (rfq.getSource() == whoLocked && salesPersonModification.getState() == RFQState.Putback) {
                    System.out.println(String.format("%d (%s) %s RFQ%s Unlock %s %s", System.nanoTime(), sbp.getName(),
                            rfq.getUsername(), rfq.getRFQId(), salesPersonModification.getState(), rfq.getSource().getName()));
                    currentState = RFQState.SendToSales;
                    currentStateTime = System.nanoTime();
                    whoLocked = null;
                    sbp.sendToAllSales(rfq, currentState);
                } else if (rfq.getSource() == whoLocked && salesPersonModification.getState() == RFQState.SendPrice) {
                    currentState = RFQState.Complete;
                    currentStateTime = System.nanoTime();
                    whoLocked = null;
                    sbp.sendToAllSales(rfq, currentState);
                } else {
                    System.out.println(String.format("%d (%s) %s RFQ%s IGNORED(Locked) %s %s", System.nanoTime(), sbp.getName(),
                            rfq.getUsername(), rfq.getRFQId(), salesPersonModification.getState(), rfq.getSource().getName()));
                }
            } else {
                switch (salesPersonModification.getState()) {
                    case Pickup:
                        currentState = RFQState.Pickup;
                        currentStateTime =System.nanoTime();
                        System.out.println(String.format("%d (%s) RFQ%s %s %s", System.nanoTime(), sbp.getName(),
                                rfq.getRFQId(), currentState, currentStateTime));

                        sbp.notifyMesh(rfq, currentState, currentStateTime);
                        break;
                    case Putback:
                        currentState = RFQState.SendToSales;
                        currentStateTime =System.nanoTime();
                        System.out.println(String.format("%d (%s) RFQ%s %s %s", System.nanoTime(), sbp.getName(),
                                rfq.getRFQId(), currentState, currentStateTime));

                        sbp.sendToAllSales(rfq, currentState);

                        if (whoLocked!= null) {
                            sbp.notfyRegion(whoLocked, rfq, RFQState.Putback, currentStateTime);
                            whoLocked = null;
                        }
                        break;
                    case SendPrice:
                        currentState = RFQState.SendPrice;
                        currentStateTime =System.nanoTime();
                        System.out.println(String.format("%d (%s) RFQ%s %s %s", System.nanoTime(), sbp.getName(),
                                rfq.getRFQId(), currentState, currentStateTime));

                        sbp.notifyMesh(rfq, currentState, currentStateTime);
                        rfq.getUser().price(salesPersonModification.getPrice());
                        break;
                    default:
                        throw new RuntimeException("Unknown State");
                }
            }

            return;
        } else if (payload instanceof MeshPayload) {
            final MeshPayload meshPayload = (MeshPayload)payload;
            if (meshPayload.getState() == RFQState.Locked) {
                currentState = RFQState.Locked;
                currentStateTime = System.nanoTime();
                whoLocked = meshPayload.getSource();

                System.out.println(String.format("%d (%s) RFQ%s %s %s", System.nanoTime(), sbp.getName(),
                        rfq.getRFQId(), currentState, currentStateTime));
                return;
            } else if (meshPayload.getTime() > currentStateTime && currentState != RFQState.Locked) {
                // Cancel this back to the source
                System.out.println(String.format("%d (%s) Telling %s to Lock RFQ%s %s %s ", System.nanoTime(),sbp.getName(),
                        meshPayload.getSource().getName(), meshPayload.getRFQId(), meshPayload.getTime(), currentStateTime));

                sbp.notfyRegion(meshPayload.getSource(), meshPayload.getRFQ(), RFQState.Locked, currentStateTime);
                return;
            } else {
                System.out.println(String.format("%d (%s) RFQ%s LOCKED by %s %s %s %s", System.nanoTime(), sbp.getName(),
                        meshPayload.getRFQId(), meshPayload.getSource().getName(), meshPayload.getState(), meshPayload.getTime(), meshPayload.getSource().getName()));

                currentState = RFQState.Locked;
                currentStateTime = meshPayload.getTime();
                whoLocked = meshPayload.getSource();
                return;
            }
        }

        throw new RuntimeException("Unknown State");
    }

    public RFQState getCurrentState() {
        return currentState;
    }

    public long getCurrentStateTime() {
        return currentStateTime;
    }

}
