package com.mycompany.myproject.test.integration.java;
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

import io.vertx.rxcore.eventbus.RxEventBus;
import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.testtools.TestVerticle;
import rx.Observable;
import rx.util.functions.Action1;
import rx.util.functions.Func1;
import rx.util.functions.Func2;

import static org.vertx.testtools.VertxAssert.*;

public class RxIntegrationTest extends TestVerticle {

  @Test
  public void testSimpleSubscribeReply() {
    vertx.eventBus().registerHandler("foo", new Handler<Message<String>>() {
      @Override
      public void handle(Message<String> message) {
        message.reply("pong!");
      }
    });
    RxEventBus rxEventBus = new RxEventBus(vertx.eventBus());
    Observable<Message<String>> obs = rxEventBus.send("foo", "ping!");
    obs.subscribe(new Action1<Message<String>>() {
      @Override
      public void call(Message<String> message) {
        assertEquals("pong!", message.body());
        testComplete();
      }
    });
  }


// Unfortunately concat in RxJava is currently blocking so this won't work in Vert.x https://github.com/Netflix/RxJava/issues/270
//  @Test
//  // Send some messages in series - i.e. wait for result of previous one before sending next one
//  public void testSimpleSerial() {
//    vertx.eventBus().registerHandler("foo", new Handler<Message<String>>() {
//      @Override
//      public void handle(Message<String> message) {
//        message.reply("pong!");
//      }
//    });
//
//    final RxEventBus rxEventBus = new RxEventBus(vertx.eventBus());
//
//    Observable<Message<String>> obs1 = rxEventBus.send("foo", "ping!");
//    Observable<Message<String>> obs2 = rxEventBus.send("foo", "ping!");
//    Observable<Message<String>> obs3 = rxEventBus.send("foo", "ping!");
//
//    Observable<Message<String>> concatenated = Observable.concat(obs1, obs2, obs3);
//
//    concatenated.subscribe(new Action1<Message<String>>() {
//      @Override
//      public void call(Message<String> message) {
//        assertEquals("pong!", message.body());
//        testComplete();
//      }
//    });
//  }

  @Test
  // Send some messages in series where next message sent is function of reply from previous message
  public void testSerial() {
    vertx.eventBus().registerHandler("foo", new Handler<Message<String>>() {
      @Override
      public void handle(Message<String> message) {
        // just echo back
        message.reply(message.body());
      }
    });

    final RxEventBus rxEventBus = new RxEventBus(vertx.eventBus());

    Observable<Message<String>> obs1 = rxEventBus.send("foo", "ping1");
    Observable<Message<String>> obs2 = obs1.mapMany(new Func1<Message<String>, Observable<Message<String>>>() {
      @Override
      public Observable<Message<String>> call(Message<String> reply) {
        return rxEventBus.send("foo", reply.body() + "ping2");
      }
    });
    Observable<Message<String>> obs3 = obs2.mapMany(new Func1<Message<String>, Observable<Message<String>>>() {
      @Override
      public Observable<Message<String>> call(Message<String> reply) {
        return rxEventBus.send("foo", reply.body() + "ping3");
      }
    });
    obs3.subscribe(new Action1<Message<String>>() {
      @Override
      public void call(Message<String> message) {
        assertEquals("ping1ping2ping3", message.body());
        testComplete();
      }
    });

  }

  @Test
  // Send some messages in parallel and wait for all replies before doing something
  public void testGather() {
    vertx.eventBus().registerHandler("foo", new Handler<Message<String>>() {
      @Override
      public void handle(Message<String> message) {
        message.reply("pong!");
      }
    });

    final RxEventBus rxEventBus = new RxEventBus(vertx.eventBus());

    Observable<Message<String>> obs1 = rxEventBus.send("foo", "ping!");
    Observable<Message<String>> obs2 = rxEventBus.send("foo", "ping!");
    Observable<Message<String>> obs3 = rxEventBus.send("foo", "ping!");
    Observable<Message<String>> merged = Observable.merge(obs1, obs2, obs3);

    merged.takeLast(1).subscribe(new Action1<Message<String>>() {
      @Override
      public void call(Message<String> message) {
        assertEquals("pong!", message.body());
        testComplete();
      }
    });

  }

  @Test
  // Send some messages in parallel and return result of concatenating all the messages
  public void testConcatResults() {
    vertx.eventBus().registerHandler("foo", new Handler<Message<String>>() {
      @Override
      public void handle(Message<String> message) {
        message.reply("pong!");
      }
    });

    final RxEventBus rxEventBus = new RxEventBus(vertx.eventBus());

    Observable<Message<String>> obs1 = rxEventBus.send("foo", "ping!");
    Observable<Message<String>> obs2 = rxEventBus.send("foo", "ping!");
    Observable<Message<String>> obs3 = rxEventBus.send("foo", "ping!");
    Observable<Message<String>> merged = Observable.merge(obs1, obs2, obs3);
    Observable<String> result = Observable.reduce(merged, "", new Func2<String, Message<String>, String>() {
      @Override
      public String call(String accum, Message<String> reply) {
        return accum + reply.body();
      }
    });

    result.takeLast(1).subscribe(new Action1<String>() {
      @Override
      public void call(String res) {
        assertEquals("pong!pong!pong!", res);
        testComplete();
      }
    });

  }

  @Test
  public void testSimpleRegisterHandler() {
    final RxEventBus rxEventBus = new RxEventBus(vertx.eventBus());

    Observable<Message<String>> obs = rxEventBus.registerHandler("foo");

    obs.subscribe(new Action1<Message<String>>() {
      @Override
      public void call(Message<String> message) {
        assertEquals("hello", message.body());
        testComplete();
      }
    });

    vertx.eventBus().send("foo", "hello");
  }


}
