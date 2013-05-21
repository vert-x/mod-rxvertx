package io.vertx.rxcore.java.http;

import io.vertx.rxcore.java.impl.*;
import org.vertx.java.core.http.*;
import rx.Observable;

/** Rx wrapper for HttpServer
 * @author <a href="http://github.com/petermd">Peter McDonnell</a>
 */
public class RxHttpServer {
  
  private final HttpServer core;
  
  public RxHttpServer(HttpServer core) {
    this.core=core;
  }

  public HttpServer coreHttpServer() {
    return this.core;
  }
  
  /** Convenience wrapper */
  public void closeNow() {
    this.core.close();
  }

  /** Convenience wrapper */
  public Observable<Void> close() {
    AsyncResultMemoizeHandler<Void> rh=new AsyncResultMemoizeHandler<Void>();
    core.close(rh);
    return Observable.create(rh.subscribe);
  }

  // Rx extensions
  
  public Observable<RxHttpServerRequest> http() {
    return Observable.create(
      new SingleObserverHandler<RxHttpServerRequest, HttpServerRequest>() {
          public void register() {
            core.requestHandler(this);
          }
          public void clear() {
            core.requestHandler(null);
          }
          public RxHttpServerRequest wrap(HttpServerRequest r) {
            return new RxHttpServerRequest(r);
          }
        }.subscribe      
    );
  }

  public Observable<RxServerWebSocket> websocket() {
    return Observable.create(
      new SingleObserverHandler<RxServerWebSocket, ServerWebSocket>() {
          public void register() {
            core.websocketHandler(this);
          }
          public void clear() {
            core.websocketHandler(null);
          }
          public RxServerWebSocket wrap(ServerWebSocket s) {
            return new RxServerWebSocket(s);
          }
        }.subscribe      
    );
  }
}
