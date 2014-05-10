package io.vertx.rxcore.java.net;

import io.vertx.rxcore.java.impl.AsyncResultMemoizeHandler;
import org.vertx.java.core.net.NetClient;
import org.vertx.java.core.net.NetSocket;
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
    AsyncResultMemoizeHandler<RxNetSocket,NetSocket> rh=new AsyncResultMemoizeHandler<RxNetSocket,NetSocket>() {
      @Override
      public RxNetSocket wrap(NetSocket s) {
        return new RxNetSocket(s);
      }
    };
    netClient.connect(port,host,rh);
    return Observable.create(rh.subscribe);
  }
}
