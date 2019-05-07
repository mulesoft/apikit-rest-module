/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form;

import org.mule.module.apikit.api.exception.InvalidFormParameterException;
import org.mule.module.apikit.validation.body.form.transformation.DataWeaveTransformer;
import org.mule.module.apikit.validation.body.form.transformation.MultipartFormData;
import org.mule.module.apikit.validation.body.form.transformation.MultipartFormDataParameter;
import org.mule.raml.interfaces.model.parameter.IParameter;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.streaming.CursorProvider;
import org.mule.runtime.api.streaming.bytes.CursorStreamProvider;
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
    final InputStream inputStream = originalPayload.getValue() instanceof CursorProvider ? ((CursorStreamProvider) originalPayload.getValue()).openCursor() : ((InputStream) originalPayload.getValue());
    final byte[] boundary = originalPayload.getDataType().getMediaType().getParameter("boundary").getBytes();
    MultipartFormData multipartFormData = new MultipartFormData(inputStream, boundary);
    Map<String, MultipartFormDataParameter> actualParameters = multipartFormData.getFormDataParameters();

    for (String expectedKey : formParameters.keySet()) {
      if (formParameters.get(expectedKey).size() != 1) {
        //do not perform validation when multi-type parameters are used
        continue;
      }

      IParameter expected = formParameters.get(expectedKey).get(0);
      if (actualParameters.keySet().contains(expectedKey)) {
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
    if (multipartFormData.areDefaultsAdedd()) {
      return TypedValue.of(multipartFormData.build());
    } else {
      return originalPayload;
    }
  }

}
