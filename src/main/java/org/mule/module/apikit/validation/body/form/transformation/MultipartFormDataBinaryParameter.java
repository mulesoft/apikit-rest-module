/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form.transformation;

import org.mule.apikit.model.parameter.FileProperties;
import org.mule.apikit.model.parameter.Parameter;
import org.mule.module.apikit.api.exception.InvalidFormParameterException;
import org.mule.runtime.api.metadata.MediaType;

import javax.activation.MimeType;
import javax.activation.MimeTypeParameterList;
import javax.activation.MimeTypeParseException;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;

/**
 * This class is intended to validate multipart form-data
 * binary parameters against the expected specification
 *
 */
public class MultipartFormDataBinaryParameter implements MultipartFormDataParameter {

  private final int length;
  private final MediaType mediaType;

  public MultipartFormDataBinaryParameter(int length, MediaType mediaType) {
    this.length = length;
    this.mediaType = mediaType;
  }

  @Override
  public void validate(Parameter parameter) throws InvalidFormParameterException {
    Optional<FileProperties> fileProperties = parameter.getFileProperties();
    if (!fileProperties.isPresent()) {
      return;
    }
    FileProperties properties = fileProperties.get();
    Set<String> acceptedFileTypes = properties.getFileTypes();
    Integer minValue = properties.getMinLength();
    Integer maxValue = properties.getMaxLength();

    validateMediaType(acceptedFileTypes);
    if (minValue == 0 && maxValue == 0) {
      return;
    }
    if (length < minValue ||
        length > maxValue) {
      throw new InvalidFormParameterException(
                                              format("Length must be between : %s and %s", properties.getMinLength(),
                                                     properties.getMaxLength()));
    }
  }

  private void validateMediaType(Set<String> acceptedMediaTypes) throws InvalidFormParameterException {
    if (acceptedMediaTypes == null || acceptedMediaTypes.isEmpty()) {
      return;
    }

    // If we support anything
    if (acceptedMediaTypes.contains("*/*")) {
      return;
    }

    // If we have an exact match
    if (acceptedMediaTypes.contains(mediaType.toString())) {
      return;
    }

    // If any media type is compatible
    if (acceptedMediaTypes.stream().anyMatch(accepted -> {
      try {
        MimeType acceptedMimeType = new MimeType(accepted);
        return isCompatible(acceptedMimeType, mediaType);
      } catch (MimeTypeParseException e) {
        return false;
      }
    })) {
      return;
    }

    throw new InvalidFormParameterException(format("Invalid content type: %s", mediaType));
  }

  private boolean isCompatible(MimeType expected, MediaType given) {
    String expectedPrimary = expected.getPrimaryType();
    String expectedSub = expected.getSubType();
    String givenPrimary = given.getPrimaryType();
    String givenSub = given.getSubType();

    // We treat all parameters in `expected` as required to be equal. Extra parameters on `give` are be ignored.
    MimeTypeParameterList parameters = expected.getParameters();
    Enumeration<String> parameterNames = (Enumeration<String>) parameters.getNames();
    while (parameterNames.hasMoreElements()) {
      String name = parameterNames.nextElement();
      String expectedValue = parameters.get(name);
      String givenValue = given.getParameter(name);
      if (!Objects.equals(expectedValue, givenValue)) {
        return false;
      }
    }

    // If we have the ANY media type then it's compatible
    if (Objects.equals("*", expectedPrimary) && Objects.equals("*", expectedSub)) {
      return true;
    }

    // If we have something like `image/*` then any image it's compatible (`image/png` for example)
    if (Objects.equals(expectedPrimary, givenPrimary) && Objects.equals("*", expectedSub)) {
      return true;
    }

    // Otherwise, we want the full primary and secondary types to be equal (`*/test only validates against `*/test`)
    return Objects.equals(expectedPrimary, givenPrimary) && Objects.equals(expectedSub, givenSub);
  }
}
