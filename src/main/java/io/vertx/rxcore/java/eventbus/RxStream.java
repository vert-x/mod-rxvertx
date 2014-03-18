package io.vertx.rxcore.java.eventbus;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;

/** Represents a stream of EventBus messages accessed via reply() */
public class RxStream<S,R> implements Handler<Message<R>> {

  /** Current message */
  private Message<R> cur;

  /** Handler */
  public Handler<Message<R>> callback;

  /** Send next part of stream */
  public void next(S value) {
    cur.reply(value,this.callback);
  }

  /** Finish */
  public void complete() {
  }

  /** Return the current value */
  public R value() {
    return cur.body();
  }

  // Handler implementation

  /** Handle */
  public void handle(Message<R> msg) {
    // Keep the message
    this.cur=msg;
  }
}
