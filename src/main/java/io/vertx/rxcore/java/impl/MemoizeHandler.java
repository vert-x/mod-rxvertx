package io.vertx.rxcore.java.impl;

import java.util.concurrent.atomic.AtomicReference;

import org.vertx.java.core.Handler;
import rx.Observer;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import rx.util.functions.Action0;
import rx.util.functions.Func1;

/** Subject that stores the result of a Handler and notfies all current and future Observers 
 * @author <a href="http://github.com/petermd">Peter McDonnell</a>
 **/
public class MemoizeHandler<R,T> implements Handler<T> {
  
  /** States */
  enum State { ACTIVE, COMPLETED, FAILED };
  
  /** State */
  private State state;
  
  /** Result */
  private R result;
  
  /** Error */
  private Exception error;
  
  /** Reference to active observer */
  private AtomicReference<Observer<R>> obRef=new AtomicReference<Observer<R>>();
  
  /** Create new MemoizeHandler */
  public MemoizeHandler() {
    this.state=State.ACTIVE;
    this.result=null;
    this.error=null;
  }
  
  /** Subscription function */
  public Func1<Observer<R>, Subscription> subscribe=new Func1<Observer<R>, Subscription>() {
    public Subscription call(Observer<R> newObserver) {
      // Check if complete
      switch(state) {

        // Completed. Forward the saved result
        case COMPLETED:
          newObserver.onNext(result);
          newObserver.onCompleted();
          return Subscriptions.empty();
        
        // Failed already. Forward the saved error
        case FAILED:
          newObserver.onError(error);
          return Subscriptions.empty();
      }
      
      // State=ACTIVE
      if (!obRef.compareAndSet(null, newObserver))
        throw new IllegalStateException("Cannot have multiple subscriptions");
      
      return Subscriptions.create(unsubscribe);
    }
  };

  /** Unsubscribe action */
  public Action0 unsubscribe=new Action0() {
    public void call() {
      Observer<R> ob=obRef.getAndSet(null);
      if (ob==null)
        throw new IllegalStateException("Unsubscribe without subscribe");
      // Unsubscribe triggers completed
      ob.onCompleted();
    }
  };

  /** Dispatch complete */
  public void complete(R value) {
    this.result=value;
    this.state=State.COMPLETED;

    Observer<R> ob=obRef.get();
    // Ignore if no active observer
    if (ob==null)
      return;

    try {
      ob.onNext(value);
    }
    catch (Exception e) {
      e.printStackTrace(); // FIXME: logging
      ob.onError(e);
    }

    try {
      ob.onCompleted();
    }
    catch (Exception e) {
      e.printStackTrace(); // FIXME: logging
      ob.onError(e);
    }
  }
  
  /** Dispatch failure */
  public void fail(Exception e) {
    this.error=e;
    this.state=State.FAILED;

    Observer<R> ob=obRef.get();
    // Ignore if no active observer
    if (ob==null)
      return;

    try {
      ob.onError(e);
    }
    catch (Exception ee) {
      // Ignore error in exception handler
      ee.printStackTrace(); // FIXME: logging
    }
  }
  
  // Handler implementation
  
  /** Complete */
  @SuppressWarnings("unchecked")
  public void handle(T value) {
    // Default: Assume same type
    complete((R)value);
  }
}
