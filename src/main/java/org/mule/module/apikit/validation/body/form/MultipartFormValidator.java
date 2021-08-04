/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form;


import static org.mule.runtime.api.metadata.DataType.INPUT_STREAM;

import java.util.Map.Entry;
import org.mule.apikit.model.parameter.Parameter;
import org.mule.module.apikit.api.exception.BadRequestException;
import org.mule.module.apikit.api.exception.InvalidFormParameterException;
import org.mule.module.apikit.validation.body.form.transformation.Multipart;
import org.mule.module.apikit.validation.body.form.transformation.MultipartBuilder;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.metadata.TypedValue;

import java.util.List;
import java.util.Map;
import org.mule.runtime.api.streaming.bytes.CursorStreamProvider;

public class MultipartFormValidator implements FormValidator<TypedValue> {

  private final Map<String, List<Parameter>> formParameters;

  public MultipartFormValidator(Map<String, List<Parameter>> formParameters) {
    this.formParameters = formParameters;
  }

  @Override
  public TypedValue validate(TypedValue payload) throws BadRequestException {
    CursorStreamProvider cursorStream = (CursorStreamProvider) payload.getValue();

    MultipartBuilder multipartBuilder =
        new MultipartBuilder(cursorStream, payload.getDataType().getMediaType().toString(), getBoundary(payload));

    for (Entry<String, List<Parameter>> formParameter : formParameters.entrySet()) {

      Parameter parameter = formParameter.getValue().get(0);
      multipartBuilder.withExpectedParameter(formParameter.getKey(), parameter);

      if (parameter.getDefaultValue() != null) {
        multipartBuilder.withDefaultValue(formParameter.getKey(), parameter.getDefaultValue());
      }

    }
    return getTypedValue(multipartBuilder.build());
  }

  private TypedValue getTypedValue(Multipart multipart) throws InvalidFormParameterException {
    Object is = multipart.content();
    MediaType mediaType = MediaType.parse(multipart.contentType());
    DataType dataType = DataType
        .builder(INPUT_STREAM)
        .mediaType(mediaType)
        .build();

    return new TypedValue<>(is, dataType);
  }

  private String getBoundary(TypedValue originalPayload) throws InvalidFormParameterException {
    String boundary = originalPayload.getDataType().getMediaType().getParameter("boundary");
    if (boundary == null) {
      throw new InvalidFormParameterException("Required boundary parameter not found");
    }
    return boundary;
  }
}
