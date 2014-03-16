package io.vertx.rxcore.java.http;

import java.net.InetSocketAddress;
import java.net.URI;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.security.cert.X509Certificate;

import io.vertx.rxcore.RxSupport;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.*;
import org.vertx.java.core.net.NetSocket;
import rx.Observable;

/** Rx wrapper for HttpServerRequest 
 * 
 * <p>Replace *Handler methods with asObservable*</p>
 *  
 * @author <a href="http://github.com/petermd">Peter McDonnell</a>
 */
public class RxHttpServerRequest implements HttpServerRequest {
  
  /** Real instance */
  private final HttpServerRequest nested;

  /** Create new RxHttpServerRequest */
  protected RxHttpServerRequest(HttpServerRequest nested) {
    this.nested=nested;
  }

  /** Return observable for accessing the response as a stream of Buffer */
  public Observable<Buffer> asObservable() {
    return RxSupport.toObservable(nested);
  }
  
  // HttpServerRequest implementation
  
  public HttpVersion version() {
    return nested.version();
  }

  public String method() {
    return nested.method();
  }

  public String uri() {
    return nested.uri();
  }

  public String path() {
    return nested.path();
  }

  public String query() {
    return nested.query();
  }

  public HttpServerResponse response() {
    return nested.response();
  }

  public MultiMap headers() {
    return nested.headers();
  }

  public MultiMap params() {
    return nested.params();
  }

  public InetSocketAddress remoteAddress() {
    return nested.remoteAddress();
  }

  public InetSocketAddress localAddress() {
    return nested.localAddress();
  }

  public X509Certificate[] peerCertificateChain() throws SSLPeerUnverifiedException {
    return nested.peerCertificateChain();
  }

  public URI absoluteURI() {
    return nested.absoluteURI();
  }

  public HttpServerRequest bodyHandler(Handler<Buffer> bodyHandler) {
    throw new UnsupportedOperationException("Cannot access via Rx - use asObservable()");
  }

  public NetSocket netSocket() {
    // TODO: Provider wrapper
    return nested.netSocket();
  }

  public HttpServerRequest expectMultiPart(boolean expect) {
    return nested.expectMultiPart(expect);
  }

  public HttpServerRequest uploadHandler(Handler<HttpServerFileUpload> uploadHandler) {
    // TODO: Provide upload(Observable<Buffer>)?
    return nested.uploadHandler(uploadHandler);
  }

  public MultiMap formAttributes() {
    return nested.formAttributes();
  }

  public HttpServerRequest dataHandler(Handler<Buffer> handler) {
    throw new UnsupportedOperationException("Cannot access via Rx - use asObservable()");
  }

  public HttpServerRequest pause() {
    return nested.pause();
  }

  public HttpServerRequest resume() {
    return nested.resume();
  }

  public HttpServerRequest endHandler(Handler<Void> endHandler) {
    throw new UnsupportedOperationException("Cannot access via Rx - use asObservable()");
  }

  public HttpServerRequest exceptionHandler(Handler<Throwable> handler) {
    throw new UnsupportedOperationException("Cannot access via Rx - use asObservable()");
  }
}
