package meshmadness.framework;

import rx.Observer;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import rx.subjects.Subject;
import rx.util.functions.Func1;
import rx.util.functions.Function;

import java.util.*;

public class SingleObserverHistorySubject<T> extends Subject<T, T> {

    private boolean isDone = false;
    private Throwable exception = null;
    private final Map<Subscription, Observer<? super T>> subscriptions = new HashMap<>();
    private final List<T> history = Collections.synchronizedList(new ArrayList<T>());

    public static <T> SingleObserverHistorySubject<T> create() {
        return new SingleObserverHistorySubject<>(new DelegateSubscriptionFunc<T>());
    }

    private SingleObserverHistorySubject(DelegateSubscriptionFunc<T> onSubscribe) {
        super(onSubscribe);
        onSubscribe.wrap(new SubscriptionFunc());
    }

    public static interface OnSubscribeFunc<T> extends Function {

        public Subscription onSubscribe(Observer<? super T> t1);

    }

    private static final class DelegateSubscriptionFunc<T> implements OnSubscribeFunc<T>, Func1<Observer<T>, Subscription> {
        private Func1<? super Observer<? super T>, ? extends Subscription> delegate = null;

        public void wrap(Func1<? super Observer<? super T>, ? extends Subscription> delegate)
        {
            if (this.delegate != null) {
                throw new UnsupportedOperationException("delegate already set");
            }
            this.delegate = delegate;
        }

        @Override
        public Subscription onSubscribe(Observer<? super T> observer)
        {
            return delegate.call(observer);
        }

        @Override
        public Subscription call(Observer<T> tObserver) {
            return delegate.call(tObserver);
        }
    }

    private class SubscriptionFunc implements Func1<Observer<? super T>, Subscription>
    {
        @Override
        public Subscription call(Observer<? super T> observer) {
            int item = 0;
            Subscription subscription;

            for (;;) {
                while (item < history.size()) {
                    observer.onNext(history.get(item++));
                }
                history.clear();

                synchronized (subscriptions) {
                    if (item < history.size()) {
                        continue;
                    }

                    if (exception != null) {
                        observer.onError(exception);
                        return Subscriptions.empty();
                    }
                    if (isDone) {
                        observer.onCompleted();
                        return Subscriptions.empty();
                    }

                    subscription = new RepeatSubjectSubscription();

                    if (subscriptions.size() == 0) {
                        subscriptions.put(subscription, observer);
                    } else {
                        throw new RuntimeException("Too many subscribers");
                    }
                    break;
                }
            }

            return subscription;
        }
    }

    private class RepeatSubjectSubscription implements Subscription
    {
        @Override
        public void unsubscribe()
        {
            synchronized (subscriptions) {
                subscriptions.remove(this);
            }
        }
    }

    @Override
    public void onCompleted()
    {
        synchronized (subscriptions) {
            isDone = true;
            for (Observer<? super T> observer : new ArrayList<Observer<? super T>>(subscriptions.values())) {
                observer.onCompleted();
            }
            subscriptions.clear();
        }
    }

    @Override
    public void onError(Throwable e)
    {
        synchronized (subscriptions) {
            if (isDone) {
                return;
            }
            isDone = true;
            exception = e;
            for (Observer<? super T> observer : new ArrayList<Observer<? super T>>(subscriptions.values())) {
                observer.onError(e);
            }
            subscriptions.clear();
        }
    }

    @Override
    public void onNext(T args)
    {
        synchronized (subscriptions) {
            history.add(args);
            for (Observer<? super T> observer : new ArrayList<Observer<? super T>>(subscriptions.values())) {
                observer.onNext(args);
                history.remove(args);
            }
        }
    }
}
