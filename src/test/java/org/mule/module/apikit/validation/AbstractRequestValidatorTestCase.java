/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.extension.http.api.HttpRequestAttributesBuilder;
import org.mule.module.apikit.api.config.ValidationConfig;
import org.mule.parser.service.ParserMode;
import org.mule.runtime.api.util.MultiMap;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

import static org.apache.commons.lang3.StringUtils.isNoneBlank;
import static org.junit.Assert.assertNotNull;
import static org.mule.apikit.model.api.ApiReference.create;


/**
 * Template class for request validation against api specification
 */
@RunWith(Parameterized.class)
public abstract class AbstractRequestValidatorTestCase {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  protected TestRestRequestValidatorBuilder testRestRequestValidatorBuilder;

  @Parameter(0)
  public ParserMode parser;

  @Parameterized.Parameters(name = "{0}")
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
        {ParserMode.AMF},
        {ParserMode.RAML}
    });
  }

  @Before
  public void setup() {
    testRestRequestValidatorBuilder = new TestRestRequestValidatorBuilder();
  }

  protected class TestRestRequestValidatorBuilder {

    private HttpRequestAttributesBuilder httpRequestAttributesBuilder;
    private String apiLocation;
    private String method;
    private String relativePath;
    private String requestPath;
    private String rawRequestPath;
    private String charset;
    private InputStream body;
    private ValidationConfig validationConfig;

    public TestRestRequestValidatorBuilder() {
      httpRequestAttributesBuilder =
          new HttpRequestAttributesBuilder()
              .listenerPath("/api/*")
              .version("1")
              .scheme("http")
              .requestUri("/")
              .rawRequestUri("/")
              .localAddress("")
              .queryString("")
              .remoteAddress("");
    }

    public TestRestRequestValidatorBuilder withRelativePath(String relativePath) {
      this.relativePath = relativePath;
      return this;
    }

    public TestRestRequestValidatorBuilder withRequestPath(String requestPath) {
      this.requestPath = requestPath;
      return this;
    }

    public TestRestRequestValidatorBuilder withRawRequestPath(String rawRequestPath) {
      this.rawRequestPath = rawRequestPath;
      return this;
    }

    public TestRestRequestValidatorBuilder withMethod(String method) {
      this.method = method;
      return this;
    }

    public TestRestRequestValidatorBuilder withQueryParams(MultiMap<String, String> queryParams) {
      this.httpRequestAttributesBuilder.queryParams(queryParams);
      return this;
    }

    public TestRestRequestValidatorBuilder withQueryString(String queryString) {
      this.httpRequestAttributesBuilder.queryString(queryString);
      return this;
    }

    public TestRestRequestValidatorBuilder withUriParams(MultiMap<String, String> uriParams) {
      this.httpRequestAttributesBuilder.uriParams(uriParams);
      return this;
    }

    public TestRestRequestValidatorBuilder withListenerPath(String listenerPath) {
      this.httpRequestAttributesBuilder.listenerPath(listenerPath);
      return this;
    }

    public TestRestRequestValidatorBuilder withHeaders(MultiMap<String, String> headers) {
      this.httpRequestAttributesBuilder.headers(headers);
      return this;
    }

    public TestRestRequestValidatorBuilder withApiLocation(String apiLocation) {
      this.apiLocation = apiLocation;
      return this;
    }

    public TestRestRequestValidatorBuilder withValidationConfig(ValidationConfig validationConfig) {
      this.validationConfig = validationConfig;
      return this;
    }

    public TestRestRequestValidatorBuilder withBody(InputStream body) {
      this.body = body;
      return this;
    }

    public TestRestRequestValidatorBuilder withBody(String body) {
      this.body = new ByteArrayInputStream(body.getBytes());
      return this;
    }

    public TestRestRequestValidatorBuilder withCharset(String payloadCharset) {
      this.charset = payloadCharset;
      return this;
    }

    public TestRestRequestValidator build() {
      validateMandatory();
      HttpRequestAttributes httpRequestAttributes = httpRequestAttributesBuilder
          .method(method)
          .relativePath(relativePath)
          .requestPath(requestPath)
          .rawRequestPath(isNoneBlank(rawRequestPath) ? rawRequestPath : relativePath)
          .build();
      return new TestRestRequestValidator(relativePath, parser, create(apiLocation), charset, body, httpRequestAttributes,
                                          validationConfig);
    }

    private void validateMandatory() {
      assertNotNull("API location is mandatory for the test to run", apiLocation);
      assertNotNull("Request path is mandatory for the test to run", requestPath);
      assertNotNull("Relative path is mandatory for the test to run", relativePath);
      assertNotNull("Method is mandatory for the test to run", method);
    }
  }

}
