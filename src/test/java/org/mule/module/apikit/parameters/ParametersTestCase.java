/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.parameters;

import org.junit.Test;
import org.mule.module.apikit.AbstractMultiParserFunctionalTestCase;
import org.mule.test.runner.ArtifactClassLoaderRunnerConfig;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.core.Is.is;

@ArtifactClassLoaderRunnerConfig
public class ParametersTestCase extends AbstractMultiParserFunctionalTestCase {

  @Override
  protected String getConfigFile() {
    return "org/mule/module/apikit/parameters/parameters-config.xml";
  }

  @Test
  public void requiredHeaderNotProvided() throws Exception {
    given()
        .expect().response().statusCode(400)
        .body(is("Required header one not specified"))
        .when().get("/api/resources?first=fi");
  }

  @Test
  public void invalidEnumHeaderProvided() throws Exception {
    given().header("one", "invalid")
        .expect().response().statusCode(400)
        .body(containsString("Invalid value 'invalid' for header one"))
        .when().get("/api/resources?first=fi");
  }

  @Test
  public void invalidHeaderPlaceholderProvided() throws Exception {
    given().header("mule-special", "dough").header("one", "foo")
        .expect().response().statusCode(400)
        .body(containsString("Invalid value 'dough' for header mule-{?}"))
        .when().get("/api/resources?first=fi");
  }

  @Test
  public void validHeaderPlaceholderProvided() throws Exception {
    given().header("mule-special", "yeah").header("one", "foo")
        .expect().response().statusCode(200)
        .when().get("/api/resources?first=fi");
  }

  @Test
  public void requiredQueryParamAndHeaderProvided() throws Exception {
    given().header("one", "foo")
        .expect().response().statusCode(200)
        .when().get("/api/resources?first=fi");
  }

  @Test
  public void defaultValue() throws Exception {
    given()
        .expect().response().statusCode(200)
        .body(is("default"))
        .when().get("/api/default");
  }

  @Test
  public void raml() throws Exception {
    given().header("Accept", "application/raml+yaml")
        .expect()
        .response().body(allOf(containsString("baseUri"),
                               containsString("http://localhost:" + serverPort.getNumber() + "/api")))
        .statusCode(200).when().get("/console/");
  }

}
