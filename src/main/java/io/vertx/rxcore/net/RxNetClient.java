package io.vertx.rxcore.net;

import io.vertx.rxcore.impl.VertxObservable;
import io.vertx.rxcore.impl.VertxSubscription;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.net.NetClient;
import org.vertx.java.core.net.NetSocket;
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
public class RxNetClient {
  private final NetClient netClient;


  public RxNetClient(NetClient netClient) {
    this.netClient = netClient;
  }

  public NetClient coreClient() {
    return netClient;
  }

  public Observable<RxNetSocket> connect(int port) {
    return connect(port, "localhost");
  }

  public Observable<RxNetSocket> connect(int port, String host) {
    final VertxSubscription<RxNetSocket> sub = new VertxSubscription<>();

    Observable<RxNetSocket> connectObservable = new VertxObservable<>(new Func1<Observer<RxNetSocket>, Subscription>() {
      @Override
      public Subscription call(Observer<RxNetSocket> replyObserver) {
        sub.setObserver(replyObserver);
        return sub;
      }
    });

    netClient.connect(port, host, new AsyncResultHandler<NetSocket>() {
      @Override
      public void handle(AsyncResult<NetSocket> asyncResult) {
        if (asyncResult.succeeded()) {
          sub.handleResult(new RxNetSocket(asyncResult.result()));
        } else {
          sub.failed(asyncResult.cause());
        }
        sub.complete();
      }
    });

    return connectObservable;
  }
}
