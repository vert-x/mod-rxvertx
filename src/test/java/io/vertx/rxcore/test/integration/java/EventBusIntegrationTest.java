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

import io.vertx.rxcore.java.eventbus.RxEventBus;
import io.vertx.rxcore.java.eventbus.RxMessage;
import org.junit.Test;
import org.vertx.testtools.TestVerticle;
import rx.Observable;
import rx.util.functions.Action1;
import rx.util.functions.Func1;
import rx.util.functions.Func2;

import static org.vertx.testtools.VertxAssert.*;

public class EventBusIntegrationTest extends TestVerticle {

  @Test
  public void testSimpleSubscribeReply() {
    RxEventBus rxEventBus = new RxEventBus(vertx.eventBus());
    rxEventBus.registerHandler("foo").subscribe(new Action1<RxMessage<String>>() {
      @Override
      public void call(RxMessage<String> message) {
        message.reply("pong!");
      }
    });
    Observable<RxMessage<String>> obs = rxEventBus.send("foo", "ping!");
    obs.subscribe(new Action1<RxMessage<String>>() {
      @Override
      public void call(RxMessage<String> message) {
        assertEquals("pong!", message.body());
        testComplete();
      }
    });
  }


// Unfortunately concat in RxJava is currently blocking so this won't work in Vert.x https://github.com/Netflix/RxJava/issues/270
//  @Test
//  // Send some messages in series - i.e. wait for result of previous one before sending next one
//  public void testSimpleSerial() {
//    vertx.eventBus().registerHandler("foo", new Handler<RxRxMessage<String>>() {
//      @Override
//      public void handle(RxRxMessage<String> message) {
//        message.reply("pong!");
//      }
//    });
//
//    final RxEventBus rxEventBus = new RxEventBus(vertx.eventBus());
//
//    Observable<RxMessage<String>> obs1 = rxEventBus.send("foo", "ping!");
//    Observable<RxMessage<String>> obs2 = rxEventBus.send("foo", "ping!");
//    Observable<RxMessage<String>> obs3 = rxEventBus.send("foo", "ping!");
//
//    Observable<RxMessage<String>> concatenated = Observable.concat(obs1, obs2, obs3);
//
//    concatenated.subscribe(new Action1<RxMessage<String>>() {
//      @Override
//      public void call(RxMessage<String> message) {
//        assertEquals("pong!", message.body());
//        testComplete();
//      }
//    });
//  }

  @Test
  // Send some messages in series where next message sent is function of reply from previous message
  public void testSerial() {
    final RxEventBus rxEventBus = new RxEventBus(vertx.eventBus());

    rxEventBus.registerHandler("foo").subscribe(new Action1<RxMessage<String>>() {
      @Override
      public void call(RxMessage<String> message) {
        message.reply(message.body());
      }
    });

    Observable<RxMessage<String>> obs1 = rxEventBus.send("foo", "ping1");
    Observable<RxMessage<String>> obs2 = obs1.mapMany(new Func1<RxMessage<String>, Observable<RxMessage<String>>>() {
      @Override
      public Observable<RxMessage<String>> call(RxMessage<String> reply) {
        return rxEventBus.send("foo", reply.body() + "ping2");
      }
    });
    Observable<RxMessage<String>> obs3 = obs2.mapMany(new Func1<RxMessage<String>, Observable<RxMessage<String>>>() {
      @Override
      public Observable<RxMessage<String>> call(RxMessage<String> reply) {
        return rxEventBus.send("foo", reply.body() + "ping3");
      }
    });
    obs3.subscribe(new Action1<RxMessage<String>>() {
      @Override
      public void call(RxMessage<String> message) {
        assertEquals("ping1ping2ping3", message.body());
        testComplete();
      }
    });

  }

