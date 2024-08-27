/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.attributes;

import org.mule.apikit.model.Response;
import org.mule.apikit.model.parameter.Parameter;
import org.mule.module.apikit.HeaderName;
import org.mule.module.apikit.api.deserializing.AttributesDeserializingStrategies;
import org.mule.module.apikit.api.exception.InvalidHeaderException;
import org.mule.module.apikit.deserializing.AttributeDeserializer;
import org.mule.module.apikit.deserializing.AttributesDeserializerFactory;
import org.mule.module.apikit.deserializing.MimeType;
import org.mule.module.apikit.exception.NotAcceptableException;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.http.api.domain.CaseInsensitiveMultiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.base.Joiner.on;
import static com.google.common.collect.Sets.difference;
import static com.google.common.collect.Sets.union;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.MapUtils.isEmpty;
import static org.mule.module.apikit.api.deserializing.ArrayHeaderDelimiter.COMMA;
import static org.mule.module.apikit.deserializing.AttributesDeserializingStrategyIdentifier.ARRAY_HEADER_DESERIALIZING_STRATEGY;
import static org.mule.module.apikit.helpers.AttributesHelper.getAcceptedResponseMediaTypes;
import static org.mule.module.apikit.helpers.AttributesHelper.getParamValues;
import static org.mule.runtime.api.util.MultiMap.emptyMultiMap;


public class HeadersValidator {

  private static final Logger LOGGER = LoggerFactory.getLogger(HeadersValidator.class);

  private static final String PLACEHOLDER_TOKEN = "{?}";

  public static MultiMap<String, String> validateAndAddDefaults(Map<String, Parameter> headers, Map<String, Response> responses,
                                                                String successStatusCode,
                                                                MultiMap<String, String> incomingHeaders,
                                                                boolean headersStrictValidation,
                                                                AttributesDeserializingStrategies attributesDeserializingStrategy)
      throws InvalidHeaderException, NotAcceptableException {
    MultiMap<String, String> headersWithDefaults =
        analyseRequestHeaders(headers, incomingHeaders, headersStrictValidation, attributesDeserializingStrategy);
    analyseAcceptHeader(responses, successStatusCode, headersWithDefaults);
    return headersWithDefaults;
  }

  private static MultiMap<String, String> analyseRequestHeaders(Map<String, Parameter> headers,
                                                                MultiMap<String, String> incomingHeaders,
                                                                boolean headersStrictValidation,
                                                                AttributesDeserializingStrategies attributesDeserializingStrategy)
      throws InvalidHeaderException {
    if (headersStrictValidation) {
      validateHeadersStrictly(headers, incomingHeaders);
    }
    MultiMap<String, String> copyIncomingHeaders = emptyMultiMap();

    for (Map.Entry<String, Parameter> entry : headers.entrySet()) {
      final String headerName = entry.getKey();
      final Parameter headerType = entry.getValue();

      if (headerName.contains(PLACEHOLDER_TOKEN)) {
        validateHeadersWithPlaceholderToken(incomingHeaders, headerName,
                                            headerType);
      } else {
        List<String> values = getParamValues(incomingHeaders, headerName);
        if (values.isEmpty() && headerType.isRequired()) {
          throw new InvalidHeaderException("Required header '" + headerName + "' not specified");
        }
        List<String> defaultValues = headerType.getDefaultValues();
        if (values.isEmpty() && !defaultValues.isEmpty()) {
          copyIncomingHeaders = getMutableCopy(incomingHeaders, copyIncomingHeaders);
          for (String defaultValue : defaultValues) {
            copyIncomingHeaders.put(headerName, defaultValue);
          }
        }
        if (!values.isEmpty() && headerType.isArray()) {
          values = deserializeValues(values, attributesDeserializingStrategy);
          copyIncomingHeaders = getMutableCopy(incomingHeaders, copyIncomingHeaders);
          // HTTP listener passes header names in lower case
          copyIncomingHeaders.removeAll(headerName.toLowerCase());
          copyIncomingHeaders.removeAll(headerName);
          // Putting back header name with same case as there is in spec (headers are case-insensitive)
          copyIncomingHeaders.put(headerName.toLowerCase(), values);
        }
        validateHeader(values, headerName, headerType);
      }
    }
    return copyIncomingHeaders.isEmpty() ? incomingHeaders : copyIncomingHeaders;

  }

  private static MultiMap<String, String> getMutableCopy(MultiMap<String, String> incomingHeaders,
                                                         MultiMap<String, String> copyIncomingHeaders) {
    if (copyIncomingHeaders.isEmpty()) {
      MultiMap<String, String> mapParam = new CaseInsensitiveMultiMap();
      mapParam.putAll(incomingHeaders);
      return mapParam;
    }
    return copyIncomingHeaders;
  }

