/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation;

import org.junit.Assert;
import org.mule.apikit.model.Resource;
import org.mule.apikit.model.api.ApiReference;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.module.apikit.api.RoutingTable;
import org.mule.module.apikit.api.config.ValidationConfig;
import org.mule.module.apikit.api.deserializing.AttributesDeserializingStrategies;
import org.mule.module.apikit.api.exception.MuleRestException;
import org.mule.module.apikit.api.uri.ResolvedVariables;
import org.mule.module.apikit.api.uri.URIPattern;
import org.mule.module.apikit.api.uri.URIResolver;
import org.mule.module.apikit.api.validation.ApiKitJsonSchema;
import org.mule.module.apikit.api.validation.ValidRequest;
import org.mule.parser.service.ParserMode;
import org.mule.parser.service.ParserService;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.core.api.el.ExpressionManager;

import javax.xml.validation.Schema;
import java.util.OptionalLong;

import static org.junit.Assert.assertTrue;

public class TestRestRequestValidator {

  private String relativePath;
  private ParserMode parser;
  private ApiReference apiReference;
  private String charset;
  private Object body;
  private HttpRequestAttributes httpRequestAttributes;
  private ValidationConfig validationConfig;

  public TestRestRequestValidator(String relativePath, ParserMode parser, ApiReference apiReference, String charset,
                                  Object body, HttpRequestAttributes httpRequestAttributes,
                                  ValidationConfig validationConfig) {
    this.relativePath = relativePath;
    this.parser = parser;
    this.apiReference = apiReference;
    this.charset = charset;
    this.body = body;
    this.httpRequestAttributes = httpRequestAttributes;
    this.validationConfig = validationConfig != null ? validationConfig : new ValidationConfig() {

      @Override
      public boolean isParserV2() {
        return true;
      }

      @Override
      public ApiKitJsonSchema getJsonSchema(String schemaPath) {
        return null;
      }

      @Override
      public Schema getXmlSchema(String schemaPath) {
        return null;
      }

      @Override
      public ExpressionManager getExpressionManager() {
        return null;
      }

      @Override
      public AttributesDeserializingStrategies getAttributesDeserializingStrategies() {
        return null;
      }
    };
  }

  /**
   * @return Executes {@link RestRequestValidator#validate(ResolvedVariables, HttpRequestAttributes, String, Object)}
   * @throws MuleRestException when request is invalid
   */
  public ValidRequest validateRequest() throws MuleRestException {
    URIResolver uriResolver = new URIResolver(relativePath);
    RoutingTable routingTable = new RoutingTable(
                                                 new ParserService()
                                                     .parse(apiReference, parser)
                                                     .get());
    URIPattern pattern = uriResolver.find(routingTable.keySet(), URIResolver.MatchRule.BEST_MATCH);
    Resource resource = routingTable.getResource(pattern);
    RestRequestValidator requestValidator = new RestRequestValidator(validationConfig, resource, null);
    return requestValidator.validate(uriResolver.resolve(pattern), httpRequestAttributes, charset, body);
  }

  /**
   * Assert if exception {@param expectedThrowable} is thrown with a message containing {@param containsMessage} when validating a
   * given request.
   *
   * @param expectedThrowable expected throwable exception of class {@param T}
   * @param containsMessage if not null it will validate that it is contained at the exception error message
   */
  public <T extends Throwable> void assertThrows(Class<T> expectedThrowable, String containsMessage) {
    Throwable exception = Assert.assertThrows(expectedThrowable, () -> validateRequest());
    if (containsMessage != null) {
      String actual = exception.getMessage();
      assertTrue(actual.contains(containsMessage));
    }
  }

  public OptionalLong getRequestBodyLength() {
    return body instanceof TypedValue ? ((TypedValue) body).getByteLength() : OptionalLong.empty();
  }

  public RoutingTable getRoutingTable() {
    return (apiReference != null && parser != null) ?
            new RoutingTable(new ParserService().parse(apiReference, parser).get()) : null;
  }
}
