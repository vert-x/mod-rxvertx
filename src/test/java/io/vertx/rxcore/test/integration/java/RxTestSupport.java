package io.vertx.rxcore.test.integration.java;

import rx.plugins.RxJavaErrorHandler;
import rx.plugins.RxJavaPlugins;
import rx.util.functions.Action0;
import rx.util.functions.Action1;

/** Utility functions/actions 
 * @author <a href="http://github.com/petermd">Peter McDonnell</a>
 **/
public class RxTestSupport {
  
  static {
    try {
      RxJavaPlugins.getInstance().registerErrorHandler(new RxJavaErrorHandler() {
        @Override 
        public void handleError(Throwable e) {
          System.err.println("Internal RxJava error (e="+e+")");
          e.printStackTrace(System.err);
        }
      });
      
      System.out.println("INFO Registered rxjava plugin error handler");
    }
    catch(Throwable t) {
      System.err.println("FATAL Unable to register rxjava plugin error handler (t="+t+")");
      t.printStackTrace(System.err);
    }
  }
  
  public static Action1 traceNext=new Action1() {
    public void call(Object o) {
      System.out.println("onNext:"+o);
    }
  };
  
  public static Action1<Exception> traceError=new Action1<Exception>() {
    public void call(Exception e) {
      System.err.println("onError:"+e);
      e.printStackTrace(System.err);
    }
  };

  public static Action0 traceComplete=new Action0() {
    public void call() {
      System.out.println("onComplete");
    }
  };

}
