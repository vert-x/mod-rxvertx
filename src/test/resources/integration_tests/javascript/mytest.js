var container = require("container");
var vertxTests = require("vertx_tests");
var vassert = require("vertx_assert");

// The test methods must begin with "test"

function test_1() {
  vassert.testComplete()
}

function test_2() {
  vassert.assertEquals("foo", "foo")
  vassert.testComplete()
}

var script = this;
container.deployModule(java.lang.System.getProperty("vertx.modulename"), null, 1, function(err, depID) {
  if (err != null) {
    err.printStackTrace();
  }

  vassert.assertTrue(err === null);
  vertxTests.startTests(script);
});


