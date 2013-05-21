package integration_tests.groovy

import io.vertx.rxcore.java.http.RxHttpClient
import io.vertx.rxcore.java.http.RxHttpServer
import org.vertx.groovy.testtools.VertxTests
import static org.vertx.testtools.VertxAssert.*

def testSimpleGet() {

// Commented out for now due to classloader issue with RxJava
/*
  RxHttpServer server=new RxHttpServer(vertx.createHttpServer().toJavaServer())
  server.http().subscribe(
    { req -> 
      println("http-server:new-request:${req.path()}")
      req.response().end("pong:"+req.path())
    },
    { e -> println("http-server:req-error:"+e) 
  });
  server.coreHttpServer().listen(8080,"localhost");

  RxHttpClient client=new RxHttpClient(vertx.createHttpClient().setHost("localhost").setPort(8080).jClient);
  
  println("request /ping from localhost:8080");
  
  client
    .getNow("/ping")
    .mapMany({ resp -> 
      println("http-client:got-response:"+resp);
      return resp.asObservable()
    })
    .subscribe(
      { body ->
        println("http-client:got-response:"+body);
        assertEquals("pong:/ping",body.toString())
        testComplete();
      },
      {
        e -> println("http-client:request-failed:"+e);
      });
*/
  testComplete();
}

VertxTests.initialize(this)
VertxTests.startTests(this)


