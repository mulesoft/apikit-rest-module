/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation;

import java.util.Map.Entry;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.module.apikit.api.config.ValidationConfig;
import org.mule.module.apikit.api.exception.BadRequestException;
import org.mule.module.apikit.api.validation.ValidBody;
import org.mule.module.apikit.exception.UnsupportedMediaTypeException;
import org.mule.module.apikit.validation.body.MediaTypeValidatorFactory;
import org.mule.module.apikit.validation.body.PayloadValidator;
import org.mule.apikit.model.Action;
import org.mule.apikit.model.MimeType;
import org.mule.runtime.api.exception.ErrorTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mule.module.apikit.helpers.AttributesHelper.getMediaType;

public class BodyValidator {

  protected final static Logger logger = LoggerFactory.getLogger(BodyValidator.class);

  public static ValidBody validate(Action action, HttpRequestAttributes attributes, Object payload,
                                   ValidationConfig config, String charset, ErrorTypeRepository errorTypeRepository)
      throws BadRequestException, UnsupportedMediaTypeException {
    if (action == null || !action.hasBody()) {
      logger.debug("=== no body types defined: accepting any request content-type");
      return new ValidBody(payload);
    }

    String requestMimeTypeName = getMediaType(attributes);

    Entry<String, MimeType> foundMimeType = action.getBody().entrySet().stream()
        .filter(entry -> getMediaType(entry.getKey()).equals(requestMimeTypeName))
        .findFirst()
        .orElseThrow(UnsupportedMediaTypeException::new);

    MimeType mimeType = foundMimeType.getValue();

    PayloadValidator payloadValidator = new MediaTypeValidatorFactory(config, errorTypeRepository)
        .getPayloadValidator(requestMimeTypeName.toLowerCase(), action, mimeType);
    if (payloadValidator == null) {
      return new ValidBody(payload);
    }
    return payloadValidator.validate(payload, charset);
  }
}
