/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.attributes;

import com.google.common.base.Joiner;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.mule.apikit.model.parameter.Parameter;
import org.mule.module.apikit.api.exception.InvalidQueryParameterException;
import org.mule.apikit.model.Action;
import org.mule.runtime.api.util.MultiMap;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Sets.difference;
import static org.mule.module.apikit.helpers.AttributesHelper.*;
import static org.mule.module.apikit.helpers.AttributesHelper.addQueryString;

public class QueryParameterValidator {

  private final Action action;

  public QueryParameterValidator(Action action) {
    this.action = action;
  }

  public ValidatedQueryParams validate(MultiMap<String, String> queryParams, String queryString,
                                       boolean queryParamsStrictValidation)
      throws InvalidQueryParameterException {

    if (queryParamsStrictValidation) {
      validateQueryParametersStrictly(queryParams);
    }

    validateQueryParamsSize(queryParams);
    validateQueryParamsValues(queryParams);

    return addDefaultValues(queryParams, queryString);
  }

  private void validateQueryParamsSize(MultiMap<String, String> queryParams) throws InvalidQueryParameterException {
    for (Entry<String, Parameter> queryParam : action.getQueryParameters().entrySet()) {
      String paramKey = queryParam.getKey();
      Parameter parameterDefinition = queryParam.getValue();
      List<String> values = queryParams.getAll(paramKey);
      if (values.isEmpty() && parameterDefinition.isRequired()) {
        throw new InvalidQueryParameterException("\"Required query parameter " + paramKey + " not specified\"");
      }
      if (values.size() > 1 && !(parameterDefinition.isRepeat() || parameterDefinition.isArray())) {
        throw new InvalidQueryParameterException("Query parameter " + paramKey + " is not repeatable");
      }
    }
  }

  private void validateQueryParamsValues(MultiMap<String, String> queryParams) throws InvalidQueryParameterException {
    Map<String, Parameter> queryParamsDefinition = action.getQueryParameters();

    for (String paramKey : queryParams.keySet()) {
      Parameter parameterDefinition = queryParamsDefinition.get(paramKey);
      List<String> values = queryParams.getAll(paramKey);

      if (parameterDefinition.isArray()) {
        validateQueryParamArray(paramKey, parameterDefinition, values);

      } else {
        validateQueryParam(queryParams, paramKey, parameterDefinition, values);
      }
    }
  }

  private ValidatedQueryParams addDefaultValues(MultiMap<String, String> queryParams, String queryString) {
    String queryStringWithDefaults = queryString;
    MultiMap<String, String> queryParamsWithDefaults = queryParams;

    for (Entry<String, Parameter> queryParam : action.getQueryParameters().entrySet()) {
      String queryParamKey = queryParam.getKey();
      List<String> values = queryParams.getAll(queryParamKey);
      if (values.isEmpty() && queryParam.getValue().getDefaultValue() != null) {
        String queryParamDefaultValue = queryParam.getValue().getDefaultValue();
        queryStringWithDefaults =
            addQueryString(queryStringWithDefaults, queryParams.size(), queryParamKey, queryParamDefaultValue);
        queryParamsWithDefaults = addParam(queryParamsWithDefaults, queryParamKey, queryParamDefaultValue);
      }
    }
    return new ValidatedQueryParams(queryParamsWithDefaults, queryStringWithDefaults);
  }

  private void validateQueryParametersStrictly(MultiMap<String, String> queryParams) throws InvalidQueryParameterException {
    //check that query parameters are defined in the RAML
    Set notDefinedQueryParameters = difference(queryParams.keySet(), action.getQueryParameters().keySet());
    if (!notDefinedQueryParameters.isEmpty()) {
      throw new InvalidQueryParameterException(Joiner.on(", ").join(notDefinedQueryParameters)
          + " parameters are not defined in RAML.");
    }
  }

  //only for raml 1.0
  private void validateQueryParamArray(String paramKey, Parameter expected, Collection<?> paramValues)
      throws InvalidQueryParameterException {
    StringBuilder builder = new StringBuilder();

    paramValues.forEach(paramValue -> {
      String value = String.valueOf(paramValue);
      builder.append("- ");
      builder.append(expected.surroundWithQuotesIfNeeded(value));
      builder.append("\n");
    });

    validate(paramKey, expected, builder.toString());
  }

  private void validateQueryParam(MultiMap<String, String> queryParams, String paramKey, Parameter parameterDefinition,
                                  List<String> values)
      throws InvalidQueryParameterException {
    for (String value : values) {
      // if query param value is "null" as String, we check if that query param is nullable(in raml nil type)
      if ("null".equals(value) && isNullable(parameterDefinition)) {
        // remove "null" string from query parameter values, and replace it with null
        // List<String> values = queryParams.getAll(paramKey) is unmodifiableList
        List<String> copyWithoutNull = values.stream().filter(current -> !"null".equals(current)).collect(Collectors.toList());
        queryParams.remove(paramKey);
        queryParams.put(paramKey, copyWithoutNull);
        queryParams.put(paramKey, (String) null);
      } else {
        validate(paramKey, parameterDefinition, parameterDefinition.surroundWithQuotesIfNeeded(value));
      }
    }
  }

  private void validate(String paramKey, Parameter expected, String paramValue) throws InvalidQueryParameterException {
    if (!expected.validate(paramValue)) {
      String msg = String.format("\"Invalid value '%s' for query parameter %s. %s\"",
                                 paramValue, paramKey, expected.message(paramValue));
      throw new InvalidQueryParameterException(msg);
    }
  }

  private boolean isNullable(Parameter parameter) {
    return parameter.validate(null);
  }
}
