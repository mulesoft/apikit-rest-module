/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body;

import static java.lang.String.format;

import org.mule.apikit.model.MimeType;
import org.mule.module.apikit.api.exception.InvalidFormParameterException;
import org.mule.module.apikit.validation.body.form.MultipartFormValidator;
import org.mule.module.apikit.validation.body.form.UrlencodedFormV1Validator;
import org.mule.module.apikit.validation.body.form.UrlencodedFormV2Validator;
import org.mule.runtime.core.api.el.ExpressionManager;

public class FormValidatorFactory {
  private static final String MULTIPART_FORM = "multipart/";
  private static final String URLENCODED_FORM = "application/x-www-form-urlencoded";
  private final boolean isParserV2;
  private final ExpressionManager expressionManager;

  public FormValidatorFactory(boolean isParserV2, ExpressionManager expressionManager) {
    this.isParserV2 = isParserV2;
    this.expressionManager = expressionManager;
  }

  public PayloadValidator createValidator(String requestMimeTypeName, MimeType mimeType) throws InvalidFormParameterException {
    if (requestMimeTypeName.contains(MULTIPART_FORM)) {
      return new MultipartFormValidator(mimeType.getFormParameters());
    }
    if (requestMimeTypeName.contains(URLENCODED_FORM)) {
      if (isParserV2) {
        return new UrlencodedFormV2Validator(mimeType, expressionManager);
      }
      return new UrlencodedFormV1Validator(mimeType.getFormParameters(), expressionManager);
    }
    throw new InvalidFormParameterException(format("Unexpected Mime Type %s", requestMimeTypeName));
  }
}
