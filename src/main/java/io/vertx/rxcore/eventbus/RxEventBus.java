package io.vertx.rxcore.eventbus;

import io.vertx.rxcore.impl.VertxObservable;
import io.vertx.rxcore.impl.VertxSubscription;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
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

  public Observable<Message<String>> send(String address, String msg) {

    final VertxSubscription<Message<String>> sub = new VertxSubscription<>();

    Observable<Message<String>> obs = new VertxObservable<>(new Func1<Observer<Message<String>>, Subscription>() {
      @Override
      public Subscription call(Observer<Message<String>> replyObserver) {
        sub.setObserver(replyObserver);
        return sub;
      }
    });

    eventBus.send(address, msg, new Handler<Message<String>>() {
      @Override
      public void handle(Message<String> reply) {
        sub.handleResult(reply);
      }
    });

    return obs;
  }

  public Observable<Message<String>> registerHandler(final String address) {

    final VertxSubscription<Message<String>> sub = new VertxSubscription<>();

    Observable<Message<String>> obs = new VertxObservable<>(new Func1<Observer<Message<String>>, Subscription>() {
      @Override
      public Subscription call(Observer<Message<String>> replyObserver) {
        sub.setObserver(replyObserver);
        return sub;
      }
    });

    final Handler<Message<String>> handler = new Handler<Message<String>>() {
      @Override
      public void handle(Message<String> reply) {
        sub.handleResult(reply);
      }
    };

    eventBus.registerHandler(address, handler);

    sub.setOnUnsubscribe(new Runnable() {
      public void run() {
        eventBus.unregisterHandler(address, handler);
      }
    });

    return obs;
  }


}
