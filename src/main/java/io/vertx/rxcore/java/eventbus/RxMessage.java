package io.vertx.rxcore.java.eventbus;

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
public class RxMessage<T> {

  /** Core Message */
  private final Message<T> coreMessage;

  /** Wrap Message with RxMessage */
  RxMessage(Message<T> coreMessage) {
    this.coreMessage = coreMessage;
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
  public <R,T> Observable<RxMessage<T>> observeReply(final R msg) {
    return Observable.create(new RxEventBus.SendHandler<T>() {
      @Override public void execute() {
        coreMessage.reply(msg,this);
      }
    });
  }

  /** Observe a reply with timeout */
  public <R,T> Observable<RxMessage<T>> observerReplyWithTimeout(final R msg, final long timeout) {    
    return Observable.create(new RxEventBus.AsyncSendHandler<T>() {
      @Override public void execute() {
        coreMessage.replyWithTimeout(msg,timeout,this);
      }
    });
  }
}
