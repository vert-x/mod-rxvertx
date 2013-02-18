# Import the VertxAssert class - this has the exact same API as JUnit
from io.vertx.testtools import VertxAssert

import vertx

def test_1() :
    VertxAssert.testComplete()

def test_2() :
    VertxAssert.testComplete()

# At the end of your test script make sure call init_tests
import vertx_tests
vertx_tests.init_tests(locals())