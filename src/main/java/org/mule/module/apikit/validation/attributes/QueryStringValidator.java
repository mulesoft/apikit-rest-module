/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.attributes;

import com.google.common.collect.Maps;
import org.mule.apikit.model.QueryString;
import org.mule.apikit.model.parameter.Parameter;
import org.mule.module.apikit.api.exception.InvalidQueryStringException;
import org.mule.runtime.api.util.MultiMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static org.mule.module.apikit.helpers.AttributesHelper.addQueryString;
import static org.mule.module.apikit.helpers.AttributesHelper.copyImmutableMap;

public class QueryStringValidator {

  public static ValidatedQueryParams validate(QueryString queryString, String rawQueryString,
                                              MultiMap<String, String> queryParams)
      throws InvalidQueryStringException {
    if (!shouldProcessQueryString(queryString)) {
      return null;
    }

    Map<String, Parameter> facetsWithDefault = getFacetsWithDefaultValue(queryString.facets());
    MultiMap<String, String> queryParamsCopy = copyImmutableMap(queryParams);
    queryParamsCopy.keySet().forEach(facetsWithDefault::remove);
    validateQueryString(queryParamsCopy, queryString);

    return new ValidatedQueryParams(queryParamsCopy, addDefaultValues(facetsWithDefault, queryParamsCopy, rawQueryString));
  }



  /**
   * Adds default values to raw Query String and Query parameters map.
   *
   * @param facetsWithDefault
   * @param queryParamsCopy
   * @param rawQueryString
   * @return encoded raw Query String with defaults values
   */
  private static String addDefaultValues(Map<String, Parameter> facetsWithDefault, MultiMap<String, String> queryParamsCopy,
                                         String rawQueryString) {
    String defaultValue;
    for (Entry<String, Parameter> entry : facetsWithDefault.entrySet()) {
      defaultValue = entry.getValue().getDefaultValue();
      rawQueryString = addQueryString(rawQueryString, entry.getKey(), defaultValue);
      queryParamsCopy.put(entry.getKey(), entry.getValue().getDefaultValue());
    }

    return rawQueryString;
  }

  /**
   * Validates YAML Query String.
   *
   * @param queryParamsCopy
   * @param queryString
   * @throws InvalidQueryStringException
   */
  private static void validateQueryString(MultiMap<String, String> queryParamsCopy, QueryString queryString)
      throws InvalidQueryStringException {
    if (!queryString.validate(new HashMap<>(queryParamsCopy.toListValuesMap()))) {
      throw new InvalidQueryStringException("Invalid value for query string");
    }
  }

  private static boolean shouldProcessQueryString(QueryString queryString) {
    return queryString != null && !queryString.isArray() && !queryString.isScalar();
  }

  private static Map<String, Parameter> getFacetsWithDefaultValue(Map<String, Parameter> facets) {
    HashMap<String, Parameter> result = Maps.newHashMap();
    for (Entry<String, Parameter> entry : facets.entrySet()) {
      if (entry.getValue().getDefaultValue() != null) {
        result.put(entry.getKey(), entry.getValue());
      }
    }
    return result;
  }
}
