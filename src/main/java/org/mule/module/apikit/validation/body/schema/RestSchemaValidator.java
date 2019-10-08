/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.schema;

import static java.lang.String.format;
import static org.mule.module.apikit.helpers.PayloadHelper.getPayloadAsString;
import static org.mule.module.apikit.helpers.PayloadHelper.getPayloadValue;

import org.mule.module.apikit.api.exception.BadRequestException;
import org.mule.module.apikit.api.validation.ValidBody;
import org.mule.module.apikit.validation.body.PayloadValidator;

public abstract class RestSchemaValidator implements PayloadValidator {

  private final String requestMimeType;

  public RestSchemaValidator(String requestMimeType) {
    this.requestMimeType = requestMimeType;
  }

  @Override
  public ValidBody validate(Object payload, String charset) throws BadRequestException {
    String payloadAsString = getPayloadAsString(getPayloadValue(payload), charset);
    String bodyType = getPayloadType(payloadAsString);
    if (!requestMimeType.toLowerCase().contains(bodyType)) {
      throw new BadRequestException(format("Request Mime Type : %s, body Type %s", requestMimeType, bodyType));
    }
    return validate(payloadAsString);
  }

  protected abstract ValidBody validate(String payload) throws BadRequestException;

  private String getPayloadType(String payload) {
    String trim = payload.trim();
    if (trim.startsWith("<") && !trim.startsWith("<<")) {
      return "xml";
    }
    return "json";
  }
}
