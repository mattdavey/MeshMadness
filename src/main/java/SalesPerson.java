import rx.subjects.ReplaySubject;
import rx.subjects.Subject;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class SalesPerson implements Runnable {
    final private Random randomGenerator = new Random(System.nanoTime());
    final private BlockingQueue<RFQ> rfqQueue = new LinkedBlockingQueue<RFQ>();
    private ReplaySubject<RFQStateManager.RFQState> subjectRFQNegotiation = ReplaySubject.create();

    private final String name;
    private final SBP sbp;
    private final RFQStateManager.RFQState[] dialog;

    public SalesPerson(final String name, final SBP sbp, final RFQStateManager.RFQState[] dialog) {
        this.name = name;
        this.sbp = sbp;
        this.dialog = dialog;
    }

    // Auto pickup any RFQ that is not complete
    public void SalesPersonCommunication(final RFQ rfq, final RFQStateManager.RFQState state) {
        if (state != RFQStateManager.RFQState.Complete) {
            System.out.println(String.format("%d %s Pickup FRQ%s", System.nanoTime(), name, rfq.getRFQId()));
            rfqQueue.add(rfq);
            sbp.send(new LocalPayload(rfq.getRFQId(), RFQStateManager.RFQState.Pickup, sbp));
        } else {
            System.out.println(String.format("%d %s Complete FRQ%s", System.nanoTime(), name, rfq.getRFQId()));
        }
    }

    public void run() {
        int dialogCount = 0;
        while(true){
            try {
                final RFQ rfq = rfqQueue.take();
                if (dialogCount < dialog.length) {
                    RFQStateManager.RFQState current = dialog[dialogCount++];
                    switch (current) {
                        case Putback:
                            System.out.println(String.format("%d %s Putback RFQ%s", System.nanoTime(), name, rfq.getRFQId()));
                            sbp.send(new LocalPayload(rfq.getRFQId(), RFQStateManager.RFQState.Putback, sbp));
                            break;
                        case Quote:
                            final double price = randomGenerator.nextInt(120);
                            System.out.println(String.format("%d %s Quote %s RFQ%s", System.nanoTime(), name, price, rfq.getRFQId()));
                            sbp.send(new LocalPayload(rfq.getRFQId(), RFQStateManager.RFQState.Quote, sbp, price));
                            break;
                    }

                    subjectRFQNegotiation.onNext(current);
                } else {
                    System.out.println("Finished");
                }
            } catch (InterruptedException ex) {
                System.out.println(ex);
            }
        }
    }

}
