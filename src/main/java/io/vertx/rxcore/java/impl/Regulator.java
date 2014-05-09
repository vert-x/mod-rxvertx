package io.vertx.rxcore.java.impl;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.streams.WriteStream;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.subjects.PublishSubject;

import java.util.ArrayDeque;
import java.util.concurrent.atomic.AtomicLong;

/** Regulator */
public class Regulator<R> implements Observable.Operator<R,R> {

  // Definitions

  /** Throttle */
  public abstract class Throttle<T> implements Handler<Boolean> {

    // Queue

    /** Queue */
    protected ArrayDeque<T> queue;

    /** Handler */
    protected Handler<Void> completeHandler;

    /** Restricted */
    protected boolean restricted;

    // Public methods

    /** Create new Throttle */
    public Throttle() {
      this.queue=new ArrayDeque<>();
    }

    /** Send */
    public void send(T value) {
      if (restricted)
        this.queue.addLast(value);
      else
        forward(value);
    }

    // Handler implementation

    /** Restriction */
    public void handle(Boolean restrict) {
      if (restrict) {
        this.restricted=true;
      }
      else {
        this.restricted=!flush();
      }
    }

    // Implementation

    /** Clear the queue */
    protected void clear() {
      this.queue.clear();
    }

    /** Flush the queue
     *
     * @returns true if the queue was flushed
     *
     **/
    protected boolean flush() {
      while (!this.queue.isEmpty()) {
        // Try and forward
        if (!forward(this.queue.peekFirst()))
          return false;
        // Remove the item
        this.queue.pollFirst();
      }

      if (this.completeHandler!=null) {
        this.completeHandler.handle(null);
      }

      return true;
    }

    /** Complete */
    protected void complete(Handler<Void> handler) {
      if (this.queue.isEmpty()) {
        handler.handle(null);
      }
      else {
        this.completeHandler=handler;
      }
    }

    /** Send to the target */
    protected abstract boolean forward(T value);
  }

  /** Gate */
  public class Gate<R> extends Throttle<R> implements Observer<R> {

    // Instance variables

    /** Subscriber */
    protected Subscriber<R> target;

    /** Restricted */
    protected Boolean restricted;

    // Public methods

    public Gate(Subscriber<R> subscriber) {
      this.target=subscriber;
      this.restricted=false;
    }

    // Restrict Handler

    /** Handler notification */
    public void handle(Boolean restrict) {
      this.restricted=restrict;
    }

    // Observer

    /** Next message */
    public void onNext(R r) {
      send(r);
    }

    public void onCompleted() {
      complete(new Handler<Void>() {
        public void handle(Void v) {
          target.onCompleted();
        }
      });
    }

    /** Source failure */
    public void onError(Throwable e) {
      clear();
      target.onError(e);
    }

    // Implementation

    /** Forward */
    protected boolean forward(R value) {
      target.onNext(value);
      return true;
    }
  }

  /** Buffered Stream
   *
   * Wrapper of WriteStream that buffers when the target WriteStream is full. Used to
   * detect the buffer filling up to start inhibiting the Observable
   *
   **/
  public class BufferedWriteStream extends Throttle<Buffer> implements WriteStream<BufferedWriteStream> {

    // Instance variables

    /** Restrict handler */
    protected Handler<Boolean> restrictHandler;

    /** Exception handler */
    protected Handler<Throwable> exceptionHandler;

    /** Target */
    protected WriteStream<Buffer> out;

    // Public methods

    /** Create new BufferedWriteStream */
    public BufferedWriteStream(WriteStream<Buffer> out) {
      this.out=out;

      out.drainHandler(new Handler<Void>() {
        public void handle(Void v) {
          release();
        }
      });

      out.exceptionHandler(new Handler<Throwable>() {
        public void handle(Throwable err) {
          fail(err);
        }
      });
    }

    /** Set the restriction handler */
    public void restrictHandler(Handler<Boolean> handler) {
      this.restrictHandler=handler;
    }

    // WriteStream implementation

    /** Write buffer */
    public BufferedWriteStream write(Buffer buf) {

      send(buf);

      return this;
    }

    /** Set underlying WriteQueueMaxSize */
    public BufferedWriteStream setWriteQueueMaxSize(int maxSize) {
      this.out.setWriteQueueMaxSize(maxSize);
      return this;
    }

    /** Return true if write queue is full (never true..) */
    public boolean writeQueueFull() {
      return false;
    }

    /** Provide a drain handler (not supported) */
    public BufferedWriteStream drainHandler(Handler<Void> handler) {
      throw new RuntimeException("drainHandler is not supported");
    }

    /** Register an exception handler */
    public BufferedWriteStream exceptionHandler(Handler<Throwable> handler) {
      this.exceptionHandler=handler;
      return this;
    }

    // Implementation

    /** Restrict */
    protected void restrict(Boolean restricted) {
      if (this.restrictHandler!=null) {
        this.restrictHandler.handle(restricted);
      }
    }

    /** Failure */
    protected void fail(Throwable cause) {
      clear();
      if (this.exceptionHandler!=null) {
        this.exceptionHandler.handle(cause);
      }
    }

    /** Release */
    protected void release() {
      int count=0;
      // Release the buffered queue
      while(!this.out.writeQueueFull() && !this.queue.isEmpty()) {
        this.out.write(this.queue.pollFirst());
        count++;
      }
      restrict(false);

      if (this.completeHandler!=null) {
        this.completeHandler.handle(null);
      }
    }

    /** Clear */
    protected void clear() {
      this.queue.clear();
    }

    // Throttle implementation

    /** Forward value */
    protected boolean forward(Buffer value) {
      if (this.out.writeQueueFull())
        return false;

      out.write(value);
      return true;
    }
  }

  // Instance variables

  /** Gate */
  protected Gate<R> gate;

  // Public methods

  /** Create new Regulator */
  public Regulator() {
  }

  // Operator implementation

  /** Add Subscriber */
  public Subscriber<? super R> call(Subscriber<? super R> subscriber) {

    if (this.gate!=null)
      throw new IllegalStateException("Cannot have multiple subscriptions (use publish)");

    this.gate=new Gate(subscriber);

    return new Subscriber<R>() {
      public void onCompleted() {
        gate.onCompleted();
      }
      public void onError(Throwable e) {
        gate.onError(e);
      }
      public void onNext(R r) {
        gate.onNext(r);
      }
    };
  }

  /** Stream Observable to a target stream */
  public Observable<Long> stream(Observable<Buffer> src, WriteStream<Buffer> out) {

    final PublishSubject<Long> rx=PublishSubject.create();
    final AtomicLong total=new AtomicLong();

    final BufferedWriteStream target=new BufferedWriteStream(out);

    // Trap write errors
    target.exceptionHandler(new Handler<Throwable>() {
      public void handle(Throwable t) {
        rx.onError(t);
      }
    });

    src.subscribe(
      new Action1<Buffer>() {
        public void call(Buffer buffer) {
          // Ensure the gate is mapped
          target.restrictHandler(gate);
          // Write to buffer
          target.write(buffer);
          total.addAndGet(buffer.length());
          // Output the total each buffer
          rx.onNext(total.get());
        }
      },
      new Action1<Throwable>() {
        public void call(Throwable t) {
          target.clear();
          rx.onError(t);
        }
      },
      new Action0() {
        public void call() {
          target.complete(new Handler<Void>() {
            public void handle(Void event) {
              rx.onCompleted();
            }
          });
        }
      }
    );

    return rx;
  }
}
