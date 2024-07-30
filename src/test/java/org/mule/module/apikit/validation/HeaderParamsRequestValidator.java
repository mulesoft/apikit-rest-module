/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation;

import org.junit.Test;
import org.mule.module.apikit.api.exception.MuleRestException;
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
  public void successWithNullSubTypeWoSlashWildcardAccept() throws MuleRestException {
    validateRequestForAcceptHeader("application", "/testMimeTypesWildcard");
  }

  @Test
  public void nullSubTypeThrowsNotAcceptableExceptionWildcardAccept() throws MuleRestException {
    expectedException.expect(NotAcceptableException.class);
    validateRequestForAcceptHeader("application/", "/testMimeTypesWildcard");
  }

  @Test
  public void nullTypeThrowsNotAcceptableExceptionWithWildcardAccept() throws MuleRestException {
    expectedException.expect(NotAcceptableException.class);
    validateRequestForAcceptHeader("/json", "/testMimeTypesWildcard");
  }
}
