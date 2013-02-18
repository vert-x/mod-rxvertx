// Import the VertxAssert class - this has the same API as JUnit
import org.vertx.testtools.VertxAssert
// And import static the VertxTests script
import static VertxTests.*

// The test methods must being with "test"

def test_1() {
  VertxAssert.assertEquals("foo", "foo")
  VertxAssert.testComplete()
}

def test_2() {
  VertxAssert.testComplete()
}

// Make sure you call initTests at the bottom of your script
initTests(this)