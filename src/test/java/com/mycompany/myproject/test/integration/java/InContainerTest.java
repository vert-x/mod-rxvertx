package com.mycompany.myproject.test.integration.java;/*
 * Copyright 2013 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */

import static io.vertx.testtools.VertxAssert.*;

import io.vertx.testtools.TestVerticle;
import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.http.HttpServerRequest;

/**
 * Example Java integration test
 *
 * You should extend TestVerticle.
 *
 * We do a bit of magic and the test will actually be run _inside_ the Vert.x container as a Verticle.
 *
 * You can use the standard JUnit Assert API in your test by using the VertxAssert class
 */
public class InContainerTest extends TestVerticle {

  @Test
  public void testDeployMod() {
    container.deployModule("maven:com.mycompany:my-module:1.0.0-SNAPSHOT", new Handler<String>() {
      @Override
      public void handle(String deploymentID) {
        assertNotNull("deploymentID should not be null", deploymentID);
        testComplete();
      }
    });
  }

  @Test
  public void testHTTP() {
    vertx.createHttpServer().requestHandler(new Handler<HttpServerRequest>() {
      public void handle(HttpServerRequest req) {
        req.response.end();
      }
    }).listen(8181);
    vertx.createHttpClient().setPort(8181).getNow("/",new Handler<HttpClientResponse>() {
      @Override
      public void handle(HttpClientResponse resp) {
        assertEquals(200, resp.statusCode);
        testComplete();
      }
    });
  }

  @Test
  public void test3() {
    container.deployVerticle(SomeVerticle.class.getName());
  }

  @Test
  public void test4() {
    testComplete();
  }

}
