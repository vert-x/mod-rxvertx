# Always require "vertx_tests" at the top of your test script
require "vertx_tests"

require "vertx"

# The test methods must begin with "test"

def test1
  VertxAssert.testComplete()
end

# And make sure you call init_tests at the bottom
init_tests(self)


