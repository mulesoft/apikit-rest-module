/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.mule.apikit.model.Resource;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.extension.http.api.HttpRequestAttributesBuilder;
import org.mule.module.apikit.api.RoutingTable;
import org.mule.module.apikit.api.config.ValidationConfig;
import org.mule.module.apikit.api.exception.MuleRestException;
import org.mule.module.apikit.api.parsing.AttributesDeserializingStrategy;
import org.mule.module.apikit.api.parsing.AttributesDeserializingStrategyIdentifier;
import org.mule.module.apikit.api.uri.URIPattern;
import org.mule.module.apikit.api.uri.URIResolver;
import org.mule.module.apikit.api.uri.URIResolver.MatchRule;
import org.mule.module.apikit.api.validation.ApiKitJsonSchema;
import org.mule.module.apikit.api.validation.ValidRequest;
import org.mule.parser.service.ParserMode;
import org.mule.parser.service.ParserService;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.core.api.el.ExpressionManager;

import javax.xml.validation.Schema;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

import static org.mule.apikit.model.api.ApiReference.create;


/**
 * Template class for request validation against api specification
 */
@RunWith(Parameterized.class)
public abstract class AbstractRequestValidatorTestCase {

  @Parameter(0)
  public ParserMode parser;


  @Parameterized.Parameters(name = "{0}")
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
        {ParserMode.AMF},
        {ParserMode.RAML}
    });
  }

  /**
   * @return complete path
   */
  protected abstract String getPath();

  /**
   * @return path relative to http.listener path attribute
   */
  protected abstract String getRelativePath();

  /**
   * @return HTTP request method
   */
  protected abstract String getMethod();

  /**
   * @return HTTP request query params
   */
  protected MultiMap<String, String> getQueryParams() {
    return new MultiMap<>();
  }

  /**
   * @return HTTP request uri params, always empty, uri params are resolved
   * by apikit using api specification
   */
  private MultiMap<String, String> getUriParams() {
    return new MultiMap<>();
  }

  /**
   * @return HTTP request headers to be validated
   */
  protected MultiMap<String, String> getHeaders() {
    return new MultiMap<>();
  }

  /**
   * @return path to api definition
   */
  protected abstract String getApiLocation();

  /**
   * @return default ValidationConfig interface implementation, override this method if needed
   */
  protected ValidationConfig getValidationConfig() {
    return new ValidationConfig() {

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
      public AttributesDeserializingStrategy getAttributesDeserializingStrategy(AttributesDeserializingStrategyIdentifier identifier) {
        return null;
      }
    };
  }

  /**
   * @return body to be validated
   */
  protected abstract InputStream getBody();

  /**
   * @return body charset
   */
  protected String getCharset() {
    return null;
  }

  /**
   * @return Execute RestRequestValidator.validate method
   * @throws MuleRestException when request is invalid
   */
  protected ValidRequest validateRequest() throws MuleRestException {
    HttpRequestAttributes attributes =
        new HttpRequestAttributesBuilder()
            .headers(getHeaders())
            .listenerPath("/api/*")
            .method(getMethod())
            .version("1")
            .scheme("http")
            .relativePath(getPath())
            .requestPath(getPath())
            .rawRequestPath(getPath())
            .requestUri("/")
            .rawRequestUri("/")
            .localAddress("")
            .queryString("")
            .queryParams(getQueryParams())
            .uriParams(getUriParams())
            .remoteAddress("")
            .build();

    RoutingTable routingTable = new RoutingTable(
                                                 new ParserService()
                                                     .parse(create(getApiLocation()), parser)
                                                     .get());

    URIResolver uriResolver = new URIResolver(getRelativePath());
    URIPattern pattern = uriResolver.find(routingTable.keySet(), MatchRule.BEST_MATCH);
    Resource resource = routingTable.getResource(pattern);

    RestRequestValidator requestValidator = new RestRequestValidator(getValidationConfig(), resource, null);

    return requestValidator.validate(uriResolver.resolve(pattern), attributes, getCharset(), getBody());
  }

}
