package io.vertx.rxcore.test.integration.java;

import java.util.*;

import io.vertx.rxcore.RxSupport;
import io.vertx.rxcore.java.http.*;
import org.junit.Test;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.testtools.TestVerticle;
import rx.Observable;
import rx.util.functions.*;
import static org.vertx.testtools.VertxAssert.assertEquals;
import static org.vertx.testtools.VertxAssert.testComplete;

/** HttpIntegrationTest
 * @author <a href="http://github.com/petermd">Peter McDonnell</a>
 */
public class HttpIntegrationTest extends TestVerticle {
  
  protected void createHttpPingServer() {
    RxHttpServer server=new RxHttpServer(vertx.createHttpServer());
    
    server
      .http()
        .subscribe(new Action1<RxHttpServerRequest>() {
          public void call(RxHttpServerRequest req) {
            System.out.println("HttpServer:"+req.path());
            req.response().end("pong:"+req.path());
          }
        });
    
    server.coreHttpServer().listen(8080,"localhost");
  }

  protected void createWebSocketPingServer() {
    RxHttpServer server=new RxHttpServer(vertx.createHttpServer());
    
    server
      .websocket()
        .subscribe(
          new Action1<RxServerWebSocket>() {
            public void call(final RxServerWebSocket s) {
              System.out.println("WebSocketServer:"+s.path());
              s.asObservable().subscribe(
                new Action1<Buffer>() {
                  public void call(Buffer b) {
                    System.out.println("WebSocketServer:received["+b+"]");
                    if ("EOF".equals(b.toString())) {
                      s.close();
                    } else {
                      s.writeTextFrame(b.toString());
                    }
                  }
                },
                RxTestSupport.traceError,RxTestSupport.traceComplete);
            }
        });

    server.coreHttpServer().listen(8090,"localhost");
  }

  /** Download response a single buffer */
  public static Func1<RxHttpClientResponse,Observable<Buffer>> downloadBody() {
    return new Func1<RxHttpClientResponse,Observable<Buffer>>() {
      public Observable<Buffer> call(RxHttpClientResponse resp) {
        System.out.println("HttpClient:response-received");
        return resp
          .asObservable()
          .reduce(RxSupport.mergeBuffers);
      }
    };
  } 
  
  @Test
  public void testGetNow() {

    createHttpPingServer();
    
    RxHttpClient client=new RxHttpClient(vertx.createHttpClient().setHost("localhost").setPort(8080));

    client
      .getNow("/ping/get")
      .mapMany(downloadBody())
      .subscribe(new Action1<Buffer>() {
        public void call(Buffer b) {
          System.out.println("HttpClient:response["+b+"]");
          assertEquals("response","pong:/ping/get",b.toString());
          testComplete();
        }
      });
  }

  @Test
  public void testWebSocket() {

    createWebSocketPingServer();
    
    RxHttpClient client=new RxHttpClient(vertx.createHttpClient().setHost("localhost").setPort(8090));
    
    final List<String> seq=new ArrayList(Arrays.asList("eeny","meeny","miny","moe","EOF"));

    client
      .connectWebsocket("/ping/connect")
      .subscribe(new Action1<RxWebSocket>() {
        public void call(RxWebSocket s) {
          System.out.println("WebSocket:connected");
          s
            .writeAsTextFrame(Observable.toObservable(seq))
            .subscribe(RxTestSupport.traceNext,RxTestSupport.traceError,RxTestSupport.traceComplete);
          s.asObservable()
            .subscribe(
              new Action1<Buffer>() {
                public void call(Buffer b) {
                  System.out.println("WebSocket:response["+b+"]");
                  assertEquals(b.toString(),seq.remove(0));
                }
              },
              RxTestSupport.traceError,
              new Action0() {
                public void call() {
                  testComplete();
                }
              });
        }
      });
  }
}
