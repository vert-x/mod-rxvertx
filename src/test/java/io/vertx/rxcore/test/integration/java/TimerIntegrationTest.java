package io.vertx.rxcore.test.integration.java;

import io.vertx.rxcore.java.timer.RxTimer;
import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.testtools.TestVerticle;
import rx.Observable;
import rx.Subscription;
import static io.vertx.rxcore.test.integration.java.RxAssert.assertCount;
import static io.vertx.rxcore.test.integration.java.RxAssert.assertCountThenComplete;
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
    
    Observable<Long> ob=timer.setPeriodic(50);
    
    // Expect 3 values
    final Subscription sub=assertCountThenComplete(ob,3);
    
    vertx.setTimer(50*3+25,new Handler<Long>() {
      public void handle(Long event) {
        System.out.println("Trigger unsubscribe after 3 events");
        sub.unsubscribe();
      }
    });
  }
}