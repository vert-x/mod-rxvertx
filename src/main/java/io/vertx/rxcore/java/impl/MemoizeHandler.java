package io.vertx.rxcore.java.impl;

import java.util.concurrent.atomic.AtomicReference;

import org.vertx.java.core.Handler;
import rx.*;
import rx.subscriptions.Subscriptions;

/** Handler that stores the result and provides it to a single Subscriber
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
  private Throwable error;
  
  /** Reference to active subscriber */
  private AtomicReference<Subscriber<? super R>> subRef =new AtomicReference<Subscriber<? super R>>();
  
  /** Create new MemoizeHandler */
  public MemoizeHandler() {
    this.state=State.ACTIVE;
    this.result=null;
    this.error=null;
  }
  
  /** Subscription function */
  public Observable.OnSubscribe<R> subscribe=new Observable.OnSubscribe<R>() {
    public void call(Subscriber<? super R> newSubscriber) {
      // Check if complete
      switch(state) {

        // Completed. Forward the saved result
        case COMPLETED:
          newSubscriber.onNext(result);
          newSubscriber.onCompleted();
          return;
        
        // Failed already. Forward the saved error
        case FAILED:
          newSubscriber.onError(error);
          return;
      }
      
      // State=ACTIVE
      if (!subRef.compareAndSet(null, newSubscriber))
        throw new IllegalStateException("Cannot have multiple subscriptions");
    }
  };

  /** Dispatch complete */
  public void complete(R value) {
    this.result=value;
    this.state=State.COMPLETED;

    Observer<? super R> ob=getObserver();
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

    Observer<? super R> ob=getObserver();
    // Ignore if no active observer
    if (ob==null)
      return;

    ob.onError(e);
  }
  
  // Handler implementation
  
  /** Complete */
  @SuppressWarnings("unchecked")
  public void handle(T value) {
    complete((R)value);
  }

  // Implementation

  /** Return Observer */
  protected Observer<? super R> getObserver() {
    Subscriber<? super R> s=subRef.get();

    return (s!=null) && !s.isUnsubscribed()?s:null;
  }
}
