package io.vertx.rxcore.test.integration.java;

import io.vertx.rxcore.java.RxVertx;
import org.junit.Test;
import org.vertx.java.core.Context;
import org.vertx.testtools.TestVerticle;
import rx.Observable;
import rx.Observer;
import rx.functions.Action1;
import rx.functions.Func2;

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
}
