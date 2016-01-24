package io.vertx.rxcore.java.impl;

import org.vertx.java.core.Context;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.MultiThreadedWorkerContext;
import rx.Scheduler;
import rx.Subscription;
import rx.functions.Action0;
import rx.plugins.RxJavaPlugins;
import rx.plugins.RxJavaSchedulersHook;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;

import java.util.ArrayDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/** Implementation of Scheduler that runs on a given Verticle Context */
public class ContextScheduler extends Scheduler {

  // Instance variables

  /** Vertx */
  private final Vertx vertx;
  private final Context context;

  /** Create new ContextScheduler */
  public ContextScheduler(Vertx vertx, Context context) {
    this.vertx=vertx;
    this.context=context;
  }

  // Scheduler implementation

  /** Create worker */
  @Override
  public Worker createWorker() {
    return new ContextWorker();
  }

  @Override
  public int parallelism() {
    /* Only the uncommon Vertx MultiThreadedWorkerContext could be
     * considered to be parallel in the sense meant (I think?), so
     * claim to have no parallelism otherwise.
     */
    if (context instanceof MultiThreadedWorkerContext) {
        /* maybe return the real vertx worker thread pool size,
         * not sure that's available anywhere in public api though.
         */
        return super.parallelism();
    }
    // must be one of the ordered contexts -> no parallelism intra context
    return 1;
  }

  /* ContextAction is conceptually similar to rx.internal.schedulers.ScheduledAction
   */
  private static final class ContextAction implements Subscription {
    private final Action0 action;
    private final CompositeSubscription cancel;
    private final Context context;
    private final Vertx vertx;
    private final ArrayDeque<Long> timers;

    @SuppressWarnings("unused")
    volatile int once;
    static final AtomicIntegerFieldUpdater<ContextAction> ONCE_UPDATER
            = AtomicIntegerFieldUpdater.newUpdater(ContextAction.class, "once");

    ContextAction(Vertx vertx, Context context, final Action0 action) {
      this.vertx = vertx;
      this.context = context;
      this.action = action;
      this.cancel = new CompositeSubscription();
      this.timers = new ArrayDeque<Long>();
    }


    @Override
    public void unsubscribe() {
      if (ONCE_UPDATER.compareAndSet(this, 0, 1)) {
        context.runOnContext(new Handler<Void>() {
          @Override
          public void handle(Void event) {
            while (!timers.isEmpty()) {
              vertx.cancelTimer(timers.poll());
            }
          }
        });
        cancel.unsubscribe();
      }
    }

    @Override
    public boolean isUnsubscribed() {
        return cancel.isUnsubscribed();
    }

    public void runOnContext() {
      context.runOnContext(new Handler<Void> () {
        @Override
        public void handle(final Void event) {
          try {
            action.call();
          }
          finally {
            // one shot
            unsubscribe();
          }
        }
      });
    }

    public void setTimer(final long delayTimeMillis) {
      // vertx.setTimer() rejects delays < 1 msec, just run.
      if (delayTimeMillis < 1) {
        runOnContext();
      }
      else {
      context.runOnContext(new Handler<Void>() {
        @Override
        public void handle(Void event) {
          timers.add(vertx.setTimer(delayTimeMillis, new Handler<Long>() {
            public void handle(Long nestedId) {
              try {
                action.call();
              }
              finally {
                // one shot
                unsubscribe();
              }
            }
          }));
      }});
      }
    }

