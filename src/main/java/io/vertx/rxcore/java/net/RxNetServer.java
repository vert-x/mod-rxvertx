package io.vertx.rxcore.java.net;

import io.vertx.rxcore.java.impl.AsyncResultMemoizeHandler;
import io.vertx.rxcore.java.impl.SingleSubscriptionHandler;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.net.NetServer;
import org.vertx.java.core.net.NetSocket;
import rx.*;

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

    if (connectStream!=null)
      return connectStream;

    connectStream=Observable.create(
      new SingleSubscriptionHandler<RxNetSocket, NetSocket>() {
        @Override public void execute() {
          netServer.connectHandler(this);
        }
        @Override public void onUnsubscribed() {
          netServer.connectHandler(null);
          connectStream=null;
        }
        @Override public RxNetSocket wrap(NetSocket r) {
          return new RxNetSocket(r);
        }
      }
    );

    return connectStream;
  }

  public Observable<RxNetServer> listen(int port) {
    return listen(port, "0.0.0.0");
  }

  public Observable<RxNetServer> listen(final int port, final String host) {
    final RxNetServer server=this;
    return Observable.create(
      new SingleSubscriptionHandler<RxNetServer, AsyncResult<NetServer>>() {
        @Override
        public void execute() {
          netServer.listen(port,host,this);
        }
        @Override
        public void handle(AsyncResult<NetServer> value) {
          if (value.succeeded())
            fireResult(server);
          else
            fireError(value.cause());
        }
      }
    );
  }

  public Observable<Void> close() {
    AsyncResultMemoizeHandler<Void,Void> rh=new AsyncResultMemoizeHandler<Void,Void>();
    netServer.close(rh);
    return Observable.create(rh.subscribe);
  }
}
