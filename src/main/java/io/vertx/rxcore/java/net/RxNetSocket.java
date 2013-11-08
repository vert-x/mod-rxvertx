package io.vertx.rxcore.java.net;

import io.vertx.rxcore.java.impl.VertxObservable;
import io.vertx.rxcore.java.impl.VertxSubscription;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
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
public class RxNetSocket {
  private final NetSocket netSocket;
  private Observable<Buffer> dataStream;

  RxNetSocket(NetSocket netSocket) {
    this.netSocket = netSocket;
  }

  public NetSocket coreSocket() {
    return netSocket;
  }

  public Observable<Buffer> dataStream() {
    if (dataStream == null) {
      final VertxSubscription<Buffer> sub = new VertxSubscription<>();

      dataStream = new VertxObservable<>(new Observable.OnSubscribeFunc<Buffer>() {
        @Override
        public Subscription onSubscribe(Observer<? super Buffer> replyObserver) {
          sub.setObserver(replyObserver);
          return sub;
        }
      });

      netSocket.dataHandler(new Handler<Buffer>() {
        @Override
        public void handle(Buffer buff) {
          sub.handleResult(buff);
        }
      });
      netSocket.endHandler(new Handler<Void>() {
        @Override
        public void handle(Void event) {
          sub.complete();
        }
      });

      sub.setOnUnsubscribe(new Runnable() {
        public void run() {
          netSocket.dataHandler(null);
          netSocket.endHandler(null);
          dataStream = null;
        }
      });
    }
    return dataStream;
  }
}
