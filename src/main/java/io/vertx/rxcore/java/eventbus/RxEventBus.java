package io.vertx.rxcore.java.eventbus;

import io.vertx.rxcore.java.impl.SubscriptionHandler;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
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
public class RxEventBus {

  // Customer handlers
  
  /** Standard SendHandler */
  protected static class SendHandler<R> extends SubscriptionHandler<RxMessage<R>,Message<R>> {
    @Override public void handle(Message m) {
      fireResult(new RxMessage(m));
    }
  }
  
  /** Async SendHandler */
  protected static class AsyncSendHandler<R> extends SubscriptionHandler<RxMessage<R>, AsyncResult<Message<R>>> {
    @Override public void handle(AsyncResult<Message<R>> r) {
      if (r.succeeded()) {
        fireResult(new RxMessage(r.result()));
      }
      else {
        fireError(r.cause());
      }
    }
  }
  
  /** Receive handler */
  protected static class ReceiveHandler<R> extends SubscriptionHandler<RxMessage<R>,Message> {
    @Override public void handle(Message m) {
      fireNext(new RxMessage(m));
    }
  }
  
  // Instance variables

  /** Core bus */
  private final EventBus eventBus;

  // Public
  
  /** Create new RxEventBus */
  public RxEventBus(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  /** Send a message */
  public <S,R> Observable<RxMessage<R>> send(final String address, final S msg) {
    SendHandler<R> h=new SendHandler<R>();
    this.eventBus.send(address,msg,(Handler)h);
    return Observable.create(h); 
  }
  
  /** Send a message with timeout */
  public <S,R> Observable<RxMessage<R>> sendWithTimeout(final String address, final S msg, final long timeout) {
    AsyncSendHandler<R> h=new AsyncSendHandler<R>();
    this.eventBus.sendWithTimeout(address,msg,timeout,h);
    return Observable.create(h); 
  }

  /** Create an Observable that executes send() on subscribe */
  public <S,R> Observable<RxMessage<R>> observeSend(final String address, final S msg) {
    return Observable.create(new SendHandler<R>() {
      @Override public void execute() {
        eventBus.send(address,msg,(Handler)this);
      }
    });
  }
  
  /** Create an Observable that executes sendWithTimeout() on subscribe */
  public <S,R> Observable<RxMessage<R>> observeSendWithTimeout(final String address, final S msg, final long timeout) {
    return Observable.create(new AsyncSendHandler<R>() {
      @Override public void execute() {
        eventBus.sendWithTimeout(address,msg,timeout,this);
      }
    });
  }

  /** Register a handler */
  public <T> Observable<RxMessage<T>> registerLocalHandler(final String address) {
    return Observable.create(new ReceiveHandler<T>() {
      @Override public void execute() {
        eventBus.registerLocalHandler(address,this);
      }
    });
  }

  /** Register a handler */
  public <T> Observable<RxMessage<T>> registerHandler(final String address) {
    return Observable.create(new ReceiveHandler<T>() {
      @Override public void execute() {
        eventBus.registerHandler(address,this);
      }
    });
  }
}
