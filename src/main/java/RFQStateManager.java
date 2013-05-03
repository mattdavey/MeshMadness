
public class RFQStateManager {
    public RFQState getCurrentState() {
        return currentState;
    }

    public long getCurrentStateTime() {
        return currentStateTime;
    }

    public enum RFQState {InitialRequest, SendToSales, Pickup, Locked, SendPrice, Putback};

    private final SBP sbp;
    private RFQ rfq;
    private RFQState currentState=RFQState.InitialRequest;
    private long currentStateTime =System.nanoTime();

    public RFQStateManager(final SBP sbp, final RFQ rfq) {
        this.sbp = sbp;
        this.rfq = rfq;

        System.out.println(String.format("%d (%s) %s RFQ%s %s %s", System.nanoTime(), sbp.getName(),
                rfq.getUsername(), rfq.getRFQId(), currentState, currentStateTime));
    }

    public void NextState(final Payload payload) {
        if (currentState == RFQState.InitialRequest) {
            // Send to all sales people

            currentState = RFQState.SendToSales;

            sbp.sendToAllSales(rfq);
            return;
        } else if (payload instanceof LocalPayload) {
            if (currentState == RFQState.Locked) {
                System.out.println(String.format("%d (%s) %s RFQ%s IGNORED DUE TO LOCKED", System.nanoTime(), sbp.getName(),
                        rfq.getUsername(), rfq.getRFQId()));
                return;
            }
            final LocalPayload salesPersonModification = (LocalPayload) payload;
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

                    sbp.sendToAllSales(rfq);
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

            return;
        } else if (payload instanceof MeshPayload) {
            final MeshPayload meshPayload = (MeshPayload)payload;
            if (meshPayload.getState() == RFQState.Locked) {
                currentState = RFQState.Locked;
                currentStateTime = System.nanoTime();

                System.out.println(String.format("%d (%s) %s %s %s", System.nanoTime(), sbp.getName(),
                        rfq.getRFQId(), currentState, currentStateTime));
                return;
            } else if (meshPayload.getTime() > currentStateTime && currentState != RFQState.Locked) {
                // Cancel this back to the source
                System.out.println(String.format("%d (%s) Telling %s to Lock %s %s ", System.nanoTime(),sbp.getName(),
                        meshPayload.getSource().getName(),meshPayload.getTime(), currentStateTime));

                sbp.notfyRegion(meshPayload.getSource(), meshPayload.getRFQ(), RFQState.Locked, currentStateTime);
                return;
            } else {
                System.out.println(String.format("%d (%s) RFQ%s LOCKED due to %s %s %s", System.nanoTime(), sbp.getName(),
                        meshPayload.getRFQId(), meshPayload.getSource().getName(), meshPayload.getState(), meshPayload.getTime()));

                currentState = RFQState.Locked;
                currentStateTime = meshPayload.getTime();
                return;
            }
        }

        throw new RuntimeException("Unknown State");
    }
}
