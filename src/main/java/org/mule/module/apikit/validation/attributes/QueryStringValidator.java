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

public class QueryStringValidator {

  public static void validate(QueryString queryString, MultiMap<String, String> queryParams) throws InvalidQueryStringException {
    if (!shouldProcessQueryString(queryString)) {
      return;
    }

    String actualQueryString = buildQueryString(queryString, queryParams);

    if (!queryString.validate(actualQueryString)) {
      throw new InvalidQueryStringException("Invalid value for query string");
    }
  }

  private static boolean shouldProcessQueryString(QueryString queryString) {
    return queryString != null && !queryString.isArray() && !queryString.isScalar();
  }

  private static String buildQueryString(QueryString expected, MultiMap<String, String> queryParams) {
    StringBuilder result = new StringBuilder();

    Map<String, Parameter> facets = expected.facets();
    Map<String, Parameter> facetsWithDefault = getFacetsWithDefaultValue(facets);
    Parameter facet;

    for (Object property : queryParams.keySet()) {
      facet = facets.get(property.toString());
      facetsWithDefault.remove(property.toString());
      final List<String> actualQueryParam = queryParams.getAll(property.toString());

      result.append("\n").append(property).append(": ");

      if (actualQueryParam.size() > 1 || expected.isFacetArray(property.toString())) {
        for (String value : actualQueryParam) {
          result.append("\n  - ").append(facet != null ? facet.surroundWithQuotesIfNeeded(value) : value);
        }
        result.append("\n");
      } else {
        for (String value : actualQueryParam) {
          result.append(facet != null ? facet.surroundWithQuotesIfNeeded(value) : value).append("\n");
        }
      }
    }

    String defaultValue;
    for (Entry<String, Parameter> entry : facetsWithDefault.entrySet()) {
      facet = facets.get(entry.getKey());
      defaultValue = entry.getValue().getDefaultValue();
      result.append(entry.getKey()).append(": ")
          .append(facet != null ? facet.surroundWithQuotesIfNeeded(defaultValue) : defaultValue).append("\n");
    }

    if (result.length() > 0) {
      return result.toString();
    }
    if (expected.getDefaultValue() != null) {
      return expected.getDefaultValue();
    }

    return "{}";
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
