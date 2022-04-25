/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.attributes;

import org.mule.apikit.model.parameter.Parameter;
import org.mule.module.apikit.api.exception.InvalidQueryParameterException;
import org.mule.runtime.api.util.MultiMap;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Joiner.on;
import static com.google.common.collect.Sets.difference;
import static java.lang.String.format;
import static org.mule.module.apikit.helpers.AttributesHelper.addQueryString;
import static org.mule.module.apikit.helpers.AttributesHelper.copyImmutableMap;

public class QueryParameterValidator {

  public static ValidatedQueryParams validate(Map<String, Parameter> queryParameters,
                                              MultiMap<String, String> incomingQueryParams,
                                              String queryString,
                                              boolean queryParamsStrictValidation)
      throws InvalidQueryParameterException {

    if (queryParamsStrictValidation) {
      validateQueryParametersStrictly(queryParameters, incomingQueryParams);
    }

    return validateQueryParams(queryParameters, incomingQueryParams, queryString);
  }

  private static ValidatedQueryParams validateQueryParams(Map<String, Parameter> queryParameters,
                                                          MultiMap<String, String> incomingQueryParams, String queryString)
      throws InvalidQueryParameterException {

    String queryStringWithDefaults = queryString;
    MultiMap<String, String> queryParamsCopy = copyImmutableMap(incomingQueryParams);

    for (Entry<String, Parameter> paramDefinitionEntry : queryParameters.entrySet()) {
      Parameter parameterDefinition = paramDefinitionEntry.getValue();
      String paramKey = paramDefinitionEntry.getKey();
      List<String> values = incomingQueryParams.getAll(paramKey);

      if (!values.isEmpty()) {
        if (parameterDefinition.isRepeat() || parameterDefinition.isArray()) {
          validateQueryParamArray(paramKey, parameterDefinition, values);
        } else {
          if (values.size() > 1) {
            throw new InvalidQueryParameterException("Query parameter " + paramKey + " is not repeatable");
          }
          validateQueryParam(queryParamsCopy, paramKey, parameterDefinition, values.get(0));
        }
      } else {
        if (parameterDefinition.isRequired()) {
          throw new InvalidQueryParameterException("Required query parameter " + paramKey + " not specified");
        }
        if (paramDefinitionEntry.getValue().getDefaultValue() != null) {
          String queryParamDefaultValue = paramDefinitionEntry.getValue().getDefaultValue();
          queryStringWithDefaults = addQueryString(queryStringWithDefaults, paramKey, queryParamDefaultValue);
          queryParamsCopy.put(paramKey, queryParamDefaultValue);
        }
      }
    }

    return new ValidatedQueryParams(queryParamsCopy, queryStringWithDefaults);
  }

  private static void validateQueryParametersStrictly(Map<String, Parameter> queryParameters,
                                                      MultiMap<String, String> incomingQueryParams)
      throws InvalidQueryParameterException {
    //check that query parameters are defined in the RAML
    Set notDefinedQueryParameters = difference(incomingQueryParams.keySet(), queryParameters.keySet());
    if (!notDefinedQueryParameters.isEmpty()) {
      throw new InvalidQueryParameterException(format("[%s] %s", on(", ").join(notDefinedQueryParameters),
                                                      "parameters are not defined in API spec."));
    }
  }

  //only for raml 1.0
  private static void validateQueryParamArray(String paramKey, Parameter expected, Collection<?> paramValues)
      throws InvalidQueryParameterException {
    if (!expected.validateArray(paramValues)) {
      String msg = format("Invalid value '%s' for query parameter %s. %s",
                          paramValues.stream().map(String::valueOf).collect(Collectors.joining(", ")), paramKey,
                          expected.messageFromValues(paramValues));
      throw new InvalidQueryParameterException(msg);
    }
  }

  private static void validateQueryParam(MultiMap<String, String> queryParams, String paramKey, Parameter parameterDefinition,
                                         String value)
      throws InvalidQueryParameterException {
    validate(paramKey, parameterDefinition, value);
    replaceNullStringValue(queryParams, paramKey, parameterDefinition, value);
  }

  private static void replaceNullStringValue(MultiMap<String, String> queryParams, String paramKey, Parameter parameterDefinition,
                                             String value) {
    // if query param value is "null" as String, we check if that query param is nullable(in raml nil type)
    if ("null".equals(value) && isNullable(parameterDefinition)) {
      queryParams.remove(paramKey);
      queryParams.put(paramKey, Arrays.asList((String) null));
    }
  }

  private static void validate(String paramKey, Parameter expected, String paramValue) throws InvalidQueryParameterException {
    if (!expected.validate(paramValue)) {
      String msg = format("Invalid value '%s' for query parameter %s. %s",
                          paramValue, paramKey, expected.message(paramValue));
      throw new InvalidQueryParameterException(msg);
    }
  }

  private static boolean isNullable(Parameter parameter) {
    return parameter.validate(null);
  }
}
