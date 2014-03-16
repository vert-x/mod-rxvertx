package io.vertx.rxcore.java.net;

import io.vertx.rxcore.RxSupport;
import org.vertx.java.core.buffer.Buffer;
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
public class RxNetSocket {
  private final NetSocket netSocket;
  private Observable<Buffer> dataStream;

  RxNetSocket(NetSocket netSocket) {
    this.netSocket = netSocket;
  }

  public NetSocket coreSocket() {
    return netSocket;
  }

  /** Return as Observable<Buffer> */
  public Observable<Buffer> asObservable() {
    return RxSupport.toObservable(netSocket);
  }

  /** @deprecated use {@link #asObservable()} */
  @Deprecated
  public Observable<Buffer> dataStream() {
    return RxSupport.toObservable(netSocket);
  }
}
