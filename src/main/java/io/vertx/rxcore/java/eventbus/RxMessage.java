package io.vertx.rxcore.java.eventbus;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import rx.Observable;

/*
 * Copyright 2013 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public abstract class RxMessage<T> {

  /** Core Message */
  protected final Message<T> coreMessage;

  /** Wrap Message with RxMessage */
  RxMessage(Message<T> coreMessage) {
    this.coreMessage = coreMessage;
  }

  /** Return string representation */
  public String toString() {
    return "RxMessage["+body()+"]";
  }

  /**
   * The body of the message
   */
  public T body() {
    return coreMessage.body();
  }

  /**
   * The reply address (if any)
   */
  public String replyAddress() {
    return coreMessage.replyAddress();
  }

  /**
   * @return The underlying core message
   */
  public Message<T> coreMessage() {
    return coreMessage;
  }

  /** Send empty reply */
  public void reply() {
    coreMessage.reply();
  }

  /** Send reply without expecting a response */
  public <R> void reply(final R msg) {
    coreMessage.reply(msg);
  }

  /** Observe a reply */
  public abstract <R,T> Observable<RxMessage<T>> observeReply(final R msg);

  /** Observe a reply with timeout */
  public abstract <R,T> Observable<RxMessage<T>> observeReplyWithTimeout(final R msg, final long timeout);

  /** Send a signal that processing of this message failed */
  public void fail(int failureCode, String message) {
    coreMessage.fail(failureCode, message);
  }
}
