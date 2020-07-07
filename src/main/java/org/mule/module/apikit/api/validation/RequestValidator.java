/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.api.validation;

import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.module.apikit.api.config.ValidationConfig;
import org.mule.module.apikit.api.exception.MuleRestException;
import org.mule.module.apikit.api.uri.ResolvedVariables;
import org.mule.apikit.model.Resource;
import org.mule.module.apikit.validation.RestRequestValidator;
import org.mule.runtime.api.exception.ErrorTypeRepository;
import org.mule.runtime.api.exception.MuleRuntimeException;

public class RequestValidator {

  private RequestValidator() {}

  public static ValidRequest validate(ValidationConfig config, Resource resource, HttpRequestAttributes attributes,
                                      ResolvedVariables resolvedVariables, Object payload,
                                      ErrorTypeRepository errorTypeRepository)
      throws MuleRestException {

    return validate(config, resource, attributes, resolvedVariables, payload, null, errorTypeRepository);

  }

  public static ValidRequest validate(ValidationConfig config, Resource resource, HttpRequestAttributes attributes,
                                      ResolvedVariables resolvedVariables, Object payload, String charset,
                                      ErrorTypeRepository errorTypeRepository)
      throws MuleRestException {

    if (resource == null) {
      throw new MuleRuntimeException(
                                     createStaticMessage("Unexpected error. Resource cannot be null"));
    }

    return new RestRequestValidator(config, resource, errorTypeRepository)
        .validate(resolvedVariables, attributes, charset,
                  payload);

  }
}
