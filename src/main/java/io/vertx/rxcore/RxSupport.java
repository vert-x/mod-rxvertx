package io.vertx.rxcore;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.atomic.AtomicLong;

import io.vertx.rxcore.java.impl.SubscriptionHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.streams.ReadStream;
import org.vertx.java.core.streams.WriteStream;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.functions.*;

/** RxSupport */
public class RxSupport {
  
  // Streams

  /** Accumulator that merges buffers */
  public static Func2<Buffer, Buffer, Buffer> mergeBuffers=new Func2<Buffer,Buffer,Buffer>() {
    public Buffer call(Buffer b1, Buffer b2) {
      b1.appendBuffer(b2);
      return b1;
    }
  };
    
  /** Stream Observable<Buffer> to WriteStream.
   *
   * <p>This method does not handle writeQueueFull condition</p>  
   * 
   **/
  public static Observable<Long> stream(Observable<Buffer> src, final WriteStream out) {
    final PublishSubject<Long> rx=PublishSubject.create();
    final AtomicLong total=new AtomicLong();
    src.subscribe(
      new Action1<Buffer>() {
        public void call(Buffer buffer) {
          out.write(buffer);
          total.addAndGet(buffer.length());
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

  /** Convert ReadStream to Observable */
  public static Observable<Buffer> toObservable(final ReadStream rs) {
    final SubscriptionHandler<Buffer, Buffer> rh=new SubscriptionHandler<Buffer, Buffer>() {
      @Override public void execute() {
        rs.dataHandler(this);
        rs.exceptionHandler(new Handler<Throwable>() {
          public void handle(Throwable t) {
            fireError(t);
          }
        });
        rs.endHandler(new Handler<Void>() {
          public void handle(Void v) {
            fireComplete();
          }
        });
      }
      @Override public void onUnsubscribed() {
        try {
          rs.dataHandler(null);
          rs.exceptionHandler(null);
          rs.endHandler(null);
        }
        catch(Exception e) {
          // Clearing handlers after stream closed causes issues for some (eg AsyncFile) so silently drop errors
        }
      }
    };
    
    return Observable.create(rh);
  }
  
  // JSON 
	
  /** Simple JSON encode */
  public static Func1<JsonObject, Buffer> encodeJson(final String charset) {
    return new Func1<JsonObject,Buffer>() {
      public Buffer call(JsonObject in) {
        try {
          return new Buffer(in.encode().getBytes(charset));
        }
        catch (UnsupportedEncodingException e) {
          throw new RuntimeException("Unable to encode JSON (charset="+charset+")",e);
        }
      }
    };
  }
  
  /** Simple JSON decode */
  public static Func1<Buffer,JsonObject> decodeJson(final String charset) {
    return new Func1<Buffer,JsonObject>() {
      public JsonObject call(Buffer in) {
        try {
          return new JsonObject(in.toString(charset));
        }
        catch(Exception e) {
          throw new RuntimeException("Unable to decode json request (e="+e+")");
        }
      }
    };
  }

  // EventBus
  
  /** Message */
  public static Func1<Message,Object> unwrapMessage=new Func1<Message,Object>() {
    public Object call(Message msg) {
      return msg.body();
    }
  };
}