  private static void validateHeadersWithPlaceholderToken(MultiMap<String, String> copyIncomingHeaders, String headerName,
                                                          Parameter headerType)
      throws InvalidHeaderException {
    final String regex = headerName.replace(PLACEHOLDER_TOKEN, ".*");
    for (String incomingHeader : copyIncomingHeaders.keySet()) {
      if (incomingHeader.matches(regex)) {
        validateHeader(copyIncomingHeaders.getAll(incomingHeader), headerName, headerType);
      }
    }
  }

  private static void validateHeadersStrictly(Map<String, Parameter> headers, Map<String, String> incomingHeaders)
      throws InvalidHeaderException {
    // checks that headers are defined in the spec
    final Set<String> specHeaders = headers.keySet().stream()
        .map(String::toLowerCase)
        .collect(toSet());

    final Set<String> templateHeaders = specHeaders.stream()
        .filter(header -> header.contains(PLACEHOLDER_TOKEN))
        .map(header -> header.replace(PLACEHOLDER_TOKEN, ".*"))
        .collect(toSet());

    final Set<String> unmatchedHeaders = incomingHeaders.keySet().stream()
        .filter(header -> templateHeaders.stream().noneMatch(header::matches))
        .collect(toSet());

    final Set<String> standardHeaders = stream(HeaderName.values())
        .map(header -> header.getName().toLowerCase())
        .collect(toSet());

    final Set<String> undefinedHeaders = difference(unmatchedHeaders, union(specHeaders, standardHeaders));

    if (!undefinedHeaders.isEmpty()) {
      throw new InvalidHeaderException(format("[%s] %s", on(", ").join(undefinedHeaders),
                                              "headers are not defined in API spec and strict headers validation property is true."));
    }
  }

  private static void validateHeader(List<String> values, String name, Parameter type)
      throws InvalidHeaderException {
    if (values.isEmpty()) {
      return;
    }
    if (values.size() > 1 && !type.isArray() && !type.isRepeat()) {
      throw new InvalidHeaderException("Header " + name + " is not repeatable");
    }
    if (type.isArray()) {
      validateTypeArrayValues(name, values, type);
    } else {
      validateTypeValue(name, values.get(0), type);
    }
  }

  private static List<String> deserializeValues(List<String> listOfDelimitedValues,
                                                AttributesDeserializingStrategies deserializingStrategies) {
    AttributeDeserializer deserializer =
        AttributesDeserializerFactory.INSTANCE.getDeserializer(ARRAY_HEADER_DESERIALIZING_STRATEGY, deserializingStrategies);
    return deserializer.deserializeListOfValues(listOfDelimitedValues);
  }

  private static void validateTypeArrayValues(String name, List<String> values, Parameter type) throws InvalidHeaderException {
    if (!type.validateArray(values)) {
      throw new InvalidHeaderException(format("Invalid values '%s' for header '%s'", values.stream().collect(joining(",")),
                                              name));
    }
  }

  private static void validateTypeValue(String name, String value, Parameter type) throws InvalidHeaderException {
    if (!type.validate(value)) {
      throw new InvalidHeaderException(format("Invalid value '%s' for header '%s'", value, name));
    }
  }

  private static void analyseAcceptHeader(Map<String, Response> responses, String successStatusCode,
                                          MultiMap<String, String> incomingHeaders)
      throws NotAcceptableException {
    if (isEmpty(responses)) {
      return;
    }
    List<String> mimeTypes = getResponseMimeTypes(responses, successStatusCode);
    if (isEmpty(mimeTypes)) {
      return;
    }
    if (!someRepresentationIsSupported(mimeTypes, getAcceptedResponseMediaTypes(incomingHeaders))) {
      throw new NotAcceptableException();
    }
  }

  private static List<String> getResponseMimeTypes(Map<String, Response> responses, String successStatusCode) {
    Response response = responses.get(successStatusCode);
    if (response != null && response.hasBody()) {
      return new ArrayList<>(response.getBody().keySet());
    }
    return new ArrayList<>();
  }


  public static boolean someRepresentationIsSupported(List<String> supportedRepresentations, String header) {
    List<MimeType> parseResults;
    try {
      parseResults = MimeType.listFrom(header, COMMA.getDelimiterChar());
    } catch (MimeType.MimeTypeParseException e) {
      LOGGER.warn("Failed to parse user-provided MimeType list: {}", header, e);
      return false;
    }

    return supportedRepresentations.stream()
        .flatMap(representation -> {
          try {
            return Stream.of(MimeType.from(representation));
          } catch (MimeType.MimeTypeParseException e) {
            LOGGER.warn("Failed to parse application-provided MimeType: {}", representation, e);
            return Stream.empty();
          }
        })
        .anyMatch(supported -> parseResults.stream().anyMatch(expected -> isCompatible(expected, supported)));
  }

  private static boolean isCompatible(MimeType expected, MimeType supported) {
    return isCompatible(expected.getType(), supported.getType()) && isCompatible(expected.getSubtype(), supported.getSubtype());
  }

  private static boolean isCompatible(String expected, String supported) {
    return expected.equalsIgnoreCase(supported) || "*".equals(expected) || "*".equals(supported);
  }
}
