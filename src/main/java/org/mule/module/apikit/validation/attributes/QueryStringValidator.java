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
import java.util.List;
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

    Map<String, Parameter> facets = queryString.facets();
    Map<String, Parameter> facetsWithDefault = getFacetsWithDefaultValue(facets, queryParams);
    MultiMap<String, String> queryParamsCopy = copyImmutableMap(queryParams);

    if (!queryString.validate((Map<String, List<String>>) queryParamsCopy.toListValuesMap())) {
      throw new InvalidQueryStringException("Invalid value for query string");
    }

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

  private static boolean shouldProcessQueryString(QueryString queryString) {
    return queryString != null && !queryString.isArray() && !queryString.isScalar();
  }

  private static Map<String, Parameter> getFacetsWithDefaultValue(Map<String, Parameter> facets,
                                                                  MultiMap<String, String> queryParams) {
    HashMap<String, Parameter> result = Maps.newHashMap();
    for (Entry<String, Parameter> entry : facets.entrySet()) {
      if (entry.getValue().getDefaultValue() != null && queryParams.get(entry.getKey()) == null) {
        result.put(entry.getKey(), entry.getValue());
      }
    }
    return result;
  }
}
