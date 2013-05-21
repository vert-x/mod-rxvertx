package io.vertx.rxcore.java.http;

import io.vertx.rxcore.RxSupport;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.ServerWebSocket;
import rx.Observable;

/** Rx wrapper ServerWebSocket 
 * @author <a href="http://github.com/petermd">Peter McDonnell</a>
 */
public class RxServerWebSocket extends RxWebSocket<RxServerWebSocket>  {
  
  /** Nested */
  private final ServerWebSocket nested;

  /**Create new RxServerWebSocket */
  public RxServerWebSocket(ServerWebSocket nested) {
    super(nested);
    this.nested=nested;
  }
  
  /** Return as Observable<Buffer> */
  public Observable<Buffer> asObservable() {
    return RxSupport.toObservable(this.nested);
  }
  
  // ServerWebSocket implementation

  public String path() {
    return nested.path();
  }

  public MultiMap headers() {
    return nested.headers();
  }

  public ServerWebSocket reject() {
    return nested.reject();
  }
  
}
