package meshmadness.actors;

import meshmadness.domain.RFQ;
import meshmadness.domain.RFQStateManager;
import meshmadness.domain.SBP;
import meshmadness.framework.SingleObserverHistorySubject;
import meshmadness.messaging.LocalPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.concurrency.Schedulers;
import rx.observables.ConnectableObservable;
import rx.subjects.PublishSubject;
import rx.util.functions.Action1;

import java.util.HashMap;
import java.util.Random;

public class SalesPerson {
    final Logger logger = LoggerFactory.getLogger(SalesPerson.class);

    private final Random randomGenerator = new Random(System.nanoTime());
    private final PublishSubject<RFQ> dealerQueue = PublishSubject.create();
    private final HashMap<Integer, SingleObserverHistorySubject<DealerAction>> mapDialog = new HashMap<>();

    private final String name;
    private final ConnectableObservable<RFQ> observableDealerQueue;

    private final Object newDialogLock = new Object();

    class DealerAction {
        final private RFQStateManager.RFQState state;
        final private int rfqId;

        public DealerAction(final int rfqId, final RFQStateManager.RFQState state) {
            this.state = state;
            this.rfqId = rfqId;
        }
    }

    private SingleObserverHistorySubject<DealerAction> queue;
    private ConnectableObservable<DealerAction> observableQueue;

    public SalesPerson(final String name, final SBP sbp) {
        this.name = name;

        observableDealerQueue = dealerQueue.publish();

        observableDealerQueue.observeOn(Schedulers.threadPoolForComputation()).subscribe(new Action1<RFQ>() {
            @Override
            public void call(final RFQ rfq) {
                queue = getDealerActionPublishSubject(rfq.getRFQId());
                observableQueue = queue.publish();
                observableQueue.observeOn(Schedulers.threadPoolForComputation()).subscribe(new Action1<DealerAction>() {
                    @Override
                    public void call(final DealerAction dealerAction) {
                        sbp.send(new LocalPayload(rfq.getRFQId(), RFQStateManager.RFQState.Pickup, sbp, name));
                        logger.debug(String.format("%d (%s) Pickup RFQ%s from DI queue", System.nanoTime(), name, rfq.getRFQId()));

                        fakeUserThinkingTime();

                        switch (dealerAction.state) {
                            case Putback:
                                sbp.send(new LocalPayload(dealerAction.rfqId, RFQStateManager.RFQState.Putback, sbp, name));
                                logger.debug(String.format("%d (%s) Putback RFQ%s", System.nanoTime(), name, dealerAction.rfqId));
                                break;
                            case Quote:
                                final double price = randomGenerator.nextInt(120);
                                sbp.send(new LocalPayload(dealerAction.rfqId, RFQStateManager.RFQState.Quote, sbp, price, name));
                                logger.debug(String.format("%d (%s) Quote %s RFQ%s", System.nanoTime(), name, price, dealerAction.rfqId));
                                break;
                        }
                    }
                }
                );

                observableQueue.connect();
            }
        });

        observableDealerQueue.connect();
    }

    private void fakeUserThinkingTime() {
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            logger.error(e.toString());  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void salesPersonCommunication(final RFQ rfq, final RFQStateManager.RFQState state) {
        if (state != RFQStateManager.RFQState.Complete) {
            logger.debug(String.format("%d (%s) received RFQ%s and puts into DI queue (%s)", System.nanoTime(), name, rfq.getRFQId(), state.toString()));
            dealerQueue.onNext(rfq);
        } else {
            logger.debug(String.format("%d (%s) Complete RFQ%s", System.nanoTime(), name, rfq.getRFQId()));
        }
    }

    public void dialog(final RFQStateManager.RFQState message, final int rfqId) {
        synchronized (newDialogLock) {
            final SingleObserverHistorySubject<DealerAction> queue = getDealerActionPublishSubject(rfqId);
            queue.onNext(new DealerAction(rfqId, message));
        }
        logger.debug(String.format("%d (%s) Adding dialog (%s, %d)", System.nanoTime(), name, message.toString(), rfqId));
    }

    private SingleObserverHistorySubject<DealerAction> getDealerActionPublishSubject(final int rfqId) {
        SingleObserverHistorySubject<DealerAction> queue = mapDialog.get(rfqId);
        if (queue == null) {
            queue = SingleObserverHistorySubject.create();
            mapDialog.put(rfqId, queue);
            logger.debug(String.format("%d (%s) Create Queue for RFQ%d)", System.nanoTime(), name, rfqId));
        }
        return queue;
    }
}
