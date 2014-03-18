package io.vertx.rxcore.java.timer;

import java.util.concurrent.atomic.AtomicReference;

import io.vertx.rxcore.java.impl.SingleSubscriptionHandler;
import org.vertx.java.core.Vertx;
import rx.Observable;

/** RxTimer */
public class RxTimer {

  // Definitions
  
  /** Base-class Handler for Timers with cancel on unsubscribe */
  protected class TimerHandler<R> extends SingleSubscriptionHandler<R,R> {
    
    /** Timer Id */    
    protected AtomicReference<Long> timerRef=new AtomicReference<Long>();
    
    /** Cancel timer on unsubscribe */
    @Override public void onUnsubscribed() {
      
      // Cancel the timer if still active
      Long timerId=timerRef.getAndSet(null);
      if (timerId!=null)
        RxTimer.this.core.cancelTimer(timerId);
    }
  }

  // Instance variables
  
  /** Core */
  private Vertx core;
  
  // Public
  
  /** Create new RxTimer */
  public RxTimer(Vertx core) {
    this.core=core;
  }
  
  /** Set One-Time Timer 
   *
   * <p>Timer is set on subscribe, and cancelled on unsubscribe (if not triggered)</p> 
   * 
   **/
  public Observable<Long> setTimer(final long delay) {
    return Observable.create(new TimerHandler<Long>() {
      @Override public void execute() {
        timerRef.set(RxTimer.this.core.setTimer(delay,this));
      }
      @Override public void handle(Long res) {
        fireResult(res);
      }
    });
  }

  /** Set Periodic Timer 
   *
   * <p>Timer is set on subscribe, and cancelled on unsubscribe</p> 
   * 
   **/
  public Observable<Long> setPeriodic(final long delay) {
    return Observable.create(new TimerHandler<Long>() {
      @Override public void execute() {
        timerRef.set(RxTimer.this.core.setPeriodic(delay,this));
      }
    });
  }
}
