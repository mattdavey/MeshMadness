
public class LocalPayload implements Payload {
    private final int rfqId;
    private final RFQStateManager.RFQState state;
    private double price=0.0;

    public LocalPayload(final int rfqId, final RFQStateManager.RFQState state, final double price) {
        this(rfqId, state);
        this.price = price;
    }

    public LocalPayload(final int rfqId, final RFQStateManager.RFQState state) {
        this.rfqId = rfqId;
        this.state = state;
    }

    public int getRFQId() {
        return rfqId;
    }

    public RFQStateManager.RFQState getState() {
        return state;
    }

    public double getPrice() {
        return price;
    }
}
