package io.vertx.rxcore.test.integration.java;
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

import io.vertx.rxcore.java.net.RxNetClient;
import io.vertx.rxcore.java.net.RxNetServer;
import io.vertx.rxcore.java.net.RxNetSocket;
import org.junit.Test;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.testtools.TestVerticle;
import rx.Observable;
import rx.util.functions.Action1;

import static org.vertx.testtools.VertxAssert.*;

public class NetIntegrationTest extends TestVerticle {

  @Test
  public void testSimpleConnect() {

    final RxNetServer rxNetServer = new RxNetServer(vertx.createNetServer());
    Observable<RxNetSocket> connectStream = rxNetServer.connectStream();
    Observable<RxNetServer> listenObs = rxNetServer.listen(1234);

    connectStream.subscribe(new Action1<RxNetSocket>() {
      @Override
      public void call(final RxNetSocket rxNetSocket) {
        System.out.println("Got a connection");
        rxNetSocket.dataStream().subscribe(new Action1<Buffer>() {
          @Override
          public void call(Buffer buffer) {
            // Just echo back
            rxNetSocket.coreSocket().write(buffer);
          }
        });
      }
    });

    listenObs.subscribe(new Action1<RxNetServer>() {
      @Override
      public void call(RxNetServer rxNs) {
        assertTrue(rxNetServer == rxNs);

        RxNetClient rxNetClient = new RxNetClient(vertx.createNetClient());
        Observable<RxNetSocket> connectObs = rxNetClient.connect(1234);
        connectObs.subscribe(new Action1<RxNetSocket>() {
          @Override
          public void call(RxNetSocket rxNetSocket) {
            rxNetSocket.coreSocket().write("somedata");
            rxNetSocket.dataStream().subscribe(new Action1<Buffer>() {
              @Override
              public void call(Buffer buffer) {
                System.out.println("Got data " + buffer);
                testComplete();
              }
            });
          }
        });
      }
    });
  }
}
