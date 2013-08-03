package integration_tests.groovy

import io.vertx.rxcore.java.eventbus.RxEventBus

import org.vertx.groovy.testtools.VertxTests

import static org.vertx.testtools.VertxAssert.*

def testSimpleSubscribeReply() {

  RxEventBus rxEventBus = new RxEventBus(vertx.getEventBus().javaEventBus())
  rxEventBus.registerHandler("foo").subscribe({ message -> message.reply("pong!") })
  def obs = rxEventBus.send("foo", "ping!")
  obs.subscribe({ message ->
    assertEquals("pong!", message.body())
    testComplete()
  })
  
}

VertxTests.initialize(this)
VertxTests.startTests(this)


