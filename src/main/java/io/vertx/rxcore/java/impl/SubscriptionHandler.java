package io.vertx.rxcore.java.impl;

import java.util.concurrent.atomic.AtomicReference;

import org.vertx.java.core.Handler;
import rx.*;
import rx.subscriptions.Subscriptions;

/** Handler tied to a single Subscription 
 * @author <a href="http://github.com/petermd">Peter McDonnell</a>
 **/
public class SubscriptionHandler<R,T> implements Observable.OnSubscribeFunc<R>, Subscription, Handler<T> {

  /** Observer reference */
  protected AtomicReference<Observer<? super R>> obRef=new AtomicReference<Observer<? super R>>();

  /** Create new SubscriptionHandler */
  public SubscriptionHandler() {
    this.obRef=new AtomicReference<>();
  }
  
  /** Execute */
  public void execute() {
  }

  // OnSubscribeFunc
  
  public Subscription onSubscribe(Observer<? super R> ob) {
    if (!this.obRef.compareAndSet(null,ob)) {
      throw new IllegalStateException("Cannot have multiple subscriptions");
    }

    try {
      execute();
      return this;
    }
    // If the execution fails then assume then handle() will never be called and 
    // emit an error
    catch(Throwable t) {
      fireError(t);
      return Subscriptions.empty();
    }
  }
  
  // Handler implementation
  
  /** Handle response */
  public void handle(T msg) {
    // Default operation -> assume R===T and stream
    fireNext((R)msg);
  }

  // Subscription implementation

  /** Unsubscribe */
  public void unsubscribe() {
    // Clear reference to ensure we do not propogate
    Observer<? super R> o=this.obRef.getAndSet(null);
    if (o==null)
       return;
    
    o.onCompleted();
  }
  
  // Implementation
  
  /** Fire next to active observer */
  protected void fireNext(R next) {

    Observer<? super R> o=this.obRef.get();
    if (o==null)
      return;
    
    o.onNext(next);
  }

  /** Fire result to active observer */
  protected void fireResult(R res) {

    Observer<? super R> o=this.obRef.getAndSet(null);
    if (o==null)
      return;
    
    o.onNext(res);
    o.onCompleted();
  }
  
  /** Fire error to active observer */
  protected void fireError(Throwable t) {

    Observer<? super R> o=this.obRef.getAndSet(null);
    if (o==null)
      return;
    
    o.onError(t);
  }
}
