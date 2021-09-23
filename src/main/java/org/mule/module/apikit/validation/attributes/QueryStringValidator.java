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
import static org.mule.module.apikit.validation.attributes.ValidationUtils.escapeAndSurroundWithQuotesIfNeeded;

public class QueryStringValidator {

  public static ValidatedQueryParams validate(QueryString queryString, String rawQueryString,
                                              MultiMap<String, String> queryParams)
      throws InvalidQueryStringException {
    if (!shouldProcessQueryString(queryString)) {
      return null;
    }

    Map<String, Parameter> facets = queryString.facets();
    Map<String, Parameter> facetsWithDefault = getFacetsWithDefaultValue(facets);
    MultiMap<String, String> queryParamsCopy = copyImmutableMap(queryParams);
    StringBuilder queryStringYaml = queryStringAsYaml(queryString, facets, facetsWithDefault, queryParamsCopy);
    validateQueryString(queryStringYaml, queryString);

    return new ValidatedQueryParams(queryParamsCopy, addDefaultValues(facetsWithDefault, queryParamsCopy, rawQueryString));
  }

  /**
   * Builds a YAML from provided Query String.
   *
   * @param queryString
   * @param facets
   * @param facetsWithDefault
   * @param queryParamsCopy
   * @return StringBuilder for the Query String as YAML
   */
  private static StringBuilder queryStringAsYaml(QueryString queryString, Map<String, Parameter> facets,
                                                 Map<String, Parameter> facetsWithDefault,
                                                 MultiMap<String, String> queryParamsCopy) {
    StringBuilder queryStringYaml = new StringBuilder();
    Parameter facet;
    for (Object property : queryParamsCopy.keySet()) {
      facet = facets.get(property.toString());
      facetsWithDefault.remove(property.toString());
      final List<String> actualQueryParam = queryParamsCopy.getAll(property.toString());

      queryStringYaml.append("\n").append(property).append(": ");

      if (actualQueryParam.size() > 1 || queryString.isFacetArray(property.toString())) {
        for (String value : actualQueryParam) {
          queryStringYaml.append("\n  - ").append(escapeAndSurroundWithQuotesIfNeeded(facet, value));
        }
        queryStringYaml.append("\n");
      } else {
        for (String value : actualQueryParam) {
          queryStringYaml.append(escapeAndSurroundWithQuotesIfNeeded(facet, value)).append("\n");
        }
      }
    }
    return queryStringYaml;
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
   * @param queryStringYaml
   * @param queryString
   * @throws InvalidQueryStringException
   */
  private static void validateQueryString(StringBuilder queryStringYaml, QueryString queryString)
      throws InvalidQueryStringException {
    // If no YAML, empty value ends up in an empty JSON object
    if (queryStringYaml.length() == 0) {
      queryStringYaml.append("{}");
    }

    if (!queryString.validate(queryStringYaml.toString())) {
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
