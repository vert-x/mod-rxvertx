package io.vertx.rxcore.java.impl;

import java.util.concurrent.atomic.AtomicReference;

import org.vertx.java.core.Handler;
import rx.*;

/** Handler tied to a single Subscription 
 * @author <a href="http://github.com/petermd">Peter McDonnell</a>
 **/
public class SubscriptionHandler<R,T> implements Observable.OnSubscribe<R>, Handler<T> {

  /** Observer reference */
  protected AtomicReference<Subscriber<? super R>> subRef =new AtomicReference<Subscriber<? super R>>();

  /** Create new SubscriptionHandler */
  public SubscriptionHandler() {
    this.subRef =new AtomicReference<>();
  }
  
  /** Execute */
  public void execute() {
  }

  /** Unsubscribe */
  public void onUnsubscribed() {
  }

  // OnSubscribe

  /** Subscription */
  public void call(Subscriber<? super R> ob) {
    if (!this.subRef.compareAndSet(null,ob)) {
      throw new IllegalStateException("Cannot have multiple subscriptions");
    }

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
    s.onCompleted();

    onUnsubscribed();
    subRef.set(null);
  }

  /** Fire completed to active observer */
  protected void fireComplete() {
    Subscriber<? super R> s=getSubscriber();
    if (s==null)
      return;

    s.onCompleted();

    onUnsubscribed();
    subRef.set(null);
  }
  
  /** Fire error to active observer */
  protected void fireError(Throwable t) {

    Subscriber<? super R> s=getSubscriber();
    if (s==null)
      return;
    
    s.onError(t);

    onUnsubscribed();
    subRef.set(null);
  }

  /** Get subscriber */
  protected Subscriber getSubscriber() {
    Subscriber<? super R> sub=this.subRef.get();
    if (sub==null)
      return null;

    // If subscriber has unsubscribed then process first then
    // remove reference
    if (sub.isUnsubscribed()) {
      // Cleanup handler
      onUnsubscribed();
      // Unsunscribe should trigger onCompleted
      sub.onCompleted();
      // Clear reference so we are now done
      this.subRef.set(null);
      return null;
    }

    return sub;
  }
}
