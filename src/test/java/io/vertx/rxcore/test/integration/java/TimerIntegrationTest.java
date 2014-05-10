package io.vertx.rxcore.test.integration.java;

import io.vertx.rxcore.java.timer.RxTimer;
import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.testtools.TestVerticle;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action0;

import static io.vertx.rxcore.test.integration.java.RxAssert.assertCount;
import static io.vertx.rxcore.test.integration.java.RxAssert.assertCountThen;
import static io.vertx.rxcore.test.integration.java.RxAssert.assertCountThenComplete;
import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.testComplete;

/**
 * TimerIntegrationTest
 */
public class TimerIntegrationTest extends TestVerticle {

  @Test
  public void testSetTimer() {
    RxTimer timer=new RxTimer(vertx);
    
    Observable<Long> ob=timer.setTimer(50);
    
    // Wait for 1 element to complete Observable
    assertCountThenComplete(ob,1);
  }
  
  @Test
  public void testSetTimerButCancel() {
    RxTimer timer=new RxTimer(vertx);
    
    Observable<Long> ob=timer.setTimer(50);

    // Subscription that expects 0 elements
    Subscription sub=assertCountThenComplete(ob,0);
    
    // Cancel via unsubscribe should complete empty
    sub.unsubscribe();
  }
  
  @Test
  public void testSetTimerCancelAfter() {
    RxTimer timer=new RxTimer(vertx);
    
    Observable<Long> ob=timer.setTimer(50);

    // Ensure we receive one element only
    final Subscription sub=assertCount(ob,1);
    
    vertx.setTimer(50+25,new Handler<Long>() {
      public void handle(Long event) {
        System.out.println("Trigger unsubscribe after timer");
        sub.unsubscribe();
        // All good
        testComplete();
      }
    });
  }

  @Test
  public void testSetPeriodic() {
    RxTimer timer=new RxTimer(vertx);
    
    Observable<Long> ob=timer.setPeriodic(50).take(3);

    final long startTime=System.currentTimeMillis();

    // Expect 3 values
    final Subscription sub=assertCountThen(ob,new Action0() {
      public void call() {
        long totalTime=System.currentTimeMillis()-startTime;
        System.out.println("Test complete after "+(totalTime)+"ms");
        // Ensure the total time is withing 20% of the expected value
        assertTrue("Time within 20% of expected",Math.abs((totalTime/(50*3))-1)<0.2);
        testComplete();
      }
    }, 3);
  }
}