package yeay.tv.yeay.utils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import de.rhm.rxsemaphore.RxSemaphoreTransformer;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


@RunWith(JUnit4.class)
public class RxSemaphoreTest {

    private PublishSubject<Boolean> lock;
    private TestObserver<Boolean> observer;
    private Observable<Boolean> observable;

    @Before
    public void setup() {
        lock = PublishSubject.create();
        observer = TestObserver.create();
        observable = lock.compose(new RxSemaphoreTransformer());
        observable.subscribe(observer);
    }

    @Test
    public void singleEvent_result_locked() throws Exception {
        lock.onNext(true);

        observer.assertNotComplete();
        observer.assertNoErrors();
        observer.assertValueCount(1);
        observer.assertValue(false);
    }

    @Test
    public void lockAndUnlock_result_unlocked() throws Exception {
        lock.onNext(true);
        lock.onNext(false);

        observer.assertNotComplete();
        observer.assertNoErrors();
        observer.assertValueCount(2);
        assertThat(observer.values().get(0), is(false));
        assertThat(observer.values().get(1), is(true));
    }

    @Test
    public void multipleLock_singleUnlock_result_locked() throws Exception {
        lock.onNext(true);
        lock.onNext(true);
        lock.onNext(true);
        lock.onNext(false);

        observer.assertNotComplete();
        observer.assertNoErrors();
        observer.assertValueCount(4);
        assertThat(observer.values().get(0), is(false));
        assertThat(observer.values().get(1), is(false));
        assertThat(observer.values().get(2), is(false));
        assertThat(observer.values().get(3), is(false));
    }

    @Test
    public void multipleLock_multipleUnlock_result_unlocked() throws Exception {
        lock.onNext(true);
        lock.onNext(true);
        lock.onNext(true);
        lock.onNext(false);
        lock.onNext(false);
        lock.onNext(false);

        observer.assertNotComplete();
        observer.assertNoErrors();
        observer.assertValueCount(6);
        assertThat(observer.values().get(0), is(false));
        assertThat(observer.values().get(1), is(false));
        assertThat(observer.values().get(2), is(false));
        assertThat(observer.values().get(3), is(false));
        assertThat(observer.values().get(4), is(false));
        assertThat(observer.values().get(5), is(true));
    }

    @Test
    public void singleLock_multipleUnlock_result_unlock() throws Exception {
        lock.onNext(true);
        lock.onNext(false);
        lock.onNext(false);
        lock.onNext(false);

        observer.assertNotComplete();
        observer.assertNoErrors();
        observer.assertValueCount(4);
        assertThat(observer.values().get(0), is(false));
        assertThat(observer.values().get(1), is(true));
        assertThat(observer.values().get(2), is(true));
        assertThat(observer.values().get(3), is(true));
    }

    @Test
    public void singleLock_unsubscribe_result_locked() throws Exception {
        lock.onNext(true);
        lock.onComplete();
        lock.onNext(false);

        observer.assertComplete();
        observer.assertNoErrors();
        observer.assertValueCount(1);
        assertThat(observer.values().get(0), is(false));

    }

    @Test
    public void singleLock_error_result_locked() throws Exception {
        Throwable t = new Throwable();

        lock.onNext(true);
        lock.onError(t);
        lock.onNext(false);

        observer.assertTerminated();
        observer.assertError(t);
        observer.assertValueCount(1);
        assertThat(observer.values().get(0), is(false));
    }

    @Test
    public void multipleUnlock_singleLock_result_unlocked() {
        lock.onNext(false);
        lock.onNext(false);
        lock.onNext(false);
        lock.onNext(true);

        observer.assertNotComplete();
        observer.assertNoErrors();
        observer.assertValueCount(4);
        assertThat(observer.values().get(0), is(true));
        assertThat(observer.values().get(1), is(true));
        assertThat(observer.values().get(2), is(true));
        assertThat(observer.values().get(3), is(true));
    }

    @Test
    public void disposed_observer_result_noEvents(){
        observer.onComplete();
        observer.dispose();

        lock.onNext(true);

        observer.assertNoErrors();
        observer.assertComplete();
        observer.assertNoValues();
    }

    @Test
    public void unlock_multipleSubscribersDisposed_result_noValuesEmitted(){
        TestObserver<Boolean> observer2 = TestObserver.create();
        observable.subscribe(observer2);

        observer.dispose();
        observer2.dispose();
        lock.onNext(false);

        observer.assertNoValues();
        observer.assertNoErrors();
        observer2.assertNoValues();
        observer2.assertNoErrors();
    }

    @Test
    public void unlock_singleSubscribersDisposed_result_valuesEmittedForOne(){
        TestObserver<Boolean> observer2 = TestObserver.create();
        observable.subscribe(observer2);
      
        lock.onNext(true);
        observer2.dispose();
        lock.onNext(false);

        observer.assertNotComplete();
        observer.assertNoErrors();
        observer.assertValueCount(2);
        assertThat(observer.values().get(0), is(false));
        assertThat(observer.values().get(1), is(true));

        observer2.assertNoErrors();
        observer2.assertValueCount(1);
        assertThat(observer2.values().get(0), is(false));
    }
}
