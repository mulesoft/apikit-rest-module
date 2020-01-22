/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.attributes;

import org.mule.apikit.model.Action;
import org.mule.apikit.model.parameter.Parameter;
import org.mule.module.apikit.api.exception.InvalidUriParameterException;
import org.mule.module.apikit.api.uri.ResolvedVariables;

import java.util.Map;

public class UriParametersValidator {

  private final Action action;

  public UriParametersValidator(Action action) {
    this.action = action;
  }

  public void validate(ResolvedVariables resolvedVariables)
      throws InvalidUriParameterException {
    for (Map.Entry<String, Parameter> entry : action.getResolvedUriParameters().entrySet()) {
      String value = (String) resolvedVariables.get(entry.getKey());
      Parameter uriParameter = entry.getValue();
      if (!uriParameter.validate(value)) {
        String msg = String.format("Invalid value '%s' for uri parameter %s. %s",
                                   value, entry.getKey(), uriParameter.message(value));

        throw new InvalidUriParameterException(msg);
      }
    }
  }

}
