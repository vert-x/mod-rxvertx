package io.vertx.rxcore.java;

import io.vertx.rxcore.java.eventbus.RxEventBus;
import io.vertx.rxcore.java.http.RxHttpClient;
import io.vertx.rxcore.java.http.RxHttpServer;
import io.vertx.rxcore.java.net.RxNetClient;
import io.vertx.rxcore.java.net.RxNetServer;
import io.vertx.rxcore.java.timer.RxTimer;
import org.vertx.java.core.Vertx;
import rx.Observable;

/** RxVertx */
public class RxVertx {
  
  /** Core */
  private Vertx core;
  
  /** EventBus */
  private RxEventBus eventBus;
  
  /** Timer */
  private RxTimer timer;
  
  /** Create RxVertx from Core */
  public RxVertx(Vertx vertx) {
    this.core=vertx;
    this.eventBus=new RxEventBus(core.eventBus());
    this.timer=new RxTimer(core);
  }
  
  /** Return core */
  public Vertx coreVertx() {
    return this.core;
  }
  
  // Services 
  
  /** Create NetServer */
  public RxNetServer createNetServer() {
    return new RxNetServer(core.createNetServer());
  }

  /** Create NetClient */
  public RxNetClient createNetClient() {
    return new RxNetClient(core.createNetClient());
  }

  /** Create HttpServer */
  public RxHttpServer createHttpServer() {
    return new RxHttpServer(core.createHttpServer());
  }

  /** Create HttpClient */
  public RxHttpClient createHttpClient() {
    return new RxHttpClient(core.createHttpClient());
  }

  // TODO: createFileSystem 

  /** Return EventBus */
  public RxEventBus eventBus() {
    return this.eventBus; 
  }

  // Timer
  
  /** Set One-off Timer 
   *
   * @see RxTimer#setTimer 
   * 
   **/ 
  public Observable<Long> setTimer(final long delay) {
    return this.timer.setTimer(delay);
  }
  
  /** Set Periodic Timer
   *
   * @see RxTimer#setPeriodic 
   * 
   **/ 
  public Observable<Long> setPeriodic(final long delay) {
    return this.timer.setPeriodic(delay);
  }
}
