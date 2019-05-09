/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.schema.v2;

import org.mule.module.apikit.api.exception.BadRequestException;
import org.mule.module.apikit.validation.body.schema.IRestSchemaValidatorStrategy;
import org.mule.apikit.model.MimeType;
import org.mule.apikit.validation.ApiValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RestSchemaV2Validator implements IRestSchemaValidatorStrategy {

  protected static final Logger logger = LoggerFactory.getLogger(RestSchemaV2Validator.class);
  private MimeType mimeType;

  public RestSchemaV2Validator(MimeType mimeType) {
    this.mimeType = mimeType;
  }

  public void validate(String payload) throws BadRequestException {
    final List<ApiValidationResult> validationResults = mimeType.validate(payload);

    if (!validationResults.isEmpty()) {
      String logMessage = validationResults.get(0).getMessage();
      logger.info("Schema validation failed: " + logMessage);
      throw new BadRequestException(logMessage);
    }
  }
}
