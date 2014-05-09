package io.vertx.rxcore.java.impl;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import rx.Scheduler;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.subscriptions.BooleanSubscription;

import java.util.ArrayDeque;
import java.util.concurrent.TimeUnit;

/** Implementation of Scheduler that runs on the Verticle Context */
public class ContextScheduler extends Scheduler {

  // Instance variables

  /** Vertx */
  private final Vertx vertx;

  /** Create new ContextScheduler */
  public ContextScheduler(Vertx vertx) {
    this.vertx=vertx;
  }

  // Scheduler implementation

  /** Schedule immediate task */
  @Override
  public Subscription schedule(final Action1<Inner> action) {
    InnerScheduler inner=new InnerScheduler();
    inner.schedule(action);
    return inner.innerSubscription;
  }

  /** Scheduler task after delay */
  @Override
  public Subscription schedule(final Action1<Inner> action, long delayTime, TimeUnit unit) {
    InnerScheduler inner=new InnerScheduler();
    inner.schedule(action,delayTime,unit);
    return inner.innerSubscription;
  }

  /** Schedule repeating task */
  @Override
  public Subscription schedulePeriodically(final Action1<Inner> action, long initialDelay, final long period, final TimeUnit unit) {

    final InnerScheduler inner=new InnerScheduler();

    Handler bootstrap=new Handler() {
      public void handle(Object event) {

        action.call(inner);

        // Check if still active
        if (inner.innerSubscription.isUnsubscribed())
          return;

        // Start periodic sequence
        inner.timers.add(vertx.setPeriodic(unit.toMillis(period),new Handler<Long>() {
          public void handle(Long event) {
            if (inner.innerSubscription.isUnsubscribed())
              return;
            action.call(inner);
          }
        }));
      }
    };

    // initialDelay=0 -> schedule next
    if (unit.toMillis(initialDelay)<1) {
      vertx.runOnContext(bootstrap);
    }
    else {
      vertx.setTimer(unit.toMillis(initialDelay),bootstrap);
    }

    return inner.innerSubscription;
  }

  // Inner scheduler

  /** Inner Scheduler */
  private class InnerScheduler extends Inner {

    /** Maintain list of all active timers */
    protected ArrayDeque<Long> timers=new ArrayDeque();

    /** Cancel all timers */
    protected Action0 cancelAll=new Action0() {
      public void call() {
        while (!timers.isEmpty())
          vertx.cancelTimer(timers.poll());
      }
    };

    /** Subscription with auto-cancel */
    protected BooleanSubscription innerSubscription=BooleanSubscription.create(cancelAll);

    // Scheduler.Inner implementation

    @Override
    public void schedule(final Action1<Inner> action) {
      final Inner self=this;
      vertx.currentContext().runOnContext(new Handler<Void>() {
        public void handle(Void event) {
          if (innerSubscription.isUnsubscribed())
            return;
          action.call(self);
        }
      });
    }

     @Override
    public void schedule(final Action1<Inner> action, long delayTime, TimeUnit unit) {
      final Inner self=this;
      timers.add(vertx.setTimer(unit.toMillis(delayTime),new Handler<Long>() {
        public void handle(Long id) {
          if (innerSubscription.isUnsubscribed())
            return;
          action.call(self);
          timers.remove(id);
        }
      }));
    }

    @Override
    public void unsubscribe() {
      innerSubscription.unsubscribe();
    }

    @Override
    public boolean isUnsubscribed() {
      return innerSubscription.isUnsubscribed();
    }
  }
}
