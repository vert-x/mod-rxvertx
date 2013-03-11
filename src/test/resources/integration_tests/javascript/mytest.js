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

var script = this;
vertx.deployModule(java.lang.System.getProperty("vertx.modulename"), null, 1, function(depID) {
  console.log("dep id is " + depID)
  initTests(script);
});


