/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation;

import org.junit.Test;
import org.mule.module.apikit.api.exception.InvalidUriParameterException;
import org.mule.module.apikit.api.exception.MuleRestException;
import org.mule.module.apikit.api.validation.ValidRequest;
import org.mule.runtime.api.util.MultiMap;

import java.io.InputStream;
import java.nio.charset.Charset;

import static org.apache.commons.io.IOUtils.toInputStream;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mule.module.apikit.helpers.PayloadHelper.makePayloadRepeatable;
import static org.mule.parser.service.ParserMode.RAML;

public class UriParametersOverridingTestCase extends AbstractRequestValidatorTestCase {

  private static final String RAML08_API_LOCATION = "unit/uri-parameters/raml08/api.raml";
  private static final String RAML10_API_LOCATION = "unit/uri-parameters/raml10/api.raml";
  private static final String OAS20_API_LOCATION = "unit/uri-parameters/oas20/api.yaml";
  private static final String OAS30_API_LOCATION = "unit/uri-parameters/oas30/api.yaml";

  @Test
  public void noUriParamsOverridden() throws MuleRestException {
    testRestRequestValidatorBuilder
        .withRequestPath("/api/noUriParamsOverridden")
        .withRelativePath("/noUriParamsOverridden")
        .withMethod("GET");
    validateAllAPIs();
  }

  @Test
  public void noUriParamsOverriddenButRequired() throws MuleRestException {
    testRestRequestValidatorBuilder
        .withRequestPath("/api/noUriParamsButRequired/testValue")
        .withRelativePath("/noUriParamsButRequired/testValue")
        .withMethod("GET");
    validateRAMLAPIs();
  }

  @Test
  public void templateUriParamsInResource() throws MuleRestException {
    testRestRequestValidatorBuilder
        .withRequestPath("/api/templateUriParamsInResource/resource")
        .withRelativePath("/templateUriParamsInResource/resource")
        .withMethod("GET");
    validateAllAPIs();
  }

  @Test
  public void templateUriParamsInResourceInvalidValue() throws MuleRestException {
    testRestRequestValidatorBuilder
        .withRequestPath("/api/templateUriParamsInResource/invalidValue")
        .withRelativePath("/templateUriParamsInResource/invalidValue")
        .withMethod("GET");
    validateAllAPIsFail();
  }

  @Test
  public void validateGetWhenTemplateUriParamsInMethods() throws MuleRestException {
    testRestRequestValidatorBuilder
        .withRequestPath("/api/templateUriParamsInMethods/method-get")
        .withRelativePath("/templateUriParamsInMethods/method-get")
        .withMethod("GET");
    validateOASAPIs();
  }

  @Test
  public void templateUriParamsInMethodsInvalidValue() throws MuleRestException {
    testRestRequestValidatorBuilder
        .withRequestPath("/api/templateUriParamsInMethods/invalidValue")
        .withRelativePath("/templateUriParamsInMethods/invalidValue")
        .withMethod("GET");
    validateOASAPIsFail();
  }

  @Test
  public void validatePutWhenTemplateUriParamsInMethods() throws MuleRestException {
    MultiMap<String, String> headers = new MultiMap<>();
    headers.put("Content-Type", "application/json");
    testRestRequestValidatorBuilder
        .withRequestPath("/api/templateUriParamsInMethods/method-put")
        .withRelativePath("/templateUriParamsInMethods/method-put")
        .withMethod("PUT")
        .withHeaders(headers)
        .withBody((InputStream) makePayloadRepeatable(toInputStream("testValue", Charset.defaultCharset())));
    validateOASAPIs();
  }

  @Test
  public void templateUriParamsInResourceAndMethods() throws MuleRestException {
    testRestRequestValidatorBuilder
        .withRequestPath("/api/templateUriParamsInResourceAndMethods/method-get")
        .withRelativePath("/templateUriParamsInResourceAndMethods/method-get")
        .withMethod("GET");
    validateOASAPIs();
  }

  @Test
  public void templateUriParamsInResourceAndMethodsInvalidValue() throws MuleRestException {
    testRestRequestValidatorBuilder
        .withRequestPath("/api/templateUriParamsInResourceAndMethods/invalidValue")
        .withRelativePath("/templateUriParamsInResourceAndMethods/invalidValue")
        .withMethod("GET");
    validateOASAPIsFail();
  }

  @Test
  public void validateGetInTemplateUriParamsInResourceOverriddenInMethodPutOnly() throws MuleRestException {
    testRestRequestValidatorBuilder
        .withRequestPath("/api/templateUriParamsInResourceOverriddenInOneMethod/resource")
        .withRelativePath("/templateUriParamsInResourceOverriddenInOneMethod/resource")
        .withMethod("GET");
    validateOASAPIs();
  }

  @Test
  public void templateUriParamsInResourceOverriddenInMethodPutOnlyInvalidValue() throws MuleRestException {
    testRestRequestValidatorBuilder
        .withRequestPath("/api/templateUriParamsInResourceOverriddenInOneMethod/method-get")
        .withRelativePath("/templateUriParamsInResourceOverriddenInOneMethod/method-get")
        .withMethod("GET");
    validateOASAPIsFail();
  }

  @Test
  public void validatePutInTemplateUriParamsInResourceOverriddenInMethodPutOnly() throws MuleRestException {
    MultiMap<String, String> headers = new MultiMap<>();
    headers.put("Content-Type", "application/json");
    testRestRequestValidatorBuilder
        .withRequestPath("/api/templateUriParamsInResourceOverriddenInOneMethod/method-put")
        .withRelativePath("/templateUriParamsInResourceOverriddenInOneMethod/method-put")
        .withMethod("PUT")
        .withHeaders(headers)
        .withBody((InputStream) makePayloadRepeatable(toInputStream("testValue", Charset.defaultCharset())));
    validateOASAPIs();
  }


  private ValidRequest validateRequest(String apiLocation) throws MuleRestException {
    return testRestRequestValidatorBuilder
        .withApiLocation(apiLocation)
        .build()
        .validateRequest();
  }

  private void validateRequestFails(String apiLocation) throws MuleRestException {
    try {
      validateRequest(apiLocation);
      fail();
    } catch (InvalidUriParameterException e) {
      assertThat(e.getMessage(), allOf(containsString("Invalid value"), containsString(" for uri parameter testParam")));
    }
  }

  public void validateRAMLAPIs() throws MuleRestException {
    validateRequest(RAML08_API_LOCATION);
    validateRequest(RAML10_API_LOCATION);
  }

  public void validateRAMLAPIsFail() throws MuleRestException {
    validateRequestFails(RAML08_API_LOCATION);
    validateRequestFails(RAML10_API_LOCATION);
  }

  public void validateOASAPIs() throws MuleRestException {
    if (!RAML.equals(parser)) {
      validateRequest(OAS20_API_LOCATION);
      validateRequest(OAS30_API_LOCATION);
    }
  }

  public void validateOASAPIsFail() throws MuleRestException {
    if (!RAML.equals(parser)) {
      validateRequestFails(OAS20_API_LOCATION);
      validateRequestFails(OAS30_API_LOCATION);
    }
  }

  private void validateAllAPIs() throws MuleRestException {
    validateRAMLAPIs();
    validateOASAPIs();
  }

  private void validateAllAPIsFail() throws MuleRestException {
    validateRAMLAPIsFail();
    validateOASAPIsFail();
  }
}
