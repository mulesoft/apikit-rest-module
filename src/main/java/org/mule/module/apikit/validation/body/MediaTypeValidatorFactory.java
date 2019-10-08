/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body;

import java.util.Arrays;
import java.util.List;
import org.mule.apikit.model.Action;
import org.mule.apikit.model.MimeType;
import org.mule.module.apikit.api.config.ValidationConfig;
import org.mule.module.apikit.api.exception.BadRequestException;
import org.mule.runtime.api.exception.ErrorTypeRepository;

public class MediaTypeValidatorFactory {

  private final ValidationConfig config;
  private final ErrorTypeRepository errorTypeRepository;
  private static final List<String> validateAsString = Arrays.asList("json", "xml");
  private static final List<String> validateAsForm = Arrays
      .asList("multipart/", "application/x-www-form-urlencoded");


  public MediaTypeValidatorFactory(ValidationConfig config,
      ErrorTypeRepository errorTypeRepository) {
    this.config = config;
    this.errorTypeRepository = errorTypeRepository;
  }

  public PayloadValidator getPayloadValidator(String requestMimeType, Action action,
      MimeType mimeType) throws BadRequestException {
    if (validateAsString.stream().anyMatch(format -> requestMimeType.contains(format))) {
      return new StringBodyValidatorFactory(config, errorTypeRepository)
          .createValidator(requestMimeType, action, mimeType);
    }

    if (validateAsForm.stream().anyMatch(format -> requestMimeType.contains(format))) {
      return new FormValidatorFactory(this.config.isParserV2(), this.config.getExpressionManager())
          .createValidator(requestMimeType, mimeType);
    }
    return null;
  }


}
