/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form.transformation;

import org.mule.module.apikit.api.exception.InvalidFormParameterException;
import org.mule.raml.interfaces.model.parameter.IParameter;
import org.mule.runtime.api.metadata.MediaType;
/**
 * This class is intended to validate multipart form-data
 * binary parameters against the expected specification
 *
 */
public class MultipartFormDataBinaryParameter implements MultipartFormDataParameter{
  private final MediaType mediaType;
  private final byte[] byteArray;

  public MultipartFormDataBinaryParameter(byte[] inputStream, MediaType mediaType) {
    this.byteArray = inputStream;
    this.mediaType = mediaType;
  }

  @Override
  public void validate(IParameter parameter) throws InvalidFormParameterException {
    //Parsers currently doesn't validate files
  }
}
