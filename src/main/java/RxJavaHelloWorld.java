import rx.Observable;
import rx.Subscription;
import rx.subjects.ReplaySubject;
import rx.subjects.Subject;
import rx.util.functions.Action1;
import rx.util.functions.Func1;

public class RxJavaHelloWorld {
    private ReplaySubject<String> subString = ReplaySubject.create();

    public static void main(String[] args) {
        new RxJavaHelloWorld().run();
    }

    private void run() {
        hello("hello");

        subString.onNext("yes");

        Subscription subscription = subString.subscribe(new Action1<String>() {
            @Override
            public void call(String str) {
                System.out.println("Received " + str);
            }
        });

        Observable<String> observable = subString;
        Observable<String> observable1 = observable.filter(new Func1<String, Boolean>() {
            @Override
            public Boolean call(String str) {
                if (str == "yes 2")
                    return true;

                return false;
            }
        });

        Subscription subscription1 = observable1.subscribe(new Action1<String>() {

            @Override
            public void call(String s) {
                System.out.println("Found " + s);
        }});

        subString.onNext("yes 1");
        subString.onNext("yes 2");
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
