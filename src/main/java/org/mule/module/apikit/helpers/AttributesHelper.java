/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.helpers;

import com.google.common.base.Strings;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.extension.http.api.HttpRequestAttributesBuilder;
import org.mule.module.apikit.api.exception.BadRequestException;
import org.mule.module.apikit.exception.UnsupportedMediaTypeException;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.util.MultiMap;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static org.mule.module.apikit.HeaderName.ACCEPT;
import static org.mule.module.apikit.HeaderName.CONTENT_TYPE;
import static org.mule.runtime.api.metadata.MediaType.parse;

public class AttributesHelper {

  private static final String ANY_RESPONSE_MEDIA_TYPE = "*/*";
  private static final String COMMA_SEPARATOR = ",";

  private AttributesHelper() {}

  public static MultiMap<String, String> copyImmutableMap(MultiMap<String, String> immutableMap, String key, String value) {
    MultiMap<String, String> mapParam = new MultiMap<>();
    immutableMap.keySet().stream().forEach(mapKey -> mapParam.put(mapKey, immutableMap.getAll(mapKey)));

    mapParam.put(key, value);
    return mapParam;
  }

  public static String addQueryString(String oldQueryString, String key, String value) {
    String newParam = oldQueryString.length() != 0 ? "&" : "";
    try {
      newParam += URLEncoder.encode(key, "UTF-8");
      if (value != null) {

        newParam += "=" + URLEncoder.encode(value, "UTF-8");
      }
    } catch (UnsupportedEncodingException e) {
      //UTF-8 will never be unsupported
    }
    return oldQueryString + newParam;
  }

  public static HttpRequestAttributes replaceParams(HttpRequestAttributes attributes, MultiMap<String, String> headers,
                                                    MultiMap<String, String> queryParams, String queryString,
                                                    Map<String, String> uriParams) {
    return new HttpRequestAttributesBuilder(attributes)
        .headers(headers)
        .queryParams(queryParams)
        .queryString(queryString)
        .uriParams(uriParams)
        .build();
  }

  /**
   * Returns the value for the parameter in the map that matches the name. It validates that only one value exists.
   * <p>
   * If multiple values are allowed for the parameter, consider using {@link AttributesHelper#getParamValues(MultiMap, String)} or {@link AttributesHelper#getCommaSeparatedParamValues(MultiMap, String)} instead.
   * </p>
   *
   * @param parameters    Map of parameter's name-value
   * @param parameterName Parameter name
   * @return Parameter value or <code>null</code> if parameter is not found
   * @throws UnsupportedMediaTypeException if multiple values are present for the parameter
   */
  public static String getParamValue(MultiMap<String, String> parameters, String parameterName)
      throws BadRequestException {
    List<String> paramValues = getParamValues(parameters, parameterName);
    int listSize = paramValues.size();
    if (listSize == 0) {
      return null;
    }
    if (listSize > 1 || paramValues.get(0).contains(COMMA_SEPARATOR)) {
      throw new BadRequestException("Multiple values are not allowed for \"" + parameterName
          + "\" header param");
    }
    return paramValues.get(0);
  }

  /**
   * Returns the list of values for the parameter that matches the name.
   *
   * @param parameters    Map of parameter's name-value
   * @param parameterName Parameter name
   * @return List of parameter values or an empty list if parameter is not found
   */
  public static List<String> getParamValues(MultiMap<String, String> parameters, String parameterName) {
    return parameters.keySet().stream()
        .filter(header -> header.equalsIgnoreCase(parameterName))
        .findFirst().map(parameters::getAll)
        .orElse(emptyList());
  }

  /**
   * Returns a comma separated list of values for the parameter that matches the name.
   *
   * @param parameters    Map of parameter's name-value
   * @param parameterName Parameter name
   * @return Comma separated list of values or <code>null</code> if parameter is not found
   */
  public static String getCommaSeparatedParamValues(MultiMap<String, String> parameters, String parameterName) {
    List<String> valuesList = getParamValues(parameters, parameterName);
    return valuesList.isEmpty() ? null : valuesList.stream().collect(Collectors.joining(COMMA_SEPARATOR));
  }

  /**
   * Returns "Accept" header param value.
   *
   * @param headers Map of parameter's name-value
   * @return
   * @throws UnsupportedMediaTypeException
   */
  public static String getContentType(MultiMap<String, String> headers) throws UnsupportedMediaTypeException {
    final String contentType;
    try {
      contentType = getParamValue(headers, CONTENT_TYPE.getName());
    } catch (BadRequestException e) {
      throw new UnsupportedMediaTypeException("Unsupported mediaType. " + e.getMessage());
    }
    return Strings.isNullOrEmpty(contentType) ? null : getMediaType(contentType);
  }

  /**
   * Parses the Media Type and returns it with "type/subtype" format. Parameters are truncated.
   *
   * @param mediaType Media Type
   * @return Media Type with "type/subtype" format
   */
  public static String getMediaType(String mediaType) {
    MediaType mType = parse(mediaType);
    return String.format("%s/%s", mType.getPrimaryType(), mType.getSubType());
  }

  /**
   * Returns "Accept" header param values as a comma separated list. If no values found, defaults to {@literal *}/{@literal *}
   *
   * @param headers Map of parameter's name-value
   * @return Comma separated list of accepted Media Types
   */
  public static String getAcceptedResponseMediaTypes(MultiMap<String, String> headers) {
    String acceptableResponseMediaTypes = getCommaSeparatedParamValues(headers, ACCEPT.getName());
    return Strings.isNullOrEmpty(acceptableResponseMediaTypes) ? ANY_RESPONSE_MEDIA_TYPE : acceptableResponseMediaTypes;
  }

}
