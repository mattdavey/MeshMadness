package meshmadness.messaging;

import meshmadness.domain.RFQ;
import meshmadness.domain.RFQStateManager;
import meshmadness.domain.SBP;

public class MeshPayload implements Payload {
    final private RFQ rfq;
    final private RFQStateManager.RFQState state;
    final private SBP source;
    private final long time;

    public MeshPayload(final RFQ rfq, final RFQStateManager.RFQState state, final SBP source, long time) {
        this.rfq = rfq;
        this.state = state;
        this.source = source;
        this.time = time;
    }

    public RFQStateManager.RFQState getState() {
        return state;
    }

    public SBP getSource() {
        return source;
    }

    public RFQ getRFQ() {
        return rfq;
    }

    public int getRFQId() {
        return rfq.getRFQId();
    }

    public long getTime() {
        return time;
    }
}
