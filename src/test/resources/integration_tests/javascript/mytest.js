load("vertx.js");
load("vertx_tests.js");

function test_1() {
  console.log("in test1")
  vassert.testComplete()
}

function test_2() {
  console.log("in test2")
  vassert.testComplete()
  //vassert.assertEquals("foo", "bar");
}

initTests(this);


