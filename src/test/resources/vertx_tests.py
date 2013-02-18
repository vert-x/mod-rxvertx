import vertx
from io.vertx.testtools import VertxAssert
from org.vertx.java.platform.impl import JythonVerticleFactory

def init_tests(locs) :
    VertxAssert.initialize(JythonVerticleFactory.vertx)
    method_name = vertx.config()['methodName']
    locs[method_name]()