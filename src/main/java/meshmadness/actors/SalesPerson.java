package meshmadness.actors;

import meshmadness.RFQ;
import meshmadness.RFQStateManager;
import meshmadness.SBP;
import meshmadness.messaging.LocalPayload;
import rx.Observable;
import rx.Subscription;
import rx.concurrency.Schedulers;
import rx.observables.ConnectableObservable;
import rx.subjects.ReplaySubject;
import rx.util.functions.Action1;
import rx.util.functions.Func2;

import java.util.Random;

public class SalesPerson {
    final private Random randomGenerator = new Random(System.nanoTime());
    private final ReplaySubject<RFQ> dealerQueue = ReplaySubject.create();
    private final ReplaySubject<RFQStateManager.RFQState> dialog = ReplaySubject.create();

    private final String name;
    private final SBP sbp;
    final ConnectableObservable<RFQ> observableDealerQueue;
    final ConnectableObservable<RFQStateManager.RFQState> observableDialog;
    final Subscription sub;

    private class ZipHolder {
        final private RFQ rfq;
        final private RFQStateManager.RFQState state;

        public ZipHolder(final RFQ rfq, final RFQStateManager.RFQState state) {
            this.rfq = rfq;
            this.state = state;
        }
    }

    public SalesPerson(final String name, final SBP sbp) {
        this.name = name;
        this.sbp = sbp;

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
            dealerQueue.onNext(rfq);
            sbp.send(new LocalPayload(rfq.getRFQId(), RFQStateManager.RFQState.Pickup, sbp));
        } else {
            System.out.println(String.format("%d %s Complete RFQ%s", System.nanoTime(), name, rfq.getRFQId()));
        }
    }

    public void dialog(final RFQStateManager.RFQState message) {
        dialog.onNext(message);
    }
}
