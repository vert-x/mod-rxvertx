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

  /** Create worker */
  @Override
  public Worker createWorker() {
    return new ContextWorker();
  }

  // Scheduler.Worker implementation

  /** Worker */
  private class ContextWorker extends Worker {

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

    // Scheduler.Worker implementation

    @Override
    public Subscription schedule(final Action0 action) {
      vertx.currentContext().runOnContext(new Handler<Void>() {
        public void handle(Void event) {
          if (innerSubscription.isUnsubscribed())
            return;
          action.call();
        }
      });
      return this.innerSubscription;
    }

    @Override
    public Subscription schedule(final Action0 action, long delayTime, TimeUnit unit) {
      timers.add(vertx.setTimer(unit.toMillis(delayTime),new Handler<Long>() {
        public void handle(Long id) {
          if (innerSubscription.isUnsubscribed())
            return;
          action.call();
          timers.remove(id);
        }
      }));
      return this.innerSubscription;
    }

    @Override
    public Subscription schedulePeriodically(final Action0 action, long initialDelay, final long delayTime, final TimeUnit unit) {

      // Use a bootstrap handler to start the periodic timer after initialDelay
      Handler bootstrap=new Handler<Long>() {
        public void handle(Long id) {

          action.call();

          // Ensure still active
          if (innerSubscription.isUnsubscribed())
            return;

          // Start the repeating timer
          timers.add(vertx.setPeriodic(unit.toMillis(delayTime),new Handler<Long>() {
            public void handle(Long nestedId) {
              if (innerSubscription.isUnsubscribed())
                return;
              action.call();
            }
          }));
        }
      };

      long bootDelay=unit.toMillis(initialDelay);

      // If initialDelay is 0 then fire bootstrap immediately
      if (bootDelay<1) {
        vertx.runOnContext(bootstrap);
      }
      else {
        timers.add(vertx.setTimer(bootDelay,bootstrap));
      }

      return this.innerSubscription;
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
