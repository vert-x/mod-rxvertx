// Import the VertxAssert class - this has the same API as JUnit

import static org.vertx.testtools.VertxAssert.*

// And import static the VertxTests script
import org.vertx.groovy.testtools.VertxTests;

// The test methods must being with "test"

def test_1() {
  assertEquals("foo", "foo")
  testComplete()
}

def test_2() {
  testComplete()
}

// Make sure you call initTests at the bottom of your script
VertxTests.initTests(this)