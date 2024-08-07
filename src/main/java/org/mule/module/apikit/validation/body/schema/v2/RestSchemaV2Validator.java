/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.schema.v2;

import org.mule.apikit.model.MimeType;
import org.mule.apikit.validation.ApiValidationResult;
import org.mule.module.apikit.api.exception.BadRequestException;
import org.mule.module.apikit.validation.body.schema.IRestSchemaValidatorStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;

import static java.util.stream.Collectors.joining;

public class RestSchemaV2Validator implements IRestSchemaValidatorStrategy {

  protected static final Logger logger = LoggerFactory.getLogger(RestSchemaV2Validator.class);
  private MimeType mimeType;

  public RestSchemaV2Validator(MimeType mimeType) {
    this.mimeType = mimeType;
  }

  public void validate(String payload) throws BadRequestException {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Future<List<ApiValidationResult>> future = executor.submit(() -> mimeType.validate(payload));

    try {
      final List<ApiValidationResult> validationResults = future.get(2, TimeUnit.SECONDS);
      if (!validationResults.isEmpty()) {
        throw new BadRequestException(buildLogMessage(validationResults));
      }
    } catch (TimeoutException e) {
      throw new BadRequestException("Validation timed out after 2 seconds");
    } catch (InterruptedException | ExecutionException e) {
      throw new BadRequestException("Error during validation: " + e.getMessage());
    } finally {
      executor.shutdownNow();
    }
  }

  private String buildLogMessage(List<ApiValidationResult> validationResults) {
    return validationResults.stream().map(result -> result.getMessage().replace("\n", "")).collect(joining("\n"));
  }
}
