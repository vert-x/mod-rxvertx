package io.vertx.rxcore.java.impl;

import java.util.concurrent.atomic.AtomicReference;

import org.vertx.java.core.Handler;
import rx.*;
import rx.subscriptions.Subscriptions;

/** Handler that stores the result and provides it to a single Observer 
 * @author <a href="http://github.com/petermd">Peter McDonnell</a>
 **/
public class MemoizeHandler<R,T> implements Handler<T>,Subscription {
  
  /** States */
  enum State { ACTIVE, COMPLETED, FAILED };
  
  /** State */
  private State state;
  
  /** Result */
  private R result;
  
  /** Error */
  private Throwable error;
  
  /** Reference to active observer */
  private AtomicReference<Observer<? super R>> obRef=new AtomicReference<Observer<? super R>>();
  
  /** Create new MemoizeHandler */
  public MemoizeHandler() {
    this.state=State.ACTIVE;
    this.result=null;
    this.error=null;
  }
  
  /** Subscription function */
  public Observable.OnSubscribeFunc<R> subscribe=new Observable.OnSubscribeFunc<R>() {
    public Subscription onSubscribe(Observer<? super R> newObserver) {
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

      // Subscription
      return MemoizeHandler.this;
    }
  };

  /** Dispatch complete */
  public void complete(R value) {
    this.result=value;
    this.state=State.COMPLETED;

    Observer<? super R> ob=obRef.get();
    // Ignore if no active observer
    if (ob==null)
      return;

    ob.onNext(value);
    ob.onCompleted();
  }
  
  /** Dispatch failure */
  public void fail(Throwable e) {
    this.error=e;
    this.state=State.FAILED;

    Observer<? super R> ob=obRef.get();
    // Ignore if no active observer
    if (ob==null)
      return;

    ob.onError(e);
  }
  
  // Handler implementation
  
  /** Complete */
  @SuppressWarnings("unchecked")
  public void handle(T value) {
    // Default: Assume same type
    complete((R)value);
  }

  // Subscription implementation
  
  /** Unsubscribe */
  public void unsubscribe() {
    Observer<? super R> ob=obRef.getAndSet(null);
    if (ob==null)
      throw new IllegalStateException("Unsubscribe without subscribe");
    // Unsubscribe triggers completed
    ob.onCompleted();
  }
}
