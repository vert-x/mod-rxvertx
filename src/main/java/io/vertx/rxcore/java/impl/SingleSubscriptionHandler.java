package io.vertx.rxcore.java.impl;

import java.util.concurrent.atomic.AtomicReference;

import org.vertx.java.core.Handler;
import rx.*;

/** Handler tied to a single Subscription 
 * @author <a href="http://github.com/petermd">Peter McDonnell</a>
 **/
public class SingleSubscriptionHandler<R,T> implements Observable.OnSubscribe<R>, Handler<T> {

  class SingleSubscription implements Subscription {

    /** Subscriber */
    public final Subscriber<? super R> subscriber;

    /** Create new SingleSubscription */
    public SingleSubscription(Subscriber<? super R> subscriber) {
      this.subscriber=subscriber;
    }

    /** Handle */
    public void unsubscribe() {

      if (isUnsubscribed())
        return;

      // Only trigger onUnsubscribed if we were the active subscription
      if (subRef.compareAndSet(this,null)) {
        // Trigger completed
        this.subscriber.onCompleted();
        // Handle unsubscribe
        onUnsubscribed();
      }
    }

    /** Check unsubscribed */
    public boolean isUnsubscribed() {
      // Check if still the active subscription
      return subRef.get()!=this;
    }
  }

  /** Observer reference */
  protected AtomicReference<SingleSubscription> subRef =new AtomicReference<>();

  /** Create new SingleSubscriptionHandler */
  public SingleSubscriptionHandler() {
    this.subRef=new AtomicReference<>();
  }
  
  /** Execute */
  public void execute() {
  }

  /** Unsubscribe */
  public void onUnsubscribed() {
  }

  // OnSubscribe

  /** Subscription */
  public void call(Subscriber<? super R> sub) {

    SingleSubscription singleSub=new SingleSubscription(sub);

    if (!this.subRef.compareAndSet(null, singleSub)) {
      throw new IllegalStateException("Cannot have multiple subscriptions");
    }

    sub.add(singleSub);

    try {
      execute();
    }
    // If the execution fails then assume then handle() will never be called and 
    // emit an error
    catch(Throwable t) {
      fireError(t);
    }
  }
  
  // Handler implementation

  /** Override to wrap value */
  @SuppressWarnings("unchecked")
  public R wrap(T value) {
    return (R)value;
  }

  /** Handle response */
  public void handle(T msg) {
    // Assume stream
    fireNext(wrap(msg));
  }

  // Implementation
  
  /** Fire next to active observer */
  protected void fireNext(R next) {

    Subscriber<? super R> s=getSubscriber();
    if (s==null)
      return;

    s.onNext(next);
  }

  /** Fire result to active observer */
  protected void fireResult(R res) {

    Subscriber<? super R> s=getSubscriber();
    if (s==null)
      return;

    s.onNext(res);

    this.subRef.set(null);

    s.onCompleted();
  }

  /** Fire completed to active observer */
  protected void fireComplete() {
    Subscriber<? super R> s=getSubscriber();
    if (s==null)
      return;

    this.subRef.set(null);

    s.onCompleted();
  }
  
  /** Fire error to active observer */
  protected void fireError(Throwable t) {

    Subscriber<? super R> s=getSubscriber();
    if (s==null)
      return;

    this.subRef.set(null);

    s.onError(t);
  }

  /** Get subscriber */
  protected Subscriber getSubscriber() {

    SingleSubscription singleSub=this.subRef.get();

    return (singleSub!=null)?singleSub.subscriber:null;
  }
}
