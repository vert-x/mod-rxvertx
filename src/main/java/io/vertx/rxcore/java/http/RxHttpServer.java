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
    AsyncResultMemoizeHandler<Void,Void> rh=new AsyncResultMemoizeHandler<Void,Void>();
    core.close(rh);
    return Observable.create(rh.subscribe);
  }

  // Rx extensions
  
  public Observable<RxHttpServerRequest> http() {
    return Observable.create(
      new SingleSubscriptionHandler<RxHttpServerRequest, HttpServerRequest>() {
          @Override public void execute() {
            core.requestHandler(this);
          }
          @Override public void onUnsubscribed() {
            core.requestHandler(null);
          }
          @Override public RxHttpServerRequest wrap(HttpServerRequest r) {
            return new RxHttpServerRequest(r);
          }
        }
    );
  }

  public Observable<RxServerWebSocket> websocket() {
    return Observable.create(
      new SingleSubscriptionHandler<RxServerWebSocket, ServerWebSocket>() {
          @Override public void execute() {
            core.websocketHandler(this);
          }
          @Override public void onUnsubscribed() {
            core.websocketHandler(null);
          }
          @Override public RxServerWebSocket wrap(ServerWebSocket s) {
            return new RxServerWebSocket(s);
          }
        }
    );
  }
}
