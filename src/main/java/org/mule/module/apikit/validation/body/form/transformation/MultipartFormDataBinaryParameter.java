/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form.transformation;

import static java.lang.String.format;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import java.util.Set;
import org.mule.apikit.model.parameter.FileProperties;
import org.mule.module.apikit.api.exception.InvalidFormParameterException;
import org.mule.apikit.model.parameter.Parameter;
import org.mule.runtime.api.metadata.MediaType;

/**
 * This class is intended to validate multipart form-data
 * binary parameters against the expected specification
 *
 */
public class MultipartFormDataBinaryParameter implements MultipartFormDataParameter {

  private final byte[] byteArray;
  private final MediaType mediaType;

  public MultipartFormDataBinaryParameter(byte[] inputStream, MediaType mediaType) {
    this.byteArray = inputStream;
    this.mediaType = mediaType;
  }

  @Override
  public void validate(Parameter parameter) throws InvalidFormParameterException {
    if (!parameter.getFileProperties().isPresent()) {
      return;
    }
    FileProperties properties = parameter.getFileProperties().get();
    Set<String> fileTypes = properties.getFileTypes();
    Integer minValue = properties.getMinLength();
    Integer maxValue = properties.getMaxLength();

    if (isNotEmpty(fileTypes) && !anyFileTypeAllowed(fileTypes) && !fileTypes.contains(mediaType.toString())) {
      throw new InvalidFormParameterException(format("Invalid content type: %s", mediaType.toString()));
    }
    if (minValue == 0 && maxValue == 0) {
      return;
    }
    if (byteArray.length < minValue ||
        byteArray.length > maxValue) {
      throw new InvalidFormParameterException(
                                              format("Length must be between : %s and %s", properties.getMinLength(),
                                                     properties.getMaxLength()));
    }
  }

  private boolean anyFileTypeAllowed(Set<String> fileTypes) {
    return fileTypes.size() == 1 && fileTypes.contains("*/*");
  }
}
