/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form;

import org.mule.apikit.model.MimeType;
import org.mule.apikit.model.parameter.Parameter;
import org.mule.module.apikit.api.exception.BadRequestException;
import org.mule.module.apikit.api.exception.InvalidFormParameterException;
import org.mule.module.apikit.validation.body.form.transformation.DataWeaveTransformer;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.core.api.el.ExpressionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.joining;

public class UrlencodedFormV2Validator implements FormValidator<TypedValue> {

  protected static final Logger logger = LoggerFactory.getLogger(UrlencodedFormV2Validator.class);
  private final MimeType actionMimeType;
  private final DataWeaveTransformer dataWeaveTransformer;

  public UrlencodedFormV2Validator(MimeType actionMimeType, ExpressionManager expressionManager) {
    this.actionMimeType = actionMimeType;
    this.dataWeaveTransformer = new DataWeaveTransformer(expressionManager);
  }

  @Override
  public TypedValue validate(TypedValue originalPayload) throws BadRequestException {

    MultiMap<String, String> requestMap = dataWeaveTransformer.getMultiMapFromPayload(originalPayload);

    validateAndAddDefaults(requestMap);

    return dataWeaveTransformer.getXFormUrlEncodedStream(requestMap, originalPayload.getDataType());
  }

  private void validateAndAddDefaults(MultiMap<String, String> requestMap) throws InvalidFormParameterException {
    final Map<String, List<Parameter>> formParameters = actionMimeType.getFormParameters();

    final Set<String> expectedKeys = formParameters.keySet();

    for (String expectedKey : expectedKeys) {
      final Parameter parameter = formParameters.get(expectedKey).get(0);
      final List<String> values = requestMap.getAll(expectedKey);
      if (values.isEmpty()) {
        final List<String> defaultValues = parameter.getDefaultValues();
        if (!defaultValues.isEmpty()) {
          defaultValues.forEach(value -> requestMap.put(expectedKey, value));
        } else if (parameter.isRequired()) {
          throw new InvalidFormParameterException("Required parameter " + expectedKey + " not specified");
        }
      } else {
        if (parameter.isRepeat() || parameter.isArray()) {
          validateAsArray(expectedKey, parameter, values);
        } else if (values.size() > 1)
          throw new InvalidFormParameterException("Parameter '" + expectedKey + "' is not repeatable");
        else {
          validate(expectedKey, parameter, values.get(0));
        }
      }
    }
  }

  private void validate(String expectedKey, Parameter parameter, String value) throws InvalidFormParameterException {
    if (!parameter.validate(value))
      throw new InvalidFormParameterException("Invalid value '" + value + "' for parameter" + expectedKey);
  }

  private void validateAsArray(String expectedKey, Parameter parameter, List<String> values)
      throws InvalidFormParameterException {
    final String valueToValidate = values.stream().map(v -> "- " + v).collect(joining("\n"));
    if (!parameter.validate(valueToValidate)) {
      // Numeric values are always parsed as number, this is a workaround to validate them as string
      if (!parameter.validate(values.stream().map(v -> "- '" + v + "'").collect(joining("\n")))) {
        throw new InvalidFormParameterException("Invalid value '" + valueToValidate + "' for parameter" + expectedKey);
      }
    }
  }
}
