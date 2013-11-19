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

import java.util.concurrent.atomic.AtomicInteger;

import io.vertx.rxcore.java.eventbus.RxEventBus;
import io.vertx.rxcore.java.eventbus.RxMessage;
import org.junit.Test;
import org.vertx.testtools.TestVerticle;
import rx.Observable;
import rx.util.functions.*;
import static io.vertx.rxcore.test.integration.java.RxAssert.assertMessageThenComplete;
import static org.vertx.testtools.VertxAssert.assertEquals;
import static org.vertx.testtools.VertxAssert.testComplete;

public class EventBusIntegrationTest extends TestVerticle {

  @Test
  public void testSimpleSubscribeReply() {
    RxEventBus rxEventBus = new RxEventBus(vertx.eventBus());
    rxEventBus.<String>registerHandler("foo").subscribe(new Action1<RxMessage<String>>() {
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


  @Test
  // Send some messages in series - i.e. wait for result of previous one before sending next one
  // PMCD: Added check to enforce 1-at-a-time 
  //       
  public void testSimpleSerial() {
    final RxEventBus rxEventBus = new RxEventBus(vertx.eventBus());
    final AtomicInteger totalReqs = new AtomicInteger(3);
    final AtomicInteger activeReqs = new AtomicInteger(0);
    
    rxEventBus.<String>registerHandler("foo").subscribe(new Action1<RxMessage<String>>() {
      @Override
      public void call(RxMessage<String> message) {
        System.out.println("serial-foo["+message.body()+"]");
        message.reply("pong!");
        activeReqs.incrementAndGet();
      }
    });

    Observable<RxMessage<String>> obs1 = rxEventBus.observeSend("foo", "ping!");
    Observable<RxMessage<String>> obs2 = rxEventBus.observeSend("foo", "ping!");
    Observable<RxMessage<String>> obs3 = rxEventBus.observeSend("foo", "ping!");

    Observable<RxMessage<String>> concatenated = Observable.concat(obs1, obs2, obs3);

    concatenated.subscribe(new Action1<RxMessage<String>>() {
      @Override
      public void call(RxMessage<String> message) {
        System.out.println("serial-resp["+message.body()+"]");
        assertEquals("pong!", message.body());
        assertEquals(0,activeReqs.decrementAndGet());
        if (totalReqs.decrementAndGet()==0)
          testComplete();
      }
    });
  }

  @Test
  // Send some messages in series where next message sent is function of reply from previous message
  public void testSerial() {
    final RxEventBus rxEventBus = new RxEventBus(vertx.eventBus());

    rxEventBus.<String>registerHandler("foo").subscribe(new Action1<RxMessage<String>>() {
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
    
    assertMessageThenComplete(obs3,"ping1ping2ping3");
  }

  @Test
  // Send some messages in parallel and wait for all replies before doing something
  public void testGather() {

    final RxEventBus rxEventBus = new RxEventBus(vertx.eventBus());

    rxEventBus.<String>registerHandler("foo").subscribe(new Action1<RxMessage<String>>() {
      @Override
      public void call(RxMessage<String> msg) {
        msg.reply("pong"+msg.body());
      }
    });

    Observable<RxMessage<String>> obs1 = rxEventBus.send("foo", "A");
    Observable<RxMessage<String>> obs2 = rxEventBus.send("foo", "B");
    Observable<RxMessage<String>> obs3 = rxEventBus.send("foo", "C");
    Observable<RxMessage<String>> merged = Observable.merge(obs1, obs2, obs3);

    assertMessageThenComplete(merged.takeLast(1),"pongC");
  }

  @Test
  // Send some messages in parallel and return result of concatenating all the messages
  public void testConcatResults() {
    final RxEventBus rxEventBus = new RxEventBus(vertx.eventBus());

    rxEventBus.<String>registerHandler("foo").subscribe(new Action1<RxMessage<String>>() {
      @Override
      public void call(RxMessage<String> msg) {
        msg.reply("pong"+msg.body());
      }
    });

    Observable<RxMessage<String>> obs1 = rxEventBus.send("foo", "A");
    Observable<RxMessage<String>> obs2 = rxEventBus.send("foo", "B");
    Observable<RxMessage<String>> obs3 = rxEventBus.send("foo", "C");
    Observable<RxMessage<String>> merged = Observable.merge(obs1, obs2, obs3);
    Observable<String> result = merged.reduce("", new Func2<String, RxMessage<String>, String>() {
      @Override
      public String call(String accum, RxMessage<String> reply) {
        return accum + reply.body();
      }
    });

    RxAssert.assertSequenceThenComplete(result.takeLast(1),"pongApongBpongC");
  }

  @Test
  public void testSimpleRegisterHandler() {
    final RxEventBus rxEventBus = new RxEventBus(vertx.eventBus());

    Observable<RxMessage<String>> obs = rxEventBus.registerHandler("foo");

    assertMessageThenComplete(obs.takeFirst(),"hello");

    // Send using core EventBus
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
        return stringRxMessage.observeReply("goodday1");
      }
    });

    Observable<RxMessage<String>> obsReply3 = obsReply2.mapMany(new Func1<RxMessage<String>, Observable<RxMessage<String>>>() {
      @Override
      public Observable<RxMessage<String>> call(RxMessage<String> stringRxMessage) {
        // Reply to the reply!
        assertEquals("hello2", stringRxMessage.body());
        return stringRxMessage.observeReply("goodday2");
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
        return stringRxMessage.observeReply("hello2");
      }
    });

    assertMessageThenComplete(obsSend2,"goodday2");
  }

}
