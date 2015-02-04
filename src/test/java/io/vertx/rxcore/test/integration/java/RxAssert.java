package io.vertx.rxcore.test.integration.java;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import io.vertx.rxcore.java.eventbus.RxMessage;
import rx.Observable;
import rx.Subscription;
import rx.plugins.RxJavaErrorHandler;
import rx.plugins.RxJavaPlugins;
import rx.functions.Action0;
import rx.functions.Action1;
import static org.vertx.testtools.VertxAssert.*;


/** Assertion utilities for Rx Observables
 * @author <a href="http://github.com/petermd">Peter McDonnell</a>
 **/
public class RxAssert {
  
  static {
    try {
      RxJavaPlugins.getInstance().registerErrorHandler(new RxJavaErrorHandler() {
        @Override 
        public void handleError(Throwable t) {
          System.err.println("RxJava-error: "+t);
        }
      });
      
      System.out.println("INFO Registered rxjava plugin error handler");
    }
    catch(Throwable t) {
      System.err.println("FATAL Unable to register rxjava plugin error handler (t="+t+")");
      t.printStackTrace(System.err);
    }
  }

  /** Action0 for completing test */
  static Action0 thenComplete=new Action0() {
    public void call() {
      testComplete();
    }
  };

  /** Assert a message */
  public static <T> void assertMessageThenComplete(Observable<RxMessage<T>> in, final T exp) {
    final AtomicInteger count=new AtomicInteger(1);
    in.subscribe(
      new Action1<RxMessage<T>>() {
        public void call(RxMessage<T> value) {
          assertEquals(exp,value.body());
          assertEquals(0,count.decrementAndGet());
          System.out.println("got:"+value.body());
        }
      },
      new Action1<Throwable>() {
        public void call(Throwable t) {
          fail("Error while mapping message (t="+t+")");
        }
      },
      new Action0() {
        public void call() {
          assertEquals(0,count.get());
          testComplete();
        }
      });
  }

  /** Assert a single value */
  public static <T> void assertSingle(Observable<T> in, final T value) {
    assertSequence(in,value);
  }

  /** Assert a sequence */
  public static <T> void assertSequence(Observable<T> in, final T... exp) {
    assertSequenceThen(in, new Action0() {
      @Override public void call() {
        // Do nothing
      }
    }, exp);
  }

  /** Assert that we receive N values */
  public static <T> Subscription assertCount(Observable<T> in, final int max) {
    return assertCountThen(in,new Action0() {
      @Override public void call() {
      }
    },max);
  }

  /** Assert that we receive N values then complete test */
  public static <T> Subscription assertCountThenComplete(Observable<T> in, final int max) {
    return assertCountThen(in,thenComplete,max);
  }

  /** Assert that we receive N values then call action */
  public static <T> Subscription assertCountThen(Observable<T> in, final Action0 thenAction, final int max) {
    final AtomicInteger count=new AtomicInteger(0);
    return in.subscribe(
      new Action1<T>() {
        public void call(T value) {
          assertTrue(count.incrementAndGet()<=max);
        }
      },
      new Action1<Throwable>() {
        public void call(Throwable t) {
          fail("Error while counting sequence (t="+t+")");
        }
      },
      new Action0() {
        public void call() {
          assertTrue(count.get()==max);
          System.out.println("sequence-complete");
          thenAction.call();
        }
      });
  }

  /** Assert sequence completes */
  public static <T> void assertCompletes(Observable<T> in) {
    assertSequenceThenComplete(Observable.from(new Void[] {}));
  }

  /** Assert a single value then complete test */
  public static <T> void assertSingleThenComplete(Observable<T> in, final T value) {
    assertSequenceThenComplete(in,value);
  }

  /** Assert a sequence then complete test */
  public static <T> void assertSequenceThenComplete(Observable<T> in, final T... exp) {
    assertSequenceThen(in, thenComplete, exp);
  }
  
  /** Assert a sequence then call an Action0 when complete */
  public static <T> void assertSequenceThen(Observable<T> in, final Action0 thenAction, final T... exp) {
    final List<T> expList=new ArrayList(Arrays.asList(exp));
    in.subscribe(
      new Action1<T>() {
        public void call(T value) {
          assertEquals(expList.remove(0),value);
        }
      },
      new Action1<Throwable>() {
        public void call(Throwable t) {
          fail("Error while mapping sequence (t="+t+")");
        }
      },
      new Action0() {
        public void call() {
          assertTrue(expList.isEmpty());
          System.out.println("sequence-complete");
          thenAction.call();
        }
      });
  }

  /** Assert an expected error */
  public static <T> void assertErrorThenComplete(Observable<T> in, final Class errClass) {
    assertErrorThenComplete(in, errClass, null);
  }

  /** Assert an expected error */
  public static <T> void assertErrorThenComplete(Observable<T> in, final Class errClass, final String errMsg) {
    assertErrorThen(in, thenComplete, errClass, errMsg);
  }

  /** Assert an expected error */
  public static <T> void assertErrorThen(Observable<T> in, final Action0 thenAction, final Class errClass) {
    assertErrorThen(in,thenAction,errClass,null);
  }

  /** Assert an expected error */
  public static <T> void assertErrorThen(Observable<T> in, final Action0 thenAction, final Class errClass, final String errMsg) {
    in.subscribe(
      new Action1<T>() {
        public void call(T value) {
          System.out.println("error-next:"+value);
        }
      },
      new Action1<Throwable>() {
        public void call(Throwable t) {
          System.out.println("error-caught:"+t);
          assertEquals(errClass,t.getClass());
          if (errMsg!=null)
            assertEquals(errMsg,t.getMessage());  
          thenAction.call();
        }
      },
      new Action0() {
        public void call() {
          fail("unexpected-complete: failure expected");
        }
      });
  }

  /** Fire testComplete when counter has been completed */
  public static  void assertCompleted(final CountDownLatch counter) {
    new Thread() {
      public void run() {
        try {
          counter.await();
          testComplete();
        } catch (InterruptedException e) {
          fail("Unexpected interruption");
        }
      }
    }.start();
  }
}
