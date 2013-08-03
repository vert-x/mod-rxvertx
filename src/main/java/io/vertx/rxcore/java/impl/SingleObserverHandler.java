package io.vertx.rxcore.java.impl;

import java.util.concurrent.atomic.AtomicReference;

import org.vertx.java.core.Handler;
import rx.Observer;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import rx.util.functions.Action0;
import rx.util.functions.Func1;

/** Mapping from Handler to Observer that supports a single subscription and wrapping of the response object.
 *
 * <p>Sub-classes must implement register() to attach the Handler to the relevant callback. This will only happen
 * once the subscription is made</p> 
 * 
 * @author <a href="http://github.com/petermd">Peter McDonnell</a>
 **/
public abstract class SingleObserverHandler<R, T> implements Handler<T> {

  /** Reference to active observer */
  private AtomicReference<Observer<R>> obRef=new AtomicReference<Observer<R>>();

  /** Subscription function */
  public Func1<Observer<R>, Subscription> subscribe=new Func1<Observer<R>, Subscription>() {
    public Subscription call(Observer<R> newObserver) {
      if (!obRef.compareAndSet(null, newObserver))
        throw new IllegalStateException("Cannot have multiple subscriptions");
      register();
      return Subscriptions.create(unsubscribe);
    }
  };

  /** Unsubscribe action */
  public Action0 unsubscribe = new Action0() {
    public void call() {
      Observer<R> ob=obRef.getAndSet(null);
      if (ob==null)
        throw new IllegalStateException("Unsubscribe without subscribe");
      try {
        clear();
      }
      catch(Throwable t) {
        // unregistering handlers can cause expected errors. ignore them blindly here FIXME: logging
      }
      // Unsubscribe triggers completed
      ob.onCompleted();
    }
  };

  /** Override to register handler */
  public abstract void register();

  /** Override to clear handler */
  public void clear() {
  }

  /** Override to wrap value */
  @SuppressWarnings("unchecked")
  public R wrap(T value) {
    return (R)value;
  }
  
  /** Complete the handler */
  public void complete() {

    Observer<R> ob=obRef.get();

    // Ignore if no active observer
    if (ob==null)
      return;

    ob.onCompleted();

    // Clear the observer ref, there is no next/completed/unsubscribe to follow
    obRef.set(null);
  }
  
  /** Fail the handler - used to handle errors before the handler is called */
  public void fail(Throwable e) {
    
    Observer<R> ob=obRef.get();

    // Ignore if no active observer
    if (ob==null)
      return;
    
    ob.onError(e);

    // Clear the observer ref, there is no next/completed/unsubscribe to follow
    obRef.set(null);
  }

  // Handler implementation

  public void handle(T value) {
    
    Observer<R> ob=obRef.get();

    // Ignore if no active observer
    if (ob==null)
      return;

    try {
      ob.onNext(wrap(value));
    }
    catch (Exception e) {
      
      // onNext should catch any error related to an individual message and
      // avoid killing the Observable. If it doesnt then we propogate the problem 
      // and close the observable

      ob.onError(e);

      // Clear the observer ref - we are done
      obRef.set(null);
    }
  }
}
