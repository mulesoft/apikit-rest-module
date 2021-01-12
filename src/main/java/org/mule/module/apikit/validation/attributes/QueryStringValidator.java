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

    Map<String, Parameter> facetsWithDefault = getFacetsWithDefaultValue(expected.facets());

    for (Object property : queryParams.keySet()) {
      facetsWithDefault.remove(property.toString());
      final List<String> actualQueryParam = queryParams.getAll(property.toString());

      result.append("\n").append(property).append(": ");

      if (actualQueryParam.size() > 1 || expected.isFacetArray(property.toString())) {
        for (Object o : actualQueryParam) {
          result.append("\n  - ").append(o);
        }
        result.append("\n");
      } else {
        for (Object o : actualQueryParam) {
          result.append(o).append("\n");
        }
      }
    }

    for (Entry<String, Parameter> entry : facetsWithDefault.entrySet()) {
      result.append(entry.getKey()).append(": ").append(entry.getValue().getDefaultValue()).append("\n");
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
