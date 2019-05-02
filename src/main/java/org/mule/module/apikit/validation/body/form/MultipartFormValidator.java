/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form;

import org.apache.commons.fileupload.MultipartStream;
import org.mule.module.apikit.api.exception.InvalidFormParameterException;
import org.mule.module.apikit.validation.body.form.transformation.*;
import org.mule.raml.interfaces.model.parameter.IParameter;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.streaming.CursorProvider;
import org.mule.runtime.api.streaming.bytes.CursorStreamProvider;
import org.mule.runtime.core.api.el.ExpressionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MimeTypeUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultipartFormValidator implements FormValidatorStrategy<TypedValue> {

  protected static final Logger logger = LoggerFactory.getLogger(MultipartFormValidator.class);
  Map<String, List<IParameter>> formParameters;
  DataWeaveTransformer dataWeaveTransformer;

  public MultipartFormValidator(Map<String, List<IParameter>> formParameters, ExpressionManager expressionManager) {
    this.formParameters = formParameters;
    this.dataWeaveTransformer = new DataWeaveTransformer(expressionManager);

  }

  @Override
  public TypedValue validate(TypedValue originalPayload) throws InvalidFormParameterException {
    final InputStream inputStream = originalPayload.getValue() instanceof CursorProvider ? ((CursorStreamProvider) originalPayload.getValue()).openCursor() : ((InputStream) originalPayload.getValue());
    final byte[] boundary = originalPayload.getDataType().getMediaType().getParameter("boundary").getBytes();
    Map<String, MultipartFormDataParameter> actualParameters = new MultipartFormData(inputStream,boundary).getFormDataParameters();
    DataWeaveDefaultsBuilder defaultsBuilder = new DataWeaveDefaultsBuilder();

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
          defaultsBuilder.addPart(new TextPlainPart().setName(expectedKey).setValue(expected.getDefaultValue()));
        } else if (expected.isRequired()) {
          throw new InvalidFormParameterException("Required form parameter " + expectedKey + " not specified");
        }
      }
    }
    if (defaultsBuilder.areDefaultsToAdd()) {
      return dataWeaveTransformer.runDataWeaveScript(defaultsBuilder.build(), originalPayload.getDataType(), originalPayload);
    } else {
      return originalPayload;
    }
  }

}
