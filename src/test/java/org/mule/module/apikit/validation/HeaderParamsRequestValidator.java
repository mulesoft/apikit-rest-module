/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.mule.module.apikit.api.exception.MuleRestException;
import org.mule.module.apikit.api.validation.ValidRequest;
import org.mule.module.apikit.exception.NotAcceptableException;
import org.mule.runtime.api.util.MultiMap;

import java.io.InputStream;
import java.nio.charset.Charset;

import static org.apache.commons.io.IOUtils.toInputStream;
import static org.mule.module.apikit.helpers.PayloadHelper.makePayloadRepeatable;

public class HeaderParamsRequestValidator extends AbstractRequestValidatorTestCase {

  private void validateRequestForAcceptHeader(String acceptHeaderValue) throws MuleRestException {
    validateRequestForAcceptHeader(acceptHeaderValue, "/testMimeTypes");
  }

  private void validateRequestForAcceptHeader(String acceptHeaderValue, String relativePath) throws MuleRestException {
    MultiMap<String, String> headers = new MultiMap<>();
    headers.put("Content-Type", "application/json");
    headers.put("Accept", acceptHeaderValue);
    testRestRequestValidatorBuilder
        .withApiLocation("unit/validation/mime-types-api.raml")
        .withRelativePath(relativePath)
        .withMethod("POST")
        .withHeaders(headers)
        .withBody((InputStream) makePayloadRepeatable(toInputStream("{\"message\":\"All Ok\"}", Charset.defaultCharset())))
        .build()
        .validateRequest();
  }

  @Test
  public void nullSubTypeThrowsNotAcceptableException() throws MuleRestException {
    expectedException.expect(NotAcceptableException.class);
    validateRequestForAcceptHeader("application/");
  }

  @Test
  public void successWithNullSubTypeWoSlash() throws MuleRestException {
    validateRequestForAcceptHeader("application");
  }

  @Test
  public void nullTypeAndSubTypeThrowsNotAcceptableException() throws MuleRestException {
    expectedException.expect(NotAcceptableException.class);
    validateRequestForAcceptHeader("/");
  }

  @Test
  public void nullTypeThrowsNotAcceptableException() throws MuleRestException {
    expectedException.expect(NotAcceptableException.class);
    validateRequestForAcceptHeader("/json");
  }

  @Test
  public void invalidTypeThrowsNotAcceptableException() throws MuleRestException {
    expectedException.expect(NotAcceptableException.class);
    validateRequestForAcceptHeader("application/xml");
  }

  @Test
  public void successWithValidAcceptHeaderValue() throws MuleRestException {
    validateRequestForAcceptHeader("application/json");
  }

  @Test
  public void successWithValidAcceptHeaderValueWildcardAccept() throws MuleRestException {
    validateRequestForAcceptHeader("application/json", "/testMimeTypesWildcard");
  }

  @Test
  public void successWithValidAcceptWildcardHeaderValueWildcardAccept() throws MuleRestException {
    validateRequestForAcceptHeader("*/*", "/testMimeTypesWildcard");
  }

  @Test
  public void successWithNullSubTypeWoSlashWildcardAccept() throws MuleRestException {
    validateRequestForAcceptHeader("application", "/testMimeTypesWildcard");
  }

  @Test
  public void successWithValidHeaderNameCaseInsensitivity() throws MuleRestException {
    MultiMap<String, String> headers = new MultiMap<>();
    headers.put("camelcasearray", "arrayValue");
    headers.put("camelcasestring", "stringValue");
    headers.put("smallcasearray", "arrayValue");
    headers.put("smallcasestring", "stringValue");
    MultiMap<String, String> validatedHeaders = validateRequestForArrayTypeHeader(headers).getAttributes().getHeaders();
    assertEquals(validatedHeaders.size(), 4);
    // Case Insensitivity works during header extraction
    assertTrue(validatedHeaders.containsKey("camelcasearray"));
    assertTrue(validatedHeaders.containsKey("camelcasestring"));
    assertTrue(validatedHeaders.containsKey("smallcasearray"));
    assertTrue(validatedHeaders.containsKey("smallcasestring"));

    assertTrue(validatedHeaders.containsKey("camelCaseArray"));
    assertTrue(validatedHeaders.containsKey("Smallcasestring"));
    assertTrue(validatedHeaders.containsKey("smallcaseArray"));
  }

  private ValidRequest validateRequestForArrayTypeHeader(MultiMap<String, String> headers) throws MuleRestException {

    return testRestRequestValidatorBuilder
        .withApiLocation("unit/validation/header-types-api.raml")
        .withRelativePath("/headerTypes")
        .withMethod("GET")
        .withHeaders(headers)
        .withBody((InputStream) makePayloadRepeatable(toInputStream("{\"message\":\"All Ok\"}", Charset.defaultCharset())))
        .build()
        .validateRequest();
  }
}
