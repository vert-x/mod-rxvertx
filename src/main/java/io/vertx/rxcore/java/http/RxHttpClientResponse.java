package io.vertx.rxcore.java.http;

import java.util.List;

import io.vertx.rxcore.RxSupport;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClientResponse;
import rx.Observable;

/** Rx wrapper for HttpClientResponse 
 * @author <a href="http://github.com/petermd">Peter McDonnell</a>
 */
public class RxHttpClientResponse implements HttpClientResponse {
  
  private final HttpClientResponse nested;
  
  public RxHttpClientResponse(HttpClientResponse nested) {
    this.nested=nested;
  }
  
  // Rx extensions 
  
  /** Return as Observable<Buffer> */
  public Observable<Buffer> asObservable() {
    return RxSupport.toObservable(nested);
  }
  
  // HttpClientResponse implementation
  
  public int statusCode() {
    return nested.statusCode();
  }

  public String statusMessage() {
    return nested.statusMessage();
  }

  public MultiMap headers() {
    return nested.headers();
  }

  public MultiMap trailers() {
    return nested.trailers();
  }

  public List<String> cookies() {
    return nested.cookies();
  }

  // ReadStream implementation

  public HttpClientResponse bodyHandler(Handler<Buffer> bodyHandler) {
    throw new UnsupportedOperationException("Cannot access via Rx - use asObservable()");
  }

  public HttpClientResponse dataHandler(Handler<Buffer> handler) {
    throw new UnsupportedOperationException("Cannot access via Rx - use asObservable()");
  }

  public HttpClientResponse pause() {
    return nested.pause();
  }

  public HttpClientResponse resume() {
    return nested.resume();
  }

  public HttpClientResponse endHandler(Handler<Void> endHandler) {
    throw new UnsupportedOperationException("Cannot access via Rx - use asObservable()");
  }

  public HttpClientResponse exceptionHandler(Handler<Throwable> handler) {
    throw new UnsupportedOperationException("Cannot access via Rx - use asObservable()");
  }
}
