package io.vertx.rxcore.test.integration.java;

import java.util.*;

import io.vertx.rxcore.RxSupport;
import io.vertx.rxcore.java.http.*;
import org.junit.Test;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.testtools.TestVerticle;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import static io.vertx.rxcore.test.integration.java.RxAssert.*;

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
                });
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

    Observable<Buffer> ob=client
      .getNow("/ping/get")
      .flatMap(downloadBody());
    
    assertSequenceThenComplete(ob,new Buffer("pong:/ping/get"));
  }

  /*
  * Verify that if underlying httpclient throws an exception, 
  * the observer is notified. 
  */

  @Test
  public void testHttpClientThrowsException() {
    // set the port to an invalid port to cause the underlying client to throw an exception
    final int invalidPort=90080;
    RxHttpClient client=new RxHttpClient(vertx.createHttpClient().setHost("localhost").setPort(invalidPort));
    Observable<RxHttpClientResponse> ob=client.request("GET", "/whatever-random", new Action1<HttpClientRequest>() {
      @Override
      public void call(HttpClientRequest request) {
        request.end();
      }
    });
    
    assertError(ob,IllegalArgumentException.class);
  }

  /*
  * Verify that if the requestBuilder throws an exception, 
  * the observer is notified accordingly. 
  */

  @Test
  public void testHttpClientRequestBuilderThrowsException() {
    RxHttpClient client=new RxHttpClient(vertx.createHttpClient().setHost("localhost").setPort(8080));
    Observable<RxHttpClientResponse> ob=client.request("GET", "/whatever-random", new Action1<HttpClientRequest>() {
      @Override
      public void call(HttpClientRequest request) {
        throw new RuntimeException("Builder Exception");
      }
    });
    
    assertError(ob,RuntimeException.class,"Builder Exception");
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
          assertSingle(s.writeAsTextFrame(Observable.from(seq)),5l);
          assertSequenceThenComplete(s.asObservable(),new Buffer("eeny"),new Buffer("meeny"),new Buffer("miny"),new Buffer("moe"));
        }
      });
  }
}
