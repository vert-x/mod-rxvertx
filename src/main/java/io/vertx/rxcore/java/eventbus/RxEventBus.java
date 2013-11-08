package io.vertx.rxcore.java.eventbus;

import io.vertx.rxcore.java.impl.VertxObservable;
import io.vertx.rxcore.java.impl.VertxSubscription;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
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
public class RxEventBus {

  private final EventBus eventBus;

  public RxEventBus(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  public <T> Observable<RxMessage<T>> send(String address, String msg) {
    return doSend(address, msg);
  }

  public <T> Observable<RxMessage<T>> send(String address, JsonObject msg) {
    return doSend(address, msg);
  }

  public <T> Observable<RxMessage<T>> send(String address, JsonArray msg) {
    return doSend(address, msg);
  }

  public <T> Observable<RxMessage<T>> send(String address, Buffer msg) {
    return doSend(address, msg);
  }

  public <T> Observable<RxMessage<T>> send(String address, byte[] msg) {
    return doSend(address, msg);
  }

  public <T> Observable<RxMessage<T>> send(String address, Integer msg) {
    return doSend(address, msg);
  }

  public <T> Observable<RxMessage<T>> send(String address, Long msg) {
    return doSend(address, msg);
  }

  public <T> Observable<RxMessage<T>> send(String address, Float msg) {
    return doSend(address, msg);
  }

  public <T> Observable<RxMessage<T>> send(String address, Double msg) {
    return doSend(address, msg);
  }

  public <T> Observable<RxMessage<T>> send(String address, Boolean msg) {
    return doSend(address, msg);
  }

  public <T> Observable<RxMessage<T>> send(String address, Short msg) {
    return doSend(address, msg);
  }

  public <T> Observable<RxMessage<T>> send(String address, Character msg) {
    return doSend(address, msg);
  }

  public <T> Observable<RxMessage<T>> send(String address, Byte msg) {
    return doSend(address, msg);
  }

  public <T> Observable<RxMessage<T>> registerLocalHandler(final String address) {
    return registerHandler(address, true);
  }

  public <T> Observable<RxMessage<T>> registerHandler(final String address) {
    return registerHandler(address, false);
  }

  private <T> Observable<RxMessage<T>> doSend(String address, Object msg) {
    final VertxSubscription<RxMessage<T>> sub = new VertxSubscription<>();

    Observable<RxMessage<T>> obs = new VertxObservable<>(new Observable.OnSubscribeFunc<RxMessage<T>>() {
      @Override
      public Subscription onSubscribe(Observer<? super RxMessage<T>> replyObserver) {
        sub.setObserver(replyObserver);
        return sub;
      }
    });

    eventBus.send(address, msg, new Handler<Message>() {
      @Override
      public void handle(Message reply) {
        sub.handleResult(new RxMessage<T>(reply));
        sub.complete();
      }
    });

    return obs;
  }

  private <T> Observable<RxMessage<T>> registerHandler(final String address, boolean local) {

    final VertxSubscription<RxMessage<T>> sub = new VertxSubscription<>();

    Observable<RxMessage<T>> obs = new VertxObservable<>(new Observable.OnSubscribeFunc<RxMessage<T>>() {
      @Override
      public Subscription onSubscribe(Observer<? super RxMessage<T>> replyObserver) {
        sub.setObserver(replyObserver);
        return sub;
      }
    });

    final Handler<Message<T>> handler = new Handler<Message<T>>() {
      @Override
      public void handle(Message<T> reply) {
        sub.handleResult(new RxMessage<T>(reply));
      }
    };

    if (local) {
      eventBus.registerLocalHandler(address, handler);
    } else {
      eventBus.registerHandler(address, handler);
    }

    sub.setOnUnsubscribe(new Runnable() {
      public void run() {
        eventBus.unregisterHandler(address, handler);
      }
    });

    return obs;
  }

}
