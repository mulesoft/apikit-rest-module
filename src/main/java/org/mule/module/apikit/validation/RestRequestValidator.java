/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.http.impl.client.HttpRequestFutureTask;
import org.mule.apikit.model.Action;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.extension.http.api.HttpRequestAttributesBuilder;
import org.mule.module.apikit.api.config.ValidationConfig;
import org.mule.module.apikit.api.exception.ApikitRuntimeException;
import org.mule.module.apikit.api.exception.BadRequestException;
import org.mule.module.apikit.api.exception.MuleRestException;
import org.mule.module.apikit.api.uri.ResolvedVariables;
import org.mule.module.apikit.api.validation.ValidBody;
import org.mule.module.apikit.api.validation.ValidRequest;
import org.mule.runtime.api.exception.ErrorTypeRepository;
import org.mule.runtime.api.exception.TypedException;

public class RestRequestValidator {

  private final ValidationConfig config;
  private final Action action;
  private final ErrorTypeRepository errorTypeRepository;

  public RestRequestValidator(ValidationConfig config, Action action, ErrorTypeRepository errorTypeRepository) {
    this.config = config;
    this.action = action;
    this.errorTypeRepository = errorTypeRepository;
  }

  public ValidRequest validate(ResolvedVariables uriParams, HttpRequestAttributes attributes,
                               String payloadCharset, Object body)
      throws MuleRestException {
    try {

      if (config.isDisableValidations()) {
        return ValidRequest.builder()
            .withAttributes(addUriParams(uriParams, attributes))
            .withBody(new ValidBody(body))
            .build();
      }

      Future<HttpRequestAttributes> validateAttributes = config.getScheduler().submit(
                                                                                      () -> AttributesValidator
                                                                                          .validateAndAddDefaults(attributes,
                                                                                                                  action,
                                                                                                                  uriParams,
                                                                                                                  config));
      Future<ValidBody> validateBody = config.getScheduler().submit(
                                                                    () -> BodyValidator
                                                                        .validate(action, attributes, body, config,
                                                                                  payloadCharset, errorTypeRepository));
      return ValidRequest.builder()
          .withAttributes(validateAttributes.get())
          .withBody(validateBody.get())
          .build();
    } catch (InterruptedException e) {
      throw new ApikitRuntimeException(e);
    } catch (ExecutionException ee) {
      if (ee.getCause() instanceof MuleRestException) {
        throw (MuleRestException) ee.getCause();
      }
      throw (MuleRestException) ee.getCause().getCause();
    }
  }

  private HttpRequestAttributes addUriParams(ResolvedVariables uriParams, HttpRequestAttributes attributes) {
    final Map<String, String> uriParamsMap = new HashMap<>();
    uriParams.names().stream().forEach(name -> uriParamsMap.put(name, String.valueOf(uriParams.get(name))));
    return new HttpRequestAttributesBuilder(attributes).uriParams(uriParamsMap).build();
  }
}
