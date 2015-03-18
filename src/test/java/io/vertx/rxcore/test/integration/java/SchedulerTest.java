package io.vertx.rxcore.test.integration.java;

import io.vertx.rxcore.java.RxVertx;
import org.junit.Test;
import org.vertx.java.core.Context;
import org.vertx.testtools.TestVerticle;
import rx.Observable;
import rx.Observer;
import rx.functions.Action1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.vertx.testtools.VertxAssert.*;

/** Unit-test for Scheduler */
public class SchedulerTest extends TestVerticle {

  /** Test Timer */
  @Test
  public void testTimer() {

    RxVertx rx=new RxVertx(vertx);

    final long startTime=System.currentTimeMillis();
    final Context initCtx=vertx.currentContext();

    Observable.zip(
      Observable.from(Arrays.asList(1,2,3,4,5,6,7,8,9,10)),
      Observable.timer(100, 100, TimeUnit.MILLISECONDS, rx.contextScheduler()),
      new Func2<Integer, Long, Integer>() {
        public Integer call(Integer value, Long timer) {
          return value;
        }
      })
      .subscribe(new Observer<Integer>() {
        public void onCompleted() {
          long timeTaken = System.currentTimeMillis() - startTime;
          assertTrue(Math.abs(timeTaken - 1000) < 100);
          testComplete();
        }

        public void onError(Throwable e) {
          fail("unexpected failure");
        }

        public void onNext(Integer value) {
          long timeTaken = System.currentTimeMillis() - startTime;
          System.out.println("after " + timeTaken + "ms -> " + value);
          assertEquals(initCtx, vertx.currentContext());
        }
      });
  }

  /** Test Buffer */
  @Test
  public void testBuffer() {

    RxVertx rx=new RxVertx(vertx);

    final long startTime=System.currentTimeMillis();
    final Context initCtx=vertx.currentContext();

    Observable
      .timer(10, 10, TimeUnit.MILLISECONDS, rx.contextScheduler())
      .buffer(100,TimeUnit.MILLISECONDS,rx.contextScheduler())
      .take(10)
      .subscribe(new Observer<List<Long>>() {

        private int eventCount=0;

        public void onCompleted() {
          long timeTaken = System.currentTimeMillis() - startTime;
          assertEquals(10,eventCount);
          assertTrue(Math.abs(timeTaken - 1000) < 100);
          testComplete();
        }

        public void onError(Throwable e) {
          fail("unexpected failure");
        }

        public void onNext(List<Long> value) {
          eventCount++;
          long timeTaken = System.currentTimeMillis() - startTime;
          System.out.println("after " + timeTaken + "ms -> " + Arrays.toString(value.toArray()));
          assertEquals(initCtx, vertx.currentContext());
        }
      });
  }

  /** Test Concurrent Debounce */
  @Test
  public void testConcurrentDebounce() {

    RxVertx rx=new RxVertx(vertx);

    final long startTime=System.currentTimeMillis();
    final Context initCtx=vertx.currentContext();

    final Observable<Long> whenTimer1=Observable
      .timer(1, 10, TimeUnit.MILLISECONDS, rx.contextScheduler())
      .take(10); // 0..9 over ~ 1..101 ms

    final Observable<Long> whenTimer2=Observable
      .timer(501, 10, TimeUnit.MILLISECONDS, rx.contextScheduler())
      .take(10); // 0..9 over ~ 501..601 ms

    final Observable<Long> whenTimer3=Observable
      .timer(1001, 10, TimeUnit.MILLISECONDS, rx.contextScheduler())
      .take(10); // 0..9 over ~ 1001..1101 ms

    final Observable<Long> whenTimer4=Observable
      .timer(1501, 10, TimeUnit.MILLISECONDS, rx.contextScheduler())
      .take(10); // 0..9 over ~ 1501..1601 ms

    Observable.merge(whenTimer1, whenTimer2, whenTimer3, whenTimer4)
      .debounce(200, TimeUnit.MILLISECONDS, rx.contextScheduler()) // -> should be 4 debounced events
      .subscribe(new Observer <Long>() {
        private int eventCount=0;

        public void onCompleted() {
          assertEquals(4, eventCount);
        }

        public void onError(Throwable e) {
          fail("unexpected failure");
        }

        public void onNext(Long value) {
          eventCount++;
          long timeTaken = System.currentTimeMillis() - startTime;
          System.out.println("(a) after " + timeTaken + "ms -> " + value);
          assertEquals(9L, value.longValue());
          assertEquals(initCtx, vertx.currentContext());
        }
      });

    // independent short-timeout debounce at the same time on the same scheduler
    Observable
      .timer(1, 5, TimeUnit.MILLISECONDS, rx.contextScheduler())
      .take(400) // 0..399 over ~ 1..2001 ms
      .debounce(20, TimeUnit.MILLISECONDS, rx.contextScheduler())
      .subscribe(new Observer<Long>() {
        private long lastValue=-1;

        public void onCompleted() {
          assertEquals(399,lastValue);
          testComplete();
        }

        public void onError(Throwable e) {
          fail("unexpected failure");
        }

        public void onNext(Long value) {
          lastValue=value;
          long timeTaken = System.currentTimeMillis() - startTime;
          System.out.println("(b) after " + timeTaken + "ms -> " + value);
          assertEquals(initCtx, vertx.currentContext());
        }
      });
  }

  /** Test Multi Scheduler */
  @Test
  public void testMultiScheduler() {

    RxVertx rx=new RxVertx(vertx);

    final long startTime=System.currentTimeMillis();
    final Context initCtx=vertx.currentContext();

    Observable
      .timer(10, 1, TimeUnit.MILLISECONDS, rx.contextScheduler())
      .take(10)
      .delay(3, TimeUnit.MILLISECONDS, Schedulers.newThread())
      .observeOn(rx.contextScheduler())
      .subscribe(new Observer <Long>() {

        private int eventCount=0;

        public void onCompleted() {
          assertEquals(10, eventCount);
          testComplete();
        }

        public void onError(Throwable e) {
          fail("unexpected failure");
        }

        public void onNext(Long value) {
          eventCount++;
          long timeTaken = System.currentTimeMillis() - startTime;
          System.out.println("after " + timeTaken + "ms -> " + value);
          assertEquals(initCtx, vertx.currentContext());
        }
      });
  }

  /** Test Zero Period Timer */
  @Test
  public void testZeroPeriodTimer() {

    RxVertx rx=new RxVertx(vertx);

    final long startTime=System.currentTimeMillis();
    final Context initCtx=vertx.currentContext();

    Observable
      .timer(0, 0, TimeUnit.MILLISECONDS, rx.contextScheduler())
      .take(10)
      .subscribe(new Observer <Long>() {

        private int eventCount=0;

        public void onCompleted() {
          assertEquals(10, eventCount);
          testComplete();
        }

        public void onError(Throwable e) {
          fail("unexpected failure");
        }

        public void onNext(Long value) {
          eventCount++;
          long timeTaken = System.currentTimeMillis() - startTime;
          System.out.println("after " + timeTaken + "ms -> " + value);
          assertEquals(initCtx, vertx.currentContext());
        }
      });
  }
}
