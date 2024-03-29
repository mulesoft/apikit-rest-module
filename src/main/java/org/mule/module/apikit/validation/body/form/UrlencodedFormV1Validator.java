/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form;

/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
import org.mule.apikit.model.parameter.Parameter;
import org.mule.module.apikit.api.exception.BadRequestException;
import org.mule.module.apikit.api.exception.InvalidFormParameterException;
import org.mule.module.apikit.validation.body.form.transformation.DataWeaveTransformer;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.core.api.el.ExpressionManager;

import java.util.List;
import java.util.Map;

public class UrlencodedFormV1Validator implements FormValidator<TypedValue> {

  private final Map<String, List<Parameter>> formParameters;
  private final DataWeaveTransformer dataWeaveTransformer;

  public UrlencodedFormV1Validator(Map<String, List<Parameter>> formParameters, ExpressionManager expressionManager) {
    this.formParameters = formParameters;
    this.dataWeaveTransformer = new DataWeaveTransformer(expressionManager);
  }

  @Override
  public TypedValue validate(TypedValue originalPayload) throws BadRequestException {
    MultiMap<String, String> requestMap = dataWeaveTransformer.getMultiMapFromPayload(originalPayload);

    for (String expectedKey : formParameters.keySet()) {
      if (formParameters.get(expectedKey).size() != 1) {
        // do not perform validation when multi-type parameters are used
        continue;
      }

      Parameter expected = formParameters.get(expectedKey).get(0);

      Object actual = requestMap.get(expectedKey);

      if (actual == null && expected.isRequired()) {
        throw new InvalidFormParameterException("Required form parameter " + expectedKey + " not specified");
      }

      List<String> defaultValues = expected.getDefaultValues();
      if (actual == null && !defaultValues.isEmpty()) {
        defaultValues.forEach(value -> requestMap.put(expectedKey, value));
      }

      if (actual != null && actual instanceof String) {
        if (!expected.validate((String) actual)) {
          String msg = String.format("Invalid value '%s' for form parameter %s. %s",
                                     actual, expectedKey, expected.message((String) actual));
          throw new InvalidFormParameterException(msg);
        }
      }
    }
    return dataWeaveTransformer.getXFormUrlEncodedStream(requestMap, originalPayload.getDataType());
  }


}
