load("vertx.js");
load("vertx_tests.js");

// The test methods must begin with "test"

function test_1() {
  vassert.testComplete()
}

function test_2() {
  vassert.assertEquals("foo", "foo")
  vassert.testComplete()
}

initTests(this);


