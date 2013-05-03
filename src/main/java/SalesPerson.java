import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SalesPerson implements Runnable {
    final private Random randomGenerator = new Random(System.nanoTime());
    final private BlockingQueue<RFQ> rfqQueue = new LinkedBlockingQueue<RFQ>();

    private final String name;
    private final SBP sbp;

    public SalesPerson(final String name, final SBP sbp) {
        this.name = name;
        this.sbp = sbp;
    }

    public void SalesPersonCommunication(final RFQ rfq) {
        System.out.println(String.format("%d %s Pickup FRQ%s", System.nanoTime(), name, rfq.getRFQId()));
        rfqQueue.add(rfq);
        sbp.send(new LocalPayload(rfq.getRFQId(), RFQStateManager.RFQState.Pickup));
    }

    public void run() {
        while(true){
            try {
                final RFQ rfq = rfqQueue.take();
                Thread.sleep(3000);

                switch (randomGenerator.nextInt(2)) {
                    case 0:
                        System.out.println(String.format("%d %s Putback RFQ%s", System.nanoTime(), name, rfq.getRFQId()));
                        sbp.send(new LocalPayload(rfq.getRFQId(), RFQStateManager.RFQState.Putback));
                        break;
                    case 1:
                        final double price = randomGenerator.nextInt(120);
                        System.out.println(String.format("%d %s SendPrice %s RFQ%s", System.nanoTime(), name, price, rfq.getRFQId()));
                        sbp.send(new LocalPayload(rfq.getRFQId(), RFQStateManager.RFQState.SendPrice, price));
                        break;
                }
            } catch (InterruptedException ex) {
                System.out.println(ex);
            }
        }
    }

}
