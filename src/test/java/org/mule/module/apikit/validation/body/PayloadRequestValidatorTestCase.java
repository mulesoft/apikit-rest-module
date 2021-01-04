/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mule.module.apikit.api.exception.BadRequestException;
import org.mule.module.apikit.api.exception.MuleRestException;
import org.mule.module.apikit.validation.AbstractRequestValidatorTestCase;
import org.mule.runtime.api.util.MultiMap;

import java.io.ByteArrayInputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class PayloadRequestValidatorTestCase extends AbstractRequestValidatorTestCase {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void xmlPayloadValidationForUnionType() throws MuleRestException {
    validationRequest("application/xml", "/api/xmlUnion", "/xmlUnion", "<dummy1><message1>test</message1></dummy1>");
    validationRequest("application/xml", "/api/xmlUnion", "/xmlUnion", "<dummy2><message2>test</message2></dummy2>");
  }

  @Test
  public void invalidXmlPayloadValidationForUnionType() throws MuleRestException {
    expectedException.expect(BadRequestException.class);
    validationRequest("application/xml", "/api/xmlUnion", "/xmlUnion", "<dummy3><message3>test</message3></dummy3>");
  }

  @Test
  public void jsonPayloadValidationForUnionType() throws MuleRestException {
    validationRequest("application/json", "/api/jsonUnion", "/jsonUnion", "{\"message1\": \"test\"}");
    validationRequest("application/json", "/api/jsonUnion", "/jsonUnion", "{\"message2\": \"test\"}");
  }

  @Test
  public void invalidJsonPayloadValidationForUnionType() throws MuleRestException {
    expectedException.expect(BadRequestException.class);
    validationRequest("application/json", "/api/jsonUnion", "/jsonUnion", "{\"message3\": \"test\"}");
  }

  private void validationRequest(String contentType, String requestPath, String relativePath, String body)
      throws MuleRestException {
    MultiMap<String, String> headers = new MultiMap<>();
    headers.put("Content-Type", contentType);
    testRestRequestValidatorBuilder
        .withApiLocation("unit/payload/api.raml")
        .withMethod("POST")
        .withRequestPath(requestPath)
        .withRelativePath(relativePath)
        .withBody(new ByteArrayInputStream(body.getBytes(UTF_8)))
        .withHeaders(headers)
        .build()
        .validateRequest();
  }

}
