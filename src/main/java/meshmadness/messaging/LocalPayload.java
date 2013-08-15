package meshmadness.messaging;

import meshmadness.RFQStateManager;
import meshmadness.SBP;

public class LocalPayload implements Payload {
    private final int rfqId;
    private final RFQStateManager.RFQState state;
    private final SBP source;
    private double price=0.0;

    public LocalPayload(final int rfqId, final RFQStateManager.RFQState state, final SBP source, final double price) {
        this(rfqId, state, source);
        this.price = price;
    }

    public LocalPayload(final int rfqId, final RFQStateManager.RFQState state, final SBP source) {
        this.rfqId = rfqId;
        this.state = state;
        this.source = source;
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