    public void setDelayedPeriodic(final long initialDelayMillis, final long periodMillis) {

      /* The period from schedulePeriodically() is documented to be potentially
       * 0 or negative, in which case impls are supposed to repeat the action
       * with no delay.
       * vertx.setPeriodic() rejects periods < 1 msec though. Nonetheless, using
       * runOnContext to requeue seems safe...ish..., as other things can in
       * principle still interleave. Alternatively (and perhaps less scarily),
       * we could quietly bump periodMillis < 1 to 1...
       */

      if (periodMillis<1) {
        // If initialDelayMillis is 0 then bootstrap immediately
        if (initialDelayMillis<1) {
          // Start repeating with no interrepeat delay
          context.runOnContext(new Handler<Void>() {
            @Override
            public void handle(Void event) {
              action.call();
              if (!cancel.isUnsubscribed()) { // is synchronized
                context.runOnContext(this);
              }
            }
          });
        }
        else {
          context.runOnContext(new Handler<Void>() {
            @Override
            public void handle(Void event) {
              timers.add(vertx.setTimer(initialDelayMillis, new Handler<Long>() {
                @Override
                public void handle(Long tid) {
                  action.call();
                  // Start repeating with no interepeat delay.
                  context.runOnContext(new Handler<Void>() {
                    @Override
                    public void handle(Void event) {
                      action.call();
                      if (!cancel.isUnsubscribed()) { // is synchronized
                        context.runOnContext(this);
                      }
                    }
                  });
                }
              }));
            }
          });
        }
      }
      else {
        // If initialDelayMillis is 0 then bootstrap immediately
        if (initialDelayMillis<1) {
          context.runOnContext(new Handler<Void>() {
            @Override
            public void handle(Void event) {
              action.call();
              // Start the repeating timer
              timers.add(vertx.setPeriodic(periodMillis, new Handler<Long>() {
                @Override
                public void handle(Long nestedId) {
                  action.call();
                }
              }));
            }
          });
        }
        else {
          context.runOnContext(new Handler<Void>() {
            @Override
            public void handle(Void event) {
              timers.add(vertx.setTimer(initialDelayMillis, new Handler<Long>() {
                @Override
                public void handle(Long tid) {
                  action.call();
                  // Start the repeating timer
                  timers.add(vertx.setPeriodic(periodMillis, new Handler<Long>() {
                    @Override
                    public void handle(Long nestedId) {
                      action.call();
                    }
                  }));
                }
              }));
            }
          });
        }
      }
    }

    public void addParent(final CompositeSubscription parent) {
      cancel.add(new Remover(this, parent));
    }

    // See rx.internal.schedulers.ScheduledAction.Remover
    /** Remove a child subscription from a composite when unsubscribing. */
    private static final class Remover implements Subscription {
      final Subscription s;
      final CompositeSubscription parent;
      @SuppressWarnings("unused")
      volatile int once;
      static final AtomicIntegerFieldUpdater<Remover> ONCE_UPDATER
       = AtomicIntegerFieldUpdater.newUpdater(Remover.class, "once");

      public Remover(final Subscription s, final CompositeSubscription parent) {
        this.s = s;
        this.parent = parent;
      }

      @Override
      public boolean isUnsubscribed() {
        return s.isUnsubscribed();
      }

      @Override
      public void unsubscribe() {
        if (ONCE_UPDATER.compareAndSet(this, 0, 1)) {
          parent.remove(s);
        }
      }
    }
  }

  // Scheduler.Worker implementation

  /** Worker */
  private class ContextWorker extends Scheduler.Worker {

    private final RxJavaSchedulersHook schedulersHook;

    public ContextWorker() {
      schedulersHook = RxJavaPlugins.getInstance().getSchedulersHook();
    }

    protected CompositeSubscription innerSubscription=new CompositeSubscription();

    @Override
    public Subscription schedule(final Action0 action) {
      if (innerSubscription.isUnsubscribed()) {
        // don't schedule, we are unsubscribed
        return Subscriptions.empty();
      }
      Action0 decoratedAction = schedulersHook.onSchedule(action);

      ContextAction c = new ContextAction(vertx, context, decoratedAction);
      c.runOnContext();
      c.addParent(innerSubscription);
      return c;
    }

    @Override
    public Subscription schedule(final Action0 action, final long delayTime, final TimeUnit unit) {
      if (innerSubscription.isUnsubscribed()) {
        // don't schedule, we are unsubscribed
        return Subscriptions.empty();
      }
      Action0 decoratedAction = schedulersHook.onSchedule(action);

      final long delayTimeMillis = unit.toMillis(delayTime);

      ContextAction c = new ContextAction(vertx, context, decoratedAction);
      c.setTimer(delayTimeMillis);
      c.addParent(innerSubscription);
      return c;
    }

    @Override
    public Subscription schedulePeriodically(final Action0 action, long initialDelay, final long period, final TimeUnit unit) {
      if (innerSubscription.isUnsubscribed()) {
        // don't schedule, we are unsubscribed
        return Subscriptions.empty();
      }
      Action0 decoratedAction = schedulersHook.onSchedule(action);

      final long initialDelayMillis = unit.toMillis(initialDelay);
      final long periodMillis = unit.toMillis(period);

      ContextAction c = new ContextAction(vertx, context, decoratedAction);
      c.setDelayedPeriodic(initialDelayMillis, periodMillis);
      c.addParent(innerSubscription);
      return c;
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
