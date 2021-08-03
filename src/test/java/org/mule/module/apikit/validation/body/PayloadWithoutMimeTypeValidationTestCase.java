/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body;

import org.junit.Before;
import org.junit.Test;
import org.mule.module.apikit.api.exception.BadRequestException;
import org.mule.module.apikit.validation.AbstractRequestValidatorTestCase;
import org.mule.runtime.api.util.MultiMap;

import java.util.Collections;

/**
 * This test class is mainly intended to show how we handle an OAS 2.0 specification bug where it lets you define
 * a payload with an schema and without a mime type, in order to help the user and keeps consistency with mocking
 * service APIkit will validate according the incoming Content-Type header
 */
public class PayloadWithoutMimeTypeValidationTestCase extends AbstractRequestValidatorTestCase {


  private static final String VALID_XML = "<User><id>1</id><name>name</name></User>";
  private static final String INVALID_XML = "<User><id1>1</id1><name>name</name></User>";
  private static final String VALID_JSON = "{\"id\":1, \"name\":\"test\"}";
  private static final String INVALID_JSON = "{\"id1\":1, \"name\":\"test\"}";
  private TestRestRequestValidatorBuilder withSchemaRequestBuilder;

  @Before
  public void setUp() {
    withSchemaRequestBuilder =
        testRestRequestValidatorBuilder.withApiLocation("unit/validation/oas20-payload-without-mimetype.yaml")
            .withRequestPath("/api/withSchema")
            .withRelativePath("/withSchema")
            .withMethod("POST");
  }

  @Test
  public void invalidJsonWithSchemaAndWithoutMimeTypeTest() throws Exception {
    if (parser.name().equals("RAML")) {
      return;
    }
    withSchemaRequestBuilder
        .withHeaders(new MultiMap<>(Collections.singletonMap("Content-Type", "application/json")))
        .withBody(INVALID_JSON)
        .build()
        .assertThrows(BadRequestException.class, "required key [id] not found");
  }

  @Test
  public void validJsonWithSchemaAndWithoutMimeTypeTest() throws Exception {
    if (parser.name().equals("RAML")) {
      return;
    }
    withSchemaRequestBuilder
        .withBody(VALID_JSON)
        .withHeaders(new MultiMap<>(Collections.singletonMap("Content-Type", "application/json")))
        .build()
        .validateRequest();
  }


  @Test
  public void validXMLWithSchemaAndWithoutMimeTypeTest() throws Exception {
    if (parser.name().equals("RAML")) {
      return;
    }
    withSchemaRequestBuilder
        .withBody(VALID_XML)
        .withHeaders(new MultiMap<>(Collections.singletonMap("Content-Type", "application/xml")))
        .build()
        .validateRequest();
  }


  @Test
  public void invalidXMLWithSchemaAndWithoutMimeTypeTest() throws Exception {
    if (parser.name().equals("RAML")) {
      return;
    }
    withSchemaRequestBuilder
        .withBody(INVALID_XML)
        .withHeaders(new MultiMap<>(Collections.singletonMap("Content-Type", "application/xml")))
        .build()
        .assertThrows(BadRequestException.class, "The content of element 'User' is not complete");
  }

  /**
   * When no schema is defined anything should be valid
   **/
  @Test
  public void withoutSchemaAndWithoutMimeTypeTest() throws Exception {
    if (parser.name().equals("RAML")) {
      return;
    }
    TestRestRequestValidatorBuilder withoutSchemaRequestBuilder =
        testRestRequestValidatorBuilder.withApiLocation("unit/validation/oas20-payload-without-mimetype.yaml")
            .withRequestPath("/api/withoutSchema")
            .withRelativePath("/withoutSchema")
            .withMethod("POST");

    withoutSchemaRequestBuilder
        .withBody(INVALID_JSON)
        .build()
        .validateRequest();
    withoutSchemaRequestBuilder
        .withBody(VALID_JSON)
        .build()
        .validateRequest();
    withoutSchemaRequestBuilder
        .withBody(INVALID_XML)
        .build()
        .validateRequest();
    withoutSchemaRequestBuilder
        .withBody(VALID_XML)
        .build()
        .validateRequest();
    withoutSchemaRequestBuilder
        .withBody("any")
        .build()
        .validateRequest();
  }

}
