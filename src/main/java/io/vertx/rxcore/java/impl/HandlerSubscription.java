package io.vertx.rxcore.java.impl;

import org.vertx.java.core.Handler;
import rx.Subscriber;
import rx.Subscription;

import java.util.concurrent.atomic.AtomicReference;

/** Mapping from Handler to Subscription */
public class HandlerSubscription<R,T> implements Subscription, Handler<R> {

  /** Subscriber */
  private AtomicReference<Subscriber<T>> subRef;

  /** Create new HandlerSubscription */
  public HandlerSubscription(Subscriber<T> s) {
    this.subRef=new AtomicReference<>(s);
  }

  // Handler implementation

  /** Handle event */
  public void handle(R evt) {
    fireComplete((T)evt);
  }

  // Subscription implementation

  /** Unsubscribe */
  public void unsubscribe() {
    this.subRef.set(null);
  }

  /** Return true if unsubscribed */
  public boolean isUnsubscribed() {
    return this.subRef.get()==null;
  }

  // Implementation

  /** Fire completed */
  protected void fireComplete(T res) {
    Subscriber<T> s=this.subRef.getAndSet(null);
    if ((s==null) || (s.isUnsubscribed()))
      return;

    s.onNext(res);
    s.onCompleted();
  }

  /** Fire error */
  protected void fireError(Throwable err) {
    Subscriber s=this.subRef.getAndSet(null);
    if ((s==null) || (s.isUnsubscribed()))
      return;

    s.onError(err);
  }
}
