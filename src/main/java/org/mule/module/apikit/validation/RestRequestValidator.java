/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation;

import static java.lang.String.format;

import java.util.HashMap;
import java.util.Map;
import org.mule.apikit.model.Action;
import org.mule.apikit.model.Resource;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.extension.http.api.HttpRequestAttributesBuilder;
import org.mule.module.apikit.api.config.ValidationConfig;
import org.mule.module.apikit.api.exception.MethodNotAllowedException;
import org.mule.module.apikit.api.exception.MuleRestException;
import org.mule.module.apikit.api.uri.ResolvedVariables;
import org.mule.module.apikit.api.validation.ValidBody;
import org.mule.module.apikit.api.validation.ValidRequest;
import org.mule.runtime.api.exception.ErrorTypeRepository;

public class RestRequestValidator {

  private final ValidationConfig config;
  private final Resource resource;
  private final ErrorTypeRepository errorTypeRepository;
  private static final HttpMethodValidator methodValidator = new HttpMethodValidator();

  public RestRequestValidator(ValidationConfig config, Resource resource, ErrorTypeRepository errorTypeRepository) {
    this.config = config;
    this.resource = resource;
    this.errorTypeRepository = errorTypeRepository;
  }

  public ValidRequest validate(ResolvedVariables uriParams, HttpRequestAttributes attributes,
                               String payloadCharset, Object body)
      throws MuleRestException {

    String method = attributes.getMethod().toLowerCase();
    methodValidator.validateHttpMethod(method);
    Action action = resource.getAction(method);

    if (action == null) {
      throw new MethodNotAllowedException(
                                          format("HTTP Method %s not allowed for : %s", method, resource.getUri()));
    }

    if (config.isDisableValidations()) {
      return ValidRequest.builder()
          .withAttributes(addUriParams(uriParams, attributes))
          .withBody(new ValidBody(body))
          .build();
    }

    HttpRequestAttributes validAttributes = AttributesValidator.validateAndAddDefaults(attributes, action, uriParams, config);
    ValidBody validBody = BodyValidator.validate(action, attributes, body, config, payloadCharset, errorTypeRepository);
    return ValidRequest.builder()
        .withAttributes(validAttributes)
        .withBody(validBody)
        .build();
  }

  private HttpRequestAttributes addUriParams(ResolvedVariables uriParams, HttpRequestAttributes attributes) {
    final Map<String, String> uriParamsMap = new HashMap<>();
    uriParams.names().forEach(name -> uriParamsMap.put(name, String.valueOf(uriParams.get(name))));
    return new HttpRequestAttributesBuilder(attributes).uriParams(uriParamsMap).build();
  }
}
