/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.schema.v2;

import static java.util.stream.Collectors.joining;

import org.mule.module.apikit.api.exception.BadRequestException;
import org.mule.module.apikit.api.validation.ValidBody;
import org.mule.apikit.model.MimeType;
import org.mule.apikit.validation.ApiValidationResult;
import org.mule.module.apikit.validation.body.schema.RestSchemaValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RestSchemaV2Validator extends RestSchemaValidator {

  protected static final Logger logger = LoggerFactory.getLogger(RestSchemaV2Validator.class);
  private MimeType mimeType;

  public RestSchemaV2Validator(MimeType mimeType, String requestMimeType) {
    super(requestMimeType);
    this.mimeType = mimeType;
  }

  @Override
  public ValidBody validate(String payload) throws BadRequestException {
    List<ApiValidationResult> validationResults = mimeType.validate(payload);
    if (!validationResults.isEmpty()) {
      throw new BadRequestException(buildLogMessage(validationResults));
    }
    return new ValidBody(payload);
  }

  private String buildLogMessage(List<ApiValidationResult> validationResults) {
    return validationResults.stream().map(result -> result.getMessage().replace("\n", "")).collect(joining("\n"));
  }
}
