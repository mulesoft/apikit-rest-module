/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.junit.Rule;
import org.junit.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpResponse;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.mockserver.matchers.Times.unlimited;
import static org.mockserver.model.HttpRequest.request;

import org.mule.runtime.core.api.util.IOUtils;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.tck.junit4.rule.SystemProperty;

import java.io.InputStream;

public class RouterRemoteRamlTestCase extends AbstractMultiParserFunctionalTestCase {

  @Rule
  public DynamicPort port = new DynamicPort("http.server.port");

  private ClientAndServer server;

  @Override
  protected String getConfigFile() {
    return "org/mule/module/apikit/router-remote-raml/remote-raml.xml";
  }

  @Override
  protected void doSetUpBeforeMuleContextCreation() {
    this.server = ClientAndServer.startClientAndServer(port.getNumber());
    this.server
      .when(request().withMethod("GET").withPath("/testRaml"), unlimited())
      .respond(HttpResponse.response().withBody(getTestRamlAsString())
                  .withStatusCode(200)
                  .withHeader("Content-Type", "application/raml+yaml"));
    super.doSetUpBeforeMuleContextCreation();
  }

  private String getTestRamlAsString() {
    try {
      ClassLoader ccl = Thread.currentThread().getContextClassLoader();
      return IOUtils.toString(ccl.getResourceAsStream("org/mule/module/apikit/router-remote-raml/remote.raml"));
    } catch (Exception e) {
      throw  new RuntimeException("fail to create HTTP Test Server", e);
    }
  }

  @Test
  public void simpleRouting() {
    given().header("Accept", "*/*")
        .expect()
        .response().body(is("hello"))
        .statusCode(200)
        .when().get("/api/resources");
  }
}
