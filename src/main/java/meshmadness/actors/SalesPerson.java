package meshmadness.actors;

import meshmadness.domain.RFQ;
import meshmadness.domain.RFQStateManager;
import meshmadness.domain.SBP;
import meshmadness.messaging.LocalPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscription;
import rx.concurrency.Schedulers;
import rx.observables.ConnectableObservable;
import rx.subjects.ReplaySubject;
import rx.util.functions.Action1;
import rx.util.functions.Func2;

import java.util.Random;

public class SalesPerson {
    final Logger logger = LoggerFactory.getLogger(SalesPerson.class);

    private static int ONE_SECOND = 1000;
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

        observableDealerQueue = dealerQueue.publish();
        observableDialog = dialog.publish();
        sub = Observable.zip(observableDealerQueue, observableDialog, new Func2<RFQ, RFQStateManager.RFQState, Object>() {
            @Override
            public ZipHolder call(final RFQ rfq, final RFQStateManager.RFQState rfqState) {
            sbp.send(new LocalPayload(rfq.getRFQId(), RFQStateManager.RFQState.Pickup, sbp, name));
            logger.debug(String.format("%d (%s) Processing RFQ%s", System.nanoTime(), name, rfq.getRFQId()));
            return new ZipHolder(rfq, rfqState);
            }
        }).observeOn(Schedulers.newThread()).subscribe(new Action1<ZipHolder>() {
            @Override
            public void call(final ZipHolder zipHolder) {
            switch (zipHolder.state) {
                case Putback:
                    sbp.send(new LocalPayload(zipHolder.rfq.getRFQId(), RFQStateManager.RFQState.Putback, sbp, name));
                    logger.debug(String.format("%d (%s) Putback RFQ%s", System.nanoTime(), name, zipHolder.rfq.getRFQId()));
                    break;
                case Quote:
                    final double price = randomGenerator.nextInt(120);
                    sbp.send(new LocalPayload(zipHolder.rfq.getRFQId(), RFQStateManager.RFQState.Quote, sbp, price, name));
                    logger.debug(String.format("%d (%s) Quote %s RFQ%s", System.nanoTime(), name, price, zipHolder.rfq.getRFQId()));
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
            logger.debug(String.format("%d (%s) received RFQ%s and puts into DI queue (%s)", System.nanoTime(), name, rfq.getRFQId(), state.toString()));
            dealerQueue.onNext(rfq);
        } else {
            logger.debug(String.format("%d (%s) Complete RFQ%s", System.nanoTime(), name, rfq.getRFQId()));
        }
    }

    public void dialog(final RFQStateManager.RFQState message) {
        logger.debug(String.format("%d (%s) Dialog (%s)", System.nanoTime(), name, message.toString()));
        dialog.onNext(message);
    }
}
