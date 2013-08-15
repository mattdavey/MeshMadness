package meshmadness.messaging;

import meshmadness.domain.RFQStateManager;
import meshmadness.domain.SBP;

public class LocalPayload implements Payload {
    private final int rfqId;
    private final RFQStateManager.RFQState state;
    private final SBP source;
    private final String fillerName;
    private double price=0.0;

    public LocalPayload(final int rfqId, final RFQStateManager.RFQState state, final SBP source, final double price, final String fillerName) {
        this(rfqId, state, source, fillerName);
        this.price = price;
    }

    public LocalPayload(final int rfqId, final RFQStateManager.RFQState state, final SBP source, final String fillerName) {
        this.rfqId = rfqId;
        this.state = state;
        this.source = source;
        this.fillerName = fillerName;
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

    public String getFillerName() {
        return fillerName;
    }
}
