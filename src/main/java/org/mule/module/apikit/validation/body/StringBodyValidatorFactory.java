/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body;

import static java.lang.String.format;

import java.util.concurrent.ExecutionException;
import org.mule.apikit.model.Action;
import org.mule.apikit.model.MimeType;
import org.mule.module.apikit.api.config.ValidationConfig;
import org.mule.module.apikit.api.exception.BadRequestException;
import org.mule.module.apikit.api.validation.ApiKitJsonSchema;
import org.mule.module.apikit.validation.body.schema.v1.RestJsonSchemaValidator;
import org.mule.module.apikit.validation.body.schema.v1.RestXmlSchemaValidator;
import org.mule.module.apikit.validation.body.schema.v1.cache.SchemaCacheUtils;
import org.mule.module.apikit.validation.body.schema.v2.RestSchemaV2Validator;
import org.mule.runtime.api.exception.ErrorTypeRepository;

public class StringBodyValidatorFactory {

  private final ValidationConfig config;
  private final ErrorTypeRepository errorTypeRepository;

  public StringBodyValidatorFactory(ValidationConfig config,
      ErrorTypeRepository errorTypeRepository) {
    this.config = config;
    this.errorTypeRepository = errorTypeRepository;
  }

  public PayloadValidator createValidator(String requestMimeTypeName, Action action,
      MimeType mimeType) throws BadRequestException {
    if (config.isParserV2()) {
      return new RestSchemaV2Validator(mimeType, requestMimeTypeName);
    }
    try {
      String schemaPath = SchemaCacheUtils.getSchemaCacheKey(action, requestMimeTypeName);
      if (requestMimeTypeName.contains("json")) {
        ApiKitJsonSchema schema = config.getJsonSchema(schemaPath);
        return new RestJsonSchemaValidator(schema != null ? schema.getSchema() : null, requestMimeTypeName);
      }
      if (requestMimeTypeName.contains("xml")) {
        return new RestXmlSchemaValidator(config.getXmlSchema(schemaPath), errorTypeRepository, requestMimeTypeName);
      }
      throw new BadRequestException(format("Unexpected Mime Type %s", requestMimeTypeName));
    } catch (ExecutionException e) {
      throw new BadRequestException(e);
    }
  }

}
