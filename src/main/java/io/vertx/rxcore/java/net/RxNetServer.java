package io.vertx.rxcore.java.net;

import io.vertx.rxcore.java.impl.VertxObservable;
import io.vertx.rxcore.java.impl.VertxSubscription;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.net.NetServer;
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
public class RxNetServer {
  private final NetServer netServer;
  private Observable<RxNetSocket> connectStream;

  public RxNetServer(NetServer netServer) {
    this.netServer = netServer;
  }

  public NetServer coreServer() {
    return netServer;
  }

  public Observable<RxNetSocket> connectStream() {
    if (connectStream == null) {
      final VertxSubscription<RxNetSocket> sub = new VertxSubscription<>();

      connectStream = new VertxObservable<>(new Func1<Observer<RxNetSocket>, Subscription>() {
        @Override
        public Subscription call(Observer<RxNetSocket> replyObserver) {
          sub.setObserver(replyObserver);
          return sub;
        }
      });

      netServer.connectHandler(new Handler<NetSocket>() {
        @Override
        public void handle(NetSocket sock) {
          sub.handleResult(new RxNetSocket(sock));
        }
      });

      sub.setOnUnsubscribe(new Runnable() {
        public void run() {
          netServer.connectHandler(null);
          connectStream = null;
        }
      });

    }
    return connectStream;
  }

  public Observable<RxNetServer> listen(int port) {
    return listen(port, "0.0.0.0");
  }

  public Observable<RxNetServer> listen(int port, String host) {
    final VertxSubscription<RxNetServer> sub = new VertxSubscription<>();

    Observable<RxNetServer> obs = new VertxObservable<>(new Func1<Observer<RxNetServer>, Subscription>() {
      @Override
      public Subscription call(Observer<RxNetServer> replyObserver) {
        sub.setObserver(replyObserver);
        return sub;
      }
    });

    netServer.listen(port, host, new AsyncResultHandler<NetServer>() {
      @Override
      public void handle(AsyncResult<NetServer> asyncResult) {
        if (asyncResult.succeeded()) {
          sub.handleResult(RxNetServer.this);
        } else {
          sub.failed(asyncResult.cause());
        }
        sub.complete();
      }
    });

    return obs;
  }

  public Observable<Void> close() {
    final VertxSubscription<Void> sub = new VertxSubscription<>();

    Observable<Void> obs = new VertxObservable<>(new Func1<Observer<Void>, Subscription>() {
      @Override
      public Subscription call(Observer<Void> replyObserver) {
        sub.setObserver(replyObserver);
        return sub;
      }
    });

    netServer.close(new AsyncResultHandler<Void>() {
      @Override
      public void handle(AsyncResult<Void> asyncResult) {
        if (asyncResult.succeeded()) {
          sub.handleResult(null);
        } else {
          sub.failed(asyncResult.cause());
        }
        sub.complete();
      }
    });

    return obs;
  }
}
