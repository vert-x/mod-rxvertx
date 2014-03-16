package io.vertx.rxcore.java.impl;

import io.vertx.rxcore.RxSupport;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import rx.Observable;

/** Handler for AsyncResult 
 * @author <a href="http://github.com/petermd">Peter McDonnell</a>
 **/
public class AsyncResultMemoizeHandler<R,T> extends MemoizeHandler<R, AsyncResult<T>> implements AsyncResultHandler<T> {
  
  /** Convenience */
  public static <T> Observable<T> create() {
    return Observable.create(new AsyncResultMemoizeHandler<T,T>().subscribe);
  }

  /** Override to wrap value */
  @SuppressWarnings("unchecked")
  public R wrap(T value) {
    return (R)value;
  }

  // Handler implementation
  
  @Override 
  public void handle(AsyncResult<T> value) {
    if (value.succeeded()) 
      complete(wrap(value.result()));
    else
      fail(value.cause());
  }
}
