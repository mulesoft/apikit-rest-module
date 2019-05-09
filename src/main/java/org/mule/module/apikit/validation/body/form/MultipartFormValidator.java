/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form;

import org.mule.module.apikit.StreamUtils;
import org.mule.module.apikit.api.exception.InvalidFormParameterException;
import org.mule.module.apikit.validation.body.form.transformation.MultipartFormData;
import org.mule.module.apikit.validation.body.form.transformation.MultipartFormDataParameter;
import org.mule.raml.interfaces.model.parameter.IParameter;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.core.api.el.ExpressionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class MultipartFormValidator implements FormValidatorStrategy<TypedValue> {

  protected static final Logger logger = LoggerFactory.getLogger(MultipartFormValidator.class);
  Map<String, List<IParameter>> formParameters;

  public MultipartFormValidator(Map<String, List<IParameter>> formParameters, ExpressionManager expressionManager) {
    this.formParameters = formParameters;
  }

  @Override
  public TypedValue validate(TypedValue originalPayload) throws InvalidFormParameterException {
    final InputStream inputStream = StreamUtils.unwrapCursorStream(originalPayload.getValue());
    final byte[] boundary = getBoundary(originalPayload);
    MultipartFormData multipartFormData = new MultipartFormData(inputStream, boundary);
    Map<String, MultipartFormDataParameter> actualParameters = multipartFormData.getFormDataParameters();

    for (String expectedKey : formParameters.keySet()) {
      List<IParameter> params = formParameters.get(expectedKey);
      if (params != null && params.size() == 1){
        IParameter expected = params.get(0);
        if (actualParameters.containsKey(expectedKey)) {
          MultipartFormDataParameter multipartFormDataParameter = actualParameters.get(expectedKey);
          multipartFormDataParameter.validate(expected);
        } else {
          if (expected.getDefaultValue() != null) {
            multipartFormData.addDefault(expectedKey,expected.getDefaultValue());
          } else if (expected.isRequired()) {
            throw new InvalidFormParameterException("Required form parameter " + expectedKey + " not specified");
          }
        }
      }
    }

    return TypedValue.of(multipartFormData.build());
  }

  private byte[] getBoundary(TypedValue originalPayload) throws InvalidFormParameterException {
    String boundary = originalPayload.getDataType().getMediaType().getParameter("boundary");
    if(boundary == null){
      throw new InvalidFormParameterException("Required boundary parameter not found");
    }
    return boundary.getBytes();
  }

}
