package io.vertx.rxcore.eventbus;

import io.vertx.rxcore.impl.VertxObservable;
import io.vertx.rxcore.impl.VertxSubscription;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.util.functions.Func1;

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

  private final Message<T> coreMessage;

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

  public <T> Observable<RxMessage<T>> reply(Object message) {
    final VertxSubscription<RxMessage<T>> sub = new VertxSubscription<>();

    Observable<RxMessage<T>> obs = new VertxObservable<>(new Func1<Observer<RxMessage<T>>, Subscription>() {
      @Override
      public Subscription call(Observer<RxMessage<T>> replyObserver) {
        sub.setObserver(replyObserver);
        return sub;
      }
    });

    coreMessage.reply(message, new Handler<Message<T>>() {
      @Override
      public void handle(Message<T> reply) {
        sub.handleResult(new RxMessage<>(reply));
        sub.complete();
      }
    });

    return obs;
  }

  public <T> Observable<RxMessage<T>> reply() {
    return reply((String)null);
  }

  public <T> Observable<RxMessage<T>> reply(JsonObject message) {
    return reply((Object)message);
  }

  public <T> Observable<RxMessage<T>> reply(JsonArray message) {
    return reply((Object)message);
  }

  public <T> Observable<RxMessage<T>> reply(String message) {
    return reply((Object)message);
  }

  public <T> Observable<RxMessage<T>> reply(Buffer message) {
    return reply((Object)message);
  }

  public <T> Observable<RxMessage<T>> reply(byte[] message) {
    return reply((Object)message);
  }

  public <T> Observable<RxMessage<T>> reply(Integer message) {
    return reply((Object)message);
  }

  public <T> Observable<RxMessage<T>> reply(Long message) {
    return reply((Object)message);
  }

  public <T> Observable<RxMessage<T>> reply(Short message) {
    return reply((Object)message);
  }

  public <T> Observable<RxMessage<T>> reply(Character message) {
    return reply((Object)message);
  }

  public <T> Observable<RxMessage<T>> reply(Boolean message) {
    return reply((Object)message);
  }

  public <T> Observable<RxMessage<T>> reply(Float message) {
    return reply((Object)message);
  }

  public <T> Observable<RxMessage<T>> reply(Double message) {
    return reply((Object)message);
  }

  /**
   * @return The underlying core message
   */
  public Message<T> coreMessage() {
    return coreMessage;
  }

}
