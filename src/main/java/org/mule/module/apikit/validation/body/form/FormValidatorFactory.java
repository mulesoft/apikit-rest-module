/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form;

import org.mule.apikit.model.MimeType;
import org.mule.module.apikit.api.exception.InvalidFormParameterException;
import org.mule.runtime.core.api.el.ExpressionManager;

public class FormValidatorFactory {

  private static final String MULTIPART_FORM = "multipart/form-data";
  private static final String URLENCODED_FORM = "application/x-www-form-urlencoded";
  private final MimeType mimeType;
  private final ExpressionManager expressionManager;

  public FormValidatorFactory(MimeType mimeType, ExpressionManager expressionManager) {
    this.mimeType = mimeType;
    this.expressionManager = expressionManager;
  }

  public FormValidator createValidator(String requestMimeTypeName, boolean isParserV2) throws InvalidFormParameterException {
    if (requestMimeTypeName.contains(MULTIPART_FORM)) {
      return new MultipartFormValidator(mimeType.getFormParameters());
    }
    if (requestMimeTypeName.contains(URLENCODED_FORM)) {
      if (isParserV2) {
        return new UrlencodedFormV2Validator(mimeType, expressionManager);
      }
      return new UrlencodedFormV1Validator(mimeType.getFormParameters(), expressionManager);
    }
    throw new InvalidFormParameterException("Unsupported mimeType");
  }
}
