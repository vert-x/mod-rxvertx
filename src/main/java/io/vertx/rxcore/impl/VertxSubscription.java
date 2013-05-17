package io.vertx.rxcore.impl;

import org.vertx.java.core.eventbus.Message;
import rx.Observer;
import rx.Subscription;

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
public class VertxSubscription<T> implements Subscription {

  private Observer<T> replyObserver;
  private Runnable onUnsubscribe;

  public void setOnUnsubscribe(Runnable onUnsubscribe) {
    this.onUnsubscribe = onUnsubscribe;
  }

  public void setObserver(Observer<T> replyObserver) {
    this.replyObserver = replyObserver;
  }

  public void handleResult(T message) {
    if (replyObserver != null) {
      replyObserver.onNext(message);
    }
  }

  public void complete() {
    if (replyObserver != null) {
      replyObserver.onCompleted();
    }
  }

  public void failed(Throwable t) {
    if (t instanceof Exception) {
      if (replyObserver != null) {
        replyObserver.onError((Exception)t);
      }
    } else {
      t.printStackTrace(); // FIXME - better logging
    }
  }

  @Override
  public void unsubscribe() {
    if (onUnsubscribe != null) {
      try {
        onUnsubscribe.run();
      } catch (Throwable t) {
        t.printStackTrace(); // FIXME
      }
    }
    if (replyObserver != null) {
      replyObserver.onCompleted();
      replyObserver = null;
    }
  }
}