  @Test
  // Send some messages in parallel and wait for all replies before doing something
  public void testGather() {

    final RxEventBus rxEventBus = new RxEventBus(vertx.eventBus());

    rxEventBus.registerHandler("foo").subscribe(new Action1<RxMessage<String>>() {
      @Override
      public void call(RxMessage<String> message) {
        message.reply("pong!");
      }
    });

    Observable<RxMessage<String>> obs1 = rxEventBus.send("foo", "ping!");
    Observable<RxMessage<String>> obs2 = rxEventBus.send("foo", "ping!");
    Observable<RxMessage<String>> obs3 = rxEventBus.send("foo", "ping!");
    Observable<RxMessage<String>> merged = Observable.merge(obs1, obs2, obs3);

    merged.takeLast(1).subscribe(new Action1<RxMessage<String>>() {
      @Override
      public void call(RxMessage<String> message) {
        assertEquals("pong!", message.body());
        testComplete();
      }
    });
  }

  @Test
  // Send some messages in parallel and return result of concatenating all the messages
  public void testConcatResults() {
    final RxEventBus rxEventBus = new RxEventBus(vertx.eventBus());

    rxEventBus.registerHandler("foo").subscribe(new Action1<RxMessage<String>>() {
      @Override
      public void call(RxMessage<String> message) {
        message.reply("pong!");
      }
    });

    Observable<RxMessage<String>> obs1 = rxEventBus.send("foo", "ping!");
    Observable<RxMessage<String>> obs2 = rxEventBus.send("foo", "ping!");
    Observable<RxMessage<String>> obs3 = rxEventBus.send("foo", "ping!");
    Observable<RxMessage<String>> merged = Observable.merge(obs1, obs2, obs3);
    Observable<String> result = Observable.reduce(merged, "", new Func2<String, RxMessage<String>, String>() {
      @Override
      public String call(String accum, RxMessage<String> reply) {
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

    Observable<RxMessage<String>> obs = rxEventBus.registerHandler("foo");

    obs.subscribe(new Action1<RxMessage<String>>() {
      @Override
      public void call(RxMessage<String> message) {
        assertEquals("hello", message.body());
        testComplete();
      }
    });

    vertx.eventBus().send("foo", "hello");
  }

  @Test
  public void testReplyToReply() {
    final RxEventBus rxEventBus = new RxEventBus(vertx.eventBus());

    Observable<RxMessage<String>> obsReply1 = rxEventBus.registerHandler("foo");

    Observable<RxMessage<String>> obsReply2 = obsReply1.mapMany(new Func1<RxMessage<String>, Observable<RxMessage<String>>>() {
      @Override
      public Observable<RxMessage<String>> call(RxMessage<String> stringRxMessage) {
        // Reply to the message
        assertEquals("hello1", stringRxMessage.body());
        return stringRxMessage.reply("goodday1");
      }
    });

    Observable<RxMessage<String>> obsReply3 = obsReply2.mapMany(new Func1<RxMessage<String>, Observable<RxMessage<String>>>() {
      @Override
      public Observable<RxMessage<String>> call(RxMessage<String> stringRxMessage) {
        // Reply to the reply!
        assertEquals("hello2", stringRxMessage.body());
        return stringRxMessage.reply("goodday2");
      }
    });
    obsReply3.subscribe(new Action1<RxMessage<String>>() {
      @Override
      public void call(RxMessage<String> stringRxMessage) {
      }
    });

    Observable<RxMessage<String>> obsSend1 = rxEventBus.send("foo", "hello1");

    Observable<RxMessage<String>> obsSend2 = obsSend1.mapMany(new Func1<RxMessage<String>, Observable<RxMessage<String>>>() {
      @Override
      public Observable<RxMessage<String>> call(RxMessage<String> stringRxMessage) {
        // The first reply
        assertEquals("goodday1", stringRxMessage.body());
        // Now reply to the reply
        return stringRxMessage.reply("hello2");
      }
    });

    obsSend2.subscribe(new Action1<RxMessage<String>>() {
      @Override
      public void call(RxMessage<String> stringRxMessage) {
        // The reply to the reply
        assertEquals("goodday2", stringRxMessage.body());
        testComplete();
      }
    });

  }


}
