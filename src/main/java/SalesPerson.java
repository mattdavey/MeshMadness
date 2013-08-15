import rx.Observable;
import rx.Subscription;
import rx.concurrency.Schedulers;
import rx.observables.ConnectableObservable;
import rx.subjects.ReplaySubject;
import rx.util.functions.Action1;
import rx.util.functions.Func2;

import java.util.Random;


public class SalesPerson /*implements Runnable*/ {
    final private Random randomGenerator = new Random(System.nanoTime());
    private final ReplaySubject<RFQ> dealerQueue = ReplaySubject.create();
    private final ReplaySubject<RFQStateManager.RFQState> dialog = ReplaySubject.create();

//    final private BlockingQueue<RFQ> rfqQueue = new LinkedBlockingQueue<>();
//    private ReplaySubject<RFQStateManager.RFQState> subjectRFQNegotiation = ReplaySubject.create();

    private final String name;
    private final SBP sbp;
    final ConnectableObservable<RFQ> observableDealerQueue;
    final ConnectableObservable<RFQStateManager.RFQState> observableDialog;
    final Subscription sub;
//    private final Subscription subDialog;
//    private final Subscription subDealQueue;

    class ZipHolder {
        final public RFQ rfq;
        final public RFQStateManager.RFQState state;

        public ZipHolder(final RFQ rfq, final RFQStateManager.RFQState state) {
            this.rfq = rfq;
            this.state = state;
        }
    }

    public SalesPerson(final String name, final SBP sbp) {
        this.name = name;
        this.sbp = sbp;

//        subDialog = dialog.subscribe(new Action1<RFQStateManager.RFQState>() {
//            @Override
//            public void call(RFQStateManager.RFQState state) {
//                System.out.println("Received " + state);
//            }
//        });
//
//        subDealQueue = dealerQueue.subscribe(new Action1<RFQ>() {
//            @Override
//            public void call(RFQ rfq) {
//                System.out.println("Received RFQ");
//            }
//        });

        observableDealerQueue = dealerQueue.subscribeOn(Schedulers.newThread()).publish();
        observableDialog = dialog.publish();
        sub = Observable.zip(observableDealerQueue, observableDialog, new Func2<RFQ, RFQStateManager.RFQState, ZipHolder>() {
            @Override
            public ZipHolder call(final RFQ rfq, RFQStateManager.RFQState rfqState) {
                return new ZipHolder(rfq, rfqState);
            }
        }).subscribe(new Action1<ZipHolder>() {
            @Override
            public void call(ZipHolder zipHolder) {
                switch (zipHolder.state) {
                    case Putback:
                        System.out.println(String.format("%d %s Putback RFQ%s", System.nanoTime(), name, zipHolder.rfq.getRFQId()));
                        sbp.send(new LocalPayload(zipHolder.rfq.getRFQId(), RFQStateManager.RFQState.Putback, sbp));
                        break;
                    case Quote:
                        final double price = randomGenerator.nextInt(120);
                        System.out.println(String.format("%d %s Quote %s RFQ%s", System.nanoTime(), name, price, zipHolder.rfq.getRFQId()));
                        sbp.send(new LocalPayload(zipHolder.rfq.getRFQId(), RFQStateManager.RFQState.Quote, sbp, price));
                        break;
                }
            }
        });

        observableDealerQueue.connect();
        observableDialog.connect();
    }

    // Auto pickup any RFQ that is not complete
    public void SalesPersonCommunication(final RFQ rfq, final RFQStateManager.RFQState state) {
        if (state != RFQStateManager.RFQState.Complete) {
            System.out.println(String.format("%d %s Pickup RFQ%s", System.nanoTime(), name, rfq.getRFQId()));
//            rfqQueue.add(rfq);
            dealerQueue.onNext(rfq);
            sbp.send(new LocalPayload(rfq.getRFQId(), RFQStateManager.RFQState.Pickup, sbp));
        } else {
            System.out.println(String.format("%d %s Complete RFQ%s", System.nanoTime(), name, rfq.getRFQId()));
        }
    }

//    public void run() {
//        int dialogCount = 0;
//        while (true) {
//            try {
//                final RFQ rfq = rfqQueue.take();
//                if (dialogCount < dialog.length) {
//                    RFQStateManager.RFQState current = dialog[dialogCount++];
//                    switch (current) {
//                        case Putback:
//                            System.out.println(String.format("%d %s Putback RFQ%s", System.nanoTime(), name, rfq.getRFQId()));
//                            sbp.send(new LocalPayload(rfq.getRFQId(), RFQStateManager.RFQState.Putback, sbp));
//                            break;
//                        case Quote:
//                            final double price = randomGenerator.nextInt(120);
//                            System.out.println(String.format("%d %s Quote %s RFQ%s", System.nanoTime(), name, price, rfq.getRFQId()));
//                            sbp.send(new LocalPayload(rfq.getRFQId(), RFQStateManager.RFQState.Quote, sbp, price));
//                            break;
//                    }
//
//                    subjectRFQNegotiation.onNext(current);
//                } else {
//                    System.out.println("Finished");
//                }
//            } catch (InterruptedException ex) {
//                System.out.println(ex);
//            }
//        }
//    }

    public void dialog(final RFQStateManager.RFQState message) {
        dialog.onNext(message);
    }
}
