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
import org.mule.module.apikit.HeaderName;
import org.mule.module.apikit.exception.UnsupportedMediaTypeException;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.util.MultiMap;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.join;
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

  public static String getHeaderIgnoreCase(HttpRequestAttributes attributes, HeaderName name) {
    return getHeaderIgnoreCase(attributes, name.getName());
  }

  public static String getHeaderIgnoreCase(HttpRequestAttributes attributes, String name) {
    final MultiMap<String, String> headers = attributes.getHeaders();
    return getParamIgnoreCase(headers, name);
  }

  public static String getParamIgnoreCase(MultiMap<String, String> parameters, String name) {
    for (String header : parameters.keySet()) {
      if (header.equalsIgnoreCase(name.toLowerCase())) {
        return join(parameters.getAll(header), COMMA_SEPARATOR);
      }
    }
    return null;
  }

  public static List<String> getParamsIgnoreCase(MultiMap<String, String> parameters, String name) {
    return parameters.keySet().stream()
        .filter(header -> header.equalsIgnoreCase(name))
        .findFirst().map(parameters::getAll)
        .orElse(emptyList());
  }

  public static String getMediaType(HttpRequestAttributes attributes) throws UnsupportedMediaTypeException {
    final String contentType = getHeaderIgnoreCase(attributes, CONTENT_TYPE);
    if (contentType != null) {
      if (contentType.contains(COMMA_SEPARATOR)) {
        throw new UnsupportedMediaTypeException();
      }
      return getMediaType(contentType);
    }
    return null;
  }

  public static String getMediaType(String mediaType) {
    MediaType mType = parse(mediaType);
    return String.format("%s/%s", mType.getPrimaryType(), mType.getSubType());
  }

  public static String getAcceptedResponseMediaTypes(MultiMap<String, String> headers) {
    String acceptableResponseMediaTypes = getParamIgnoreCase(headers, "accept");
    if (Strings.isNullOrEmpty(acceptableResponseMediaTypes)) {
      return ANY_RESPONSE_MEDIA_TYPE;
    }
    return acceptableResponseMediaTypes;
  }

}
