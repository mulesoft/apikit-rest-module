package org.mule.module.apikit.validation;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.extension.http.api.HttpRequestAttributesBuilder;
import org.mule.module.apikit.api.config.ValidationConfig;
import org.mule.parser.service.ParserMode;
import org.mule.runtime.api.util.MultiMap;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.apache.commons.lang3.StringUtils.isNoneBlank;
import static org.junit.Assert.assertNotNull;
import static org.mule.apikit.model.api.ApiReference.create;

public class TestRestRequestValidatorBuilder {

  private HttpRequestAttributesBuilder httpRequestAttributesBuilder;
  private String apiLocation;
  private String method;
  private String relativePath;
  private String requestPath;
  private String rawRequestPath;
  private String charset;
  private InputStream body;
  private ValidationConfig validationConfig;
  private ParserMode parser;

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

  public TestRestRequestValidatorBuilder withParser(ParserMode parser) {
    this.parser = parser;
    return this;
  }

  public TestRestRequestValidator build() {
    validateMandatory();
    setDefaults();
    HttpRequestAttributes httpRequestAttributes = httpRequestAttributesBuilder
      .method(method)
      .relativePath(relativePath)
      .requestPath(requestPath)
      .rawRequestPath(rawRequestPath)
      .build();
    return new TestRestRequestValidator(relativePath, parser, create(apiLocation), charset, body, httpRequestAttributes,
      validationConfig);
  }

  private void validateMandatory() {
    assertNotNull("API location is mandatory for the test to run", apiLocation);
    assertNotNull("Relative path is mandatory for the test to run", relativePath);
    assertNotNull("Method is mandatory for the test to run", method);
  }

  private void setDefaults() {
    this.parser = parser != null ? parser : ParserMode.AMF;
    this.requestPath = requestPath != null ? requestPath : "/api" + relativePath;
    this.rawRequestPath = isNoneBlank(rawRequestPath) ? rawRequestPath : requestPath;
  }
}
