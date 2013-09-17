package meshmadness.play;

import meshmadness.actors.User;
import meshmadness.domain.RFQ;
import meshmadness.domain.RFQStateManager;
import meshmadness.domain.SBP;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.concurrency.Schedulers;
import rx.concurrency.TestScheduler;
import rx.observables.ConnectableObservable;
import rx.subjects.PublishSubject;
import rx.subjects.ReplaySubject;
import rx.util.functions.Action0;
import rx.util.functions.Action1;
import rx.util.functions.Func1;
import rx.util.functions.Func2;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RxJavaHelloWorld {
    private ReplaySubject<String> subString = ReplaySubject.create();
    private final Random randomGenerator = new Random(System.nanoTime());

    public static void main(String[] args) throws InterruptedException {
        new RxJavaHelloWorld().run();
    }

    class DealerAction {
        private RFQStateManager.RFQState state;
        private int rfqId;
        private int price;

        public DealerAction(final int rfqId, final RFQStateManager.RFQState state) {
            this.state = state;
            this.rfqId = rfqId;
        }

        public DealerAction(final int rfqId, final int price) {
            this.rfqId = rfqId;
            this.price = price;
        }
    }

    class ZipHolder {
        final private RFQ rfq;
        final private RFQStateManager.RFQState state;

        public ZipHolder(final RFQ rfq, final RFQStateManager.RFQState state) {
            this.rfq = rfq;
            this.state = state;
        }
    }

    private void run() throws InterruptedException {
//        hello("hello");
//        TestScheduler();
//        ObservableTest();
//        ObservableTest2();
//        ObservableTest3();
        ObservableTest4();
    }

    private void waitForKeyPress() {
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void ObservableTest4() {
        final PublishSubject<RFQ> dealerQueue = PublishSubject.create();
        final PublishSubject<DealerAction> dialog = PublishSubject.create();

        dealerQueue.subscribe(new Action1<RFQ>() {
            @Override
            public void call(final RFQ rfq) {
                dialog.subscribe(new Action1<DealerAction>() {
                    @Override
                    public void call(final DealerAction dealerAction) {
                        System.out.println(String.format("Received RFQ%d %s", rfq.getRFQId(), dealerAction.toString()));
                    }
                });
            }
        });


        final SBP sbp1 = new SBP("SBP1");
        final User user1 = new User("User1", sbp1);

        dialog.onNext(new DealerAction(1, RFQStateManager.RFQState.Pickup));
        dealerQueue.onNext(new RFQ(user1, 2, sbp1));
        dealerQueue.onNext(new RFQ(user1, 1, sbp1));
        dialog.onNext(new DealerAction(2, RFQStateManager.RFQState.Pickup));
        dialog.onNext(new DealerAction(2, RFQStateManager.RFQState.Quote));
        dealerQueue.onNext(new RFQ(user1, 2, sbp1));

        waitForKeyPress();
    }

    private void ObservableTest3() {
        final PublishSubject<RFQ> dealerQueue = PublishSubject.create();
        final PublishSubject<DealerAction> dialog = PublishSubject.create();

        final ConnectableObservable<RFQ> observableDealerQueue = dealerQueue.publish();
        final ConnectableObservable<DealerAction> observableDialog = dialog.publish();

        final Subscription sub = observableDealerQueue.mapMany(new Func1<RFQ, Observable<DealerAction>>() {
            @Override
            public Observable<DealerAction> call(final RFQ rfq) {
                return null;
//                return observableDialog.map(new Func1<DealerAction, DealerAction>() {
//                    @Override
//                    public DealerAction call(final DealerAction dealerAction) {
//                        if (dealerAction.rfqId == rfq.getRFQId()) {
//                            return new DealerAction(dealerAction.rfqId, randomGenerator.nextInt(120));
//                        }
//
//                        return new DealerAction(dealerAction.rfqId, dealerAction.state);
//                    }
//                });
            }
        }).subscribe(new Action1<DealerAction>() {
            @Override
            public void call(final DealerAction dealerAction) {
                System.out.println(String.format("Found %d %s", dealerAction.rfqId, dealerAction.state));
            }
        });

        observableDealerQueue.connect();
        observableDialog.connect();

        final SBP sbp1 = new SBP("SBP1");
        final User user1 = new User("User1", sbp1);

        dialog.onNext(new DealerAction(1, RFQStateManager.RFQState.Pickup));
        dealerQueue.onNext(new RFQ(user1, 2, sbp1));
        dealerQueue.onNext(new RFQ(user1, 1, sbp1));
        dialog.onNext(new DealerAction(2, RFQStateManager.RFQState.Pickup));
        dialog.onNext(new DealerAction(2, RFQStateManager.RFQState.Quote));
        dealerQueue.onNext(new RFQ(user1, 2, sbp1));

        waitForKeyPress();
        sub.unsubscribe();
    }

    private void ObservableTest2() {
        final ReplaySubject<RFQ> dealerQueue = ReplaySubject.create();
        final ReplaySubject<DealerAction> dialog = ReplaySubject.create();

        dealerQueue.subscribe(new Action1<RFQ>() {
            @Override
            public void call(final RFQ rfq) {


                // Look for a dialog
                dialog.where(new Func1<DealerAction, Boolean>() {
                    @Override
                    public Boolean call(final DealerAction state) {
                        System.out.println(String.format("Where called with RFQ%d", state.rfqId));
                        if (state.rfqId == rfq.getRFQId()) {
                            return true;
                        }

                        return false;
                    }
                }).subscribe(new Action1<DealerAction>() {
                    @Override
                    public void call(final DealerAction stateIdHolder) {
                        System.out.println(String.format("Found %d", stateIdHolder.rfqId));
                    }
                });

            }
        });

        final SBP sbp1 = new SBP("SBP1");
        final User user1 = new User("User1", sbp1);
        dialog.onNext(new DealerAction(1, RFQStateManager.RFQState.Pickup));
        dealerQueue.onNext(new RFQ(user1, 2, sbp1));
        dealerQueue.onNext(new RFQ(user1, 1, sbp1));
        dialog.onNext(new DealerAction(2, RFQStateManager.RFQState.Pickup));
    }

    private void ObservableTest() {
        final ReplaySubject<RFQ> dealerQueue = ReplaySubject.create();
        final ReplaySubject<RFQStateManager.RFQState> dialog = ReplaySubject.create();

        final ConnectableObservable<RFQ> observableDealerQueue = dealerQueue.subscribeOn(Schedulers.newThread()).publish();
        final ConnectableObservable<RFQStateManager.RFQState> observableDialog = dialog.publish();
        final Subscription sub = Observable.zip(observableDealerQueue, observableDialog, new Func2<RFQ, RFQStateManager.RFQState, ZipHolder>() {
            @Override
            public ZipHolder call(final RFQ rfq, RFQStateManager.RFQState rfqState) {
                return new ZipHolder(rfq, rfqState);
            }
        }).subscribe(new Action1<ZipHolder>() {
            @Override
            public void call(ZipHolder zipHolder) {
                switch (zipHolder.state) {
                    case Putback:
                        break;
                    case Quote:
                        break;
                }
            }
        });

        observableDealerQueue.connect();
        observableDialog.connect();

        final SBP sbp1 = new SBP("SBP1");
        final User user1 = new User("User1", sbp1);
        dealerQueue.onNext(new RFQ(user1, 1, sbp1));
        dialog.onNext(RFQStateManager.RFQState.Pickup);
    }

    private void TestScheduler() throws InterruptedException {
        final Subscription subscription = subString.subscribe(new Action1<String>() {
            @Override
            public void call(String str) {
                System.out.println("Received " + str);
            }
        });

        subString.onNext("Message");

        final Observable<String> observable = subString.filter(new Func1<String, Boolean>() {
            @Override
            public Boolean call(String str) {
                if (str == "yes")
                    return true;

                return false;
            }
        });

        final CountDownLatch latch = new CountDownLatch(1);
        final Subscription subscription1 = observable.subscribe(new Action1<String>() {

            @Override
            public void call(String s) {
                System.out.println("Found " + s);
        }});

        final TestScheduler scheduler = new TestScheduler();
        scheduler.schedule(new Action0() {
            @Override
            public void call() {
                subString.onNext("yes");
                subString.onNext("no");
                latch.countDown();
            }
        }, 5, TimeUnit.SECONDS);
        scheduler.advanceTimeBy(5, TimeUnit.SECONDS);

        latch.await();
    }

    public static void hello(String... names) {
        Observable.toObservable(names).subscribe(new Action1<String>() {

            @Override
            public void call(String s) {
                System.out.println("Hello " + s + "!");
            }

        });
    }
}
