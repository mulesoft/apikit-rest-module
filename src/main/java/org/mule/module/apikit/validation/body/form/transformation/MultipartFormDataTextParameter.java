/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form.transformation;

import org.mule.module.apikit.api.exception.InvalidFormParameterException;
import org.mule.apikit.model.parameter.Parameter;
import org.mule.runtime.api.metadata.MediaType;

/**
 * This class is intended to validate multipart form-data
 * text parameters against the expected specification
 *
 */
public class MultipartFormDataTextParameter implements MultipartFormDataParameter {

  private final String body;

  public MultipartFormDataTextParameter(String body) {
    this.body = body;
  }

  @Override
  public void validate(Parameter expected) throws InvalidFormParameterException {
    if (!expected.validate(body)) {
      throw new InvalidFormParameterException(expected.message(body));
    }
  }
}
