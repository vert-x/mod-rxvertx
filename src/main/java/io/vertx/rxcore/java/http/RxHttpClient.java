package io.vertx.rxcore.java.http;

import io.vertx.rxcore.RxSupport;
import io.vertx.rxcore.java.impl.MemoizeHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.http.*;
import rx.Observable;
import rx.functions.Action1;

/** Rx wrapper for HttpClient 
 * @author <a href="http://github.com/petermd">Peter McDonnell</a>
 */
public class RxHttpClient {
  
  /** Nested */
  private final HttpClient core;
  
  /** Create new RxHttpClient */
  public RxHttpClient(HttpClient nested) {
    this.core=nested;
  }
  
  /** Return core */
  public HttpClient coreHttpClient() {
    return this.core;
  }

  /** Convenience wrapper */
  public void close() {
    this.core.close();
  }
  
  // Rx extensions
  
  public Observable<RxWebSocket> connectWebsocket(String uri) {
    return connectWebsocket(uri,WebSocketVersion.RFC6455);
  }

  public Observable<RxWebSocket> connectWebsocket(String uri, WebSocketVersion wsVersion) {
    return connectWebsocket(uri,wsVersion,null);
  }

  public Observable<RxWebSocket> connectWebsocket(String uri, WebSocketVersion wsVersion, MultiMap headers) {
    final MemoizeHandler<RxWebSocket,WebSocket> rh=new MemoizeHandler<RxWebSocket,WebSocket>() {
      @Override
      public void handle(WebSocket s) {
        complete(new RxWebSocket(s));
      }
    };
    core.connectWebsocket(uri,wsVersion,headers,rh);
    return Observable.create(rh.subscribe);
  }

  public Observable<RxHttpClientResponse> getNow(String uri) {
    return getNow(uri,null);
  }

  public Observable<RxHttpClientResponse> getNow(String uri, MultiMap headers) {
    final MemoizeHandler<RxHttpClientResponse,HttpClientResponse> rh=new MemoizeHandler<RxHttpClientResponse,HttpClientResponse>() {
      @Override
      public void handle(HttpClientResponse r) {
        complete(new RxHttpClientResponse(r));
      }
    };
    core.getNow(uri,headers,rh);
    return Observable.create(rh.subscribe);
  }

  public Observable<RxHttpClientResponse> options(String uri, Action1<HttpClientRequest> requestBuilder) {
    return request("OPTIONS",uri,requestBuilder);
  }

  public Observable<RxHttpClientResponse> get(String uri, Action1<HttpClientRequest> requestBuilder) {
    return request("GET",uri,requestBuilder);
  }

  public Observable<RxHttpClientResponse> post(String uri, Action1<HttpClientRequest> requestBuilder) {
    return request("POST",uri,requestBuilder);
  }

  public Observable<RxHttpClientResponse> put(String uri, Action1<HttpClientRequest> requestBuilder) {
    return request("PUT",uri,requestBuilder);
  }

  public Observable<RxHttpClientResponse> delete(String uri, Action1<HttpClientRequest> requestBuilder) {
    return request("DELETE",uri,requestBuilder);
  }

  public Observable<RxHttpClientResponse> trace(String uri, Action1<HttpClientRequest> requestBuilder) {
    return request("TRACE",uri,requestBuilder);
  }

  public Observable<RxHttpClientResponse> connect(String uri, Action1<HttpClientRequest> requestBuilder) {
    return request("CONNECT",uri,requestBuilder);
  }

  public Observable<RxHttpClientResponse> patch(String uri, Action1<HttpClientRequest> requestBuilder) {
    return request("PATCH",uri,requestBuilder);
  }

  public Observable<RxHttpClientResponse> request(String method, String uri, Action1<HttpClientRequest> requestBuilder) {
    
    final MemoizeHandler<RxHttpClientResponse,HttpClientResponse> rh=new MemoizeHandler<RxHttpClientResponse,HttpClientResponse>() {
      @Override
      public void handle(HttpClientResponse r) {
        complete(new RxHttpClientResponse(r));
      }
    };
    
    HttpClientRequest req=core.request(method,uri,rh);
      // if req fails, notify observers
      req.exceptionHandler(new Handler<Throwable>() {
          @Override
          public void handle(Throwable event) {
              rh.fail(event);
          }
      });
    
    // Use the builder to create the full request (or start upload)
    // We assume builder will call request.end()
    try {
      requestBuilder.call(req);
    }
    catch(Exception e) {
      // Request will never be sent so trigger error on the returned observable 
      rh.fail(e); 
    }
    
    return Observable.create(rh.subscribe);
  }
}
