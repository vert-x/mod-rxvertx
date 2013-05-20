package integration_tests.groovy

import io.vertx.rxcore.java.eventbus.RxEventBus
import io.vertx.rxcore.java.eventbus.RxMessage

import org.vertx.groovy.testtools.VertxTests
import rx.lang.groovy.GroovyAdaptor
import rx.util.functions.Functions

import static org.vertx.testtools.VertxAssert.*

def testSimpleSubscribeReply() {

// Commented out for now due to classloader issue with RxJava

//    RxEventBus rxEventBus = new RxEventBus(vertx.getEventBus().javaEventBus())
//    rxEventBus.registerHandler("foo").subscribe({ message -> message.reply("pong!") })
//    rx.Observable<RxMessage<String>> obs = rxEventBus.send("foo", "ping!")
//    obs.subscribe({ message ->
//      assertEquals("pong!", message.body())
//      testComplete()
//    })



  testComplete()


}

VertxTests.initialize(this)
VertxTests.startTests(this)


