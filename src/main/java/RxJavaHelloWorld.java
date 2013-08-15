import rx.Observable;
import rx.Subscription;
import rx.concurrency.Schedulers;
import rx.concurrency.TestScheduler;
import rx.observables.ConnectableObservable;
import rx.subjects.ReplaySubject;
import rx.subjects.Subject;
import rx.util.functions.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RxJavaHelloWorld {
    private ReplaySubject<String> subString = ReplaySubject.create();

    public static void main(String[] args) throws InterruptedException {
        new RxJavaHelloWorld().run();
    }

    class ZipHolder {
        final public RFQ rfq;
        final public RFQStateManager.RFQState state;

        public ZipHolder(final RFQ rfq, final RFQStateManager.RFQState state) {
            this.rfq = rfq;
            this.state = state;
        }
    }

    private void run() throws InterruptedException {
        hello("hello");

        TestScheduler();

        ObservableTest();

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
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
