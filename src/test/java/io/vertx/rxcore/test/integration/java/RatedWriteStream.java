package io.vertx.rxcore.test.integration.java;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.streams.WriteStream;

/** WriteStream that enforces a fix rate */
public class RatedWriteStream implements WriteStream<RatedWriteStream> {

  // Definitions

  /** Rate Counter */
  public static class RateCounter {

    /** Current second */
    private long second;

    /** Count */
    private long count;

    /** Create new RateCounter */
    public RateCounter() {
      this.second=System.currentTimeMillis()/1000;
      this.count=0;
    }

    /** Return time remaining in current second */
    public int remaining() {
      long now=System.currentTimeMillis();
      return (this.second==(now/1000))?(int)(1000-(now%1000)):1000;
    }

    /** Add */
    public void add(int inc) {
      long now=System.currentTimeMillis()/1000;
      if (this.second==now) {
        count+=inc;
        return;
      }
      this.second=now;
      count=inc;
    }
  }

  /** Dummy Handler */
  public static Handler traceHandler(final String id) {
    return new Handler() {
      public void handle(Object evt) {
        System.out.println("trace["+id+"]="+evt);
      }
    };
  }

  // Instance variables

  /** VertX */
  private final Vertx vertx;

  /** Writes Per Second */
  protected int wps;

  /** Counter */
  protected RateCounter counter;

  /** Handlers */
  protected Handler drainHandler;
  protected Handler<Throwable> exceptionHandler;

  // Public methods

  /** Create new RatedWriteStream */
  public RatedWriteStream(Vertx vertx, int wps) {
    this.vertx=vertx;
    this.wps=wps;
    this.counter=new RateCounter();
    this.drainHandler=traceHandler("drain");
    this.exceptionHandler=traceHandler("error");
  }

  // WriteStream implementation

  /** Write Buffer */
  public RatedWriteStream write(Buffer data) {
    this.counter.add(1);
    // Check if rate exceeded
    if (this.counter.count>this.wps) {
      exceptionHandler.handle(new RuntimeException("Rate exceeded " + counter.count + "/" + wps));
    }
    // If limit reached, automatically set a timer
    else if (this.counter.count==this.wps) {
      this.vertx.setTimer(this.counter.remaining(),new Handler<Long>() { public void handle(Long value) { drainHandler.handle(null); } });
    }
    return this;
  }

  /** Set the WriteQueueMaxSize */
  public RatedWriteStream setWriteQueueMaxSize(int maxSize) {
    // Ignored
    return this;
  }

  /** Return true if the queue is full */
  public boolean writeQueueFull() {
    return this.counter.count==this.wps;
  }

  /** Set a handler for drainage */
  public RatedWriteStream drainHandler(Handler handler) {
    this.drainHandler=handler;
    return this;
  }

  /** Exception handler */
  public RatedWriteStream exceptionHandler(Handler handler) {
    this.exceptionHandler=handler;
    return this;
  }
}
