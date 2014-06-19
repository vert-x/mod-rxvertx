package io.vertx.rxcore.java.http;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicLong;

import io.vertx.rxcore.RxSupport;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.WebSocket;
import org.vertx.java.core.http.WebSocketBase;
import org.vertx.java.core.http.WebSocketFrame;

import rx.Observable;
import rx.subjects.ReplaySubject;
import rx.functions.Action0;
import rx.functions.Action1;

/** Rx wrapper for WebSocket 
 * @author <a href="http://github.com/petermd">Peter McDonnell</a>
 **/
public class RxWebSocket<T extends WebSocket> implements WebSocket {

  /** Nested */
  private final WebSocketBase nested;
  
  public RxWebSocket(WebSocketBase nested) {
    this.nested=nested;
  }
  
  // Rx extensions 
  
  /** Write Observable<String> as text frames */
  public Observable<Long> writeAsTextFrame(Observable<String> src) {
    final ReplaySubject<Long> rx=ReplaySubject.create();
    final AtomicLong total=new AtomicLong();
    src.subscribe(
      new Action1<String>() {
        public void call(String s) {
          writeTextFrame(s);
          total.addAndGet(1);
        }
      },
      new Action1<Throwable>() {
        public void call(Throwable t) {
          rx.onError(t);
        }
      },
      new Action0() {
        public void call() {
          rx.onNext(total.get());
          rx.onCompleted();
        }
      }
    );
    return rx;
  }
  
  /** Return as Observable<Buffer> */
  public Observable<Buffer> asObservable() {
    return RxSupport.toObservable(nested);
  }

  // WebSocketBase implementation

  public String binaryHandlerID() {
    return nested.binaryHandlerID();
  }

  public String textHandlerID() {
    return nested.textHandlerID();
  }

  public T writeBinaryFrame(Buffer data) {
    nested.writeBinaryFrame(data);
    return (T)this;
  }

  public T writeTextFrame(String str) {
    nested.writeTextFrame(str);
    return (T)this;
  }

  public T closeHandler(Handler<Void> handler) {
    nested.closeHandler(handler);
    return (T)this;
  }

  public WebSocket frameHandler(Handler<WebSocketFrame> handler) {
    nested.frameHandler(handler);
    return (T)this;
  }

  public void close() {
    nested.close();
  }

  public InetSocketAddress remoteAddress() {
    return nested.remoteAddress();
  }

  public InetSocketAddress localAddress() {
    return nested.localAddress();
  }

  public T dataHandler(Handler<Buffer> handler) {
    throw new UnsupportedOperationException("Cannot access via Rx - use asObservable()");
  }

  public T pause() {
    nested.pause();
    return (T)this;
  }

  public T resume() {
    nested.resume();
    return (T)this;
  }

  public T endHandler(Handler<Void> endHandler) {
    throw new UnsupportedOperationException("Cannot access via Rx - use asObservable()");
  }
  
  // WriteStream implementation

  public T write(Buffer data) {
    nested.write(data);
    return (T)this;
  }

  public T setWriteQueueMaxSize(int maxSize) {
    nested.setWriteQueueMaxSize(maxSize);
    return (T)this;  
  }

  public boolean writeQueueFull() {
    return nested.writeQueueFull();
  }

  public T drainHandler(Handler<Void> handler) {
    nested.drainHandler(handler);
    return (T)this;
  }

  public T exceptionHandler(Handler<Throwable> handler) {
    nested.exceptionHandler(handler);
    return (T)this;
  }
}
