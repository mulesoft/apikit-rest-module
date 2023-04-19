/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.helpers;

import com.google.common.base.Strings;
import org.mule.apikit.model.Response;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.extension.http.api.HttpRequestAttributesBuilder;
import org.mule.module.apikit.exception.UnsupportedMediaTypeException;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.util.MultiMap;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.mule.module.apikit.HeaderName.ACCEPT;
import static org.mule.module.apikit.HeaderName.CONTENT_TYPE;
import static org.mule.module.apikit.api.deserializing.ArrayHeaderDelimiter.COMMA;
import static org.mule.runtime.api.metadata.MediaType.parse;

public class AttributesHelper {

  private static final String ANY_RESPONSE_MEDIA_TYPE = "*/*";

  private AttributesHelper() {}

  public static MultiMap<String, String> copyImmutableMap(MultiMap<String, String> immutableMap) {
    MultiMap<String, String> mapParam = new MultiMap<>();
    immutableMap.keySet().forEach(mapKey -> mapParam.put(mapKey, immutableMap.getAll(mapKey)));
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
      // UTF-8 will never be unsupported
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
   * Returns the list of values for the parameter that matches the name.
   *
   * @param parameters Map of parameter's name-value
   * @param parameterName Parameter name
   * @return List of parameter values or an empty list if parameter is not found
   */
  public static List<String> getParamValues(MultiMap<String, String> parameters, String parameterName) {
    return parameters.keySet().stream()
        .filter(header -> header.equalsIgnoreCase(parameterName))
        .map(parameters::getAll)
        .findFirst()
        .orElse(emptyList());
  }

  /**
   * Returns "Content-Type" header param value.
   *
   * @param headers Map of parameter's name-value
   * @return
   * @throws UnsupportedMediaTypeException
   */
  public static String getContentType(MultiMap<String, String> headers) throws UnsupportedMediaTypeException {
    final List<String> contentTypes = getParamValues(headers, CONTENT_TYPE.getName());
    if (isEmpty(contentTypes)) {
      return null;
    }
    String contentType = contentTypes.get(0);
    if (contentTypes.size() > 1 || contentType.contains(COMMA.getDelimiterValue())) {
      throw new UnsupportedMediaTypeException("Unsupported mediaType. Multiple values are not allowed for Content-Type header param");
    }
    return Strings.isNullOrEmpty(contentType) ? null : getMediaType(contentType);
  }

  /**
   * Parses the Media Type and returns it with "type/subtype" format. Parameters are truncated.
   *
   * @param mediaType Media Type
   * @return Media Type with "type/subtype" format
   */
  public static String getMediaType(String mediaType) throws UnsupportedMediaTypeException {
    try {
      MediaType mType = parse(mediaType);
      return String.format("%s/%s", mType.getPrimaryType(), mType.getSubType());
    } catch (Exception e) {
      String message = mediaType == null ? "MediaType is null" : e.getMessage();
      throw new UnsupportedMediaTypeException(message);
    }
  }

  /**
   * Returns "Accept" header param values as a comma separated list. If no values found, defaults to {@literal *}/{@literal *}.
   *
   * @param headers Map of parameter's name-value
   * @return Comma separated list of accepted Media Types
   */
  public static String getAcceptedResponseMediaTypes(MultiMap<String, String> headers) {
    String acceptableResponseMediaTypes =
        getParamValues(headers, ACCEPT.getName()).stream().collect(joining(COMMA.getDelimiterValue()));
    return Strings.isNullOrEmpty(acceptableResponseMediaTypes) ? ANY_RESPONSE_MEDIA_TYPE : acceptableResponseMediaTypes;
  }

  public static String getSuccessStatus(Map<String, Response> responses) {
    for (String status : responses.keySet()) {
      if ("default".equalsIgnoreCase(status)) {
        return "200";
      }
      int code = Integer.parseInt(status);
      if (code >= 200 && code < 300) {
        return status;
      }
    }
    // default success status
    return "200";
  }
}
