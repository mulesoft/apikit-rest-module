/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form.transformation;

import org.apache.commons.io.IOUtils;
import org.mule.module.apikit.api.exception.InvalidFormParameterException;
import org.mule.raml.interfaces.model.parameter.IParameter;
import org.mule.runtime.api.metadata.MediaType;

import java.io.IOException;
import java.io.InputStream;

public class MultipartFormDataParameter {
  private final MediaType mediaType;
  private final InputStream inputStream;

  public MultipartFormDataParameter(InputStream inputStream, MediaType mediaType){
    this.inputStream = inputStream;
    this.mediaType = mediaType;
  }

  public void validate(IParameter parameter) throws InvalidFormParameterException {
    if(mediaType.matches(MediaType.TEXT) ){
      String value;
      try {
        value = IOUtils.toString(inputStream);
      } catch (IOException e) {
        throw new InvalidFormParameterException(e);
      }
      if(!parameter.validate(value)) {
        throw new InvalidFormParameterException("Value " + value + " for parameter " + parameter.getDisplayName() + " is invalid");
      }
    }
  }

}
