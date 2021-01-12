/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.attributes;

import com.google.common.net.MediaType;
import org.mule.apikit.model.Response;
import org.mule.apikit.model.parameter.Parameter;
import org.mule.module.apikit.HeaderName;
import org.mule.module.apikit.api.exception.InvalidHeaderException;
import org.mule.module.apikit.exception.NotAcceptableException;
import org.mule.module.apikit.helpers.AttributesHelper;
import org.mule.runtime.api.util.MultiMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Joiner.on;
import static com.google.common.collect.Sets.difference;
import static com.google.common.collect.Sets.union;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;
import static org.mule.module.apikit.helpers.AttributesHelper.getSuccessStatus;



public class HeadersValidator {

  public static MultiMap<String, String> validateAndAddDefaults(Map<String, Parameter> headers, Map<String, Response> responses,
                                                                MultiMap<String, String> incomingHeaders,
                                                                boolean headersStrictValidation)
      throws InvalidHeaderException, NotAcceptableException {
    MultiMap<String, String> headersWithDefaults =
        analyseRequestHeaders(headers, incomingHeaders, headersStrictValidation);
    analyseAcceptHeader(responses, headersWithDefaults);
    return headersWithDefaults;
  }

  private static MultiMap<String, String> analyseRequestHeaders(Map<String, Parameter> headers,
                                                                MultiMap<String, String> incomingHeaders,
                                                                boolean headersStrictValidation)
      throws InvalidHeaderException {
    if (headersStrictValidation) {
      validateHeadersStrictly(headers, incomingHeaders);
    }
    MultiMap<String, String> copyIncomingHeaders = incomingHeaders;

    for (Map.Entry<String, Parameter> entry : headers.entrySet()) {
      final String ramlHeader = entry.getKey();
      final Parameter ramlType = entry.getValue();

      if (ramlHeader.contains("{?}")) {
        final String regex = ramlHeader.replace("{?}", ".*");
        for (String incomingHeader : copyIncomingHeaders.keySet()) {
          if (incomingHeader.matches(regex))
            validateHeader(copyIncomingHeaders.getAll(incomingHeader), ramlHeader, ramlType);
        }
      } else {
        final List<String> values = AttributesHelper.getParamValues(copyIncomingHeaders, ramlHeader);
        if (values.isEmpty() && ramlType.isRequired()) {
          throw new InvalidHeaderException("Required header '" + ramlHeader + "' not specified");
        }
        if (values.isEmpty() && ramlType.getDefaultValue() != null) {
          copyIncomingHeaders = AttributesHelper.copyImmutableMap(copyIncomingHeaders, ramlHeader, ramlType.getDefaultValue());
        }
        validateHeader(values, ramlHeader, ramlType);
      }
    }
    return copyIncomingHeaders;

  }

  private static void validateHeadersStrictly(Map<String, Parameter> headers, Map<String, String> incomingHeaders)
      throws InvalidHeaderException {
    //checks that headers are defined in the RAML
    final Set<String> ramlHeaders = headers.keySet().stream()
        .map(String::toLowerCase)
        .collect(toSet());

    final Set<String> templateHeaders = ramlHeaders.stream()
        .filter(header -> header.contains("{?}"))
        .map(header -> header.replace("{?}", ".*"))
        .collect(toSet());

    final Set<String> unmatchedHeaders = incomingHeaders.keySet().stream()
        .filter(header -> templateHeaders.stream().noneMatch(header::matches))
        .collect(toSet());

    final Set<String> standardHeaders = stream(HeaderName.values())
        .map(header -> header.getName().toLowerCase())
        .collect(toSet());

    final Set<String> undefinedHeaders = difference(unmatchedHeaders, union(ramlHeaders, standardHeaders));

    if (!undefinedHeaders.isEmpty()) {
      throw new InvalidHeaderException(format("[%s] %s", on(", ").join(undefinedHeaders),
                                              "headers are not defined in RAML and strict headers validation property is true."));
    }
  }

  private static void validateHeader(List<String> values, String name, Parameter type)
      throws InvalidHeaderException {
    if (values.isEmpty())
      return;

    if (values.size() > 1 && !type.isArray() && !type.isRepeat())
      throw new InvalidHeaderException("Header " + name + " is not repeatable");

    // raml 1.0 array validation
    if (type.isArray()) {
      validateTypeArrayValues(name, values, type);
    } else {
      // single header or repeat
      validateTypeValue(name, values.get(0), type);
    }
  }

  private static void validateTypeArrayValues(String name, List<String> values, Parameter type) throws InvalidHeaderException {
    String yamlArrayValue = values.stream().collect(Collectors.joining("\n- ", "- ", ""));
    validateTypeValue(name, yamlArrayValue, type);
  }

  private static void validateTypeValue(String name, String value, Parameter type) throws InvalidHeaderException {
    if (!type.validate(value)) {
      throw new InvalidHeaderException(format("Invalid value '%s' for header '%s'", value, name));
    }
  }

  private static void analyseAcceptHeader(Map<String, Response> responses, MultiMap<String, String> incomingHeaders)
      throws NotAcceptableException {
    if (responses == null) {
      return;
    }
    List<String> mimeTypes = getResponseMimeTypes(responses);
    if (mimeTypes.isEmpty()) {
      return;
    }
    MediaType bestMatch = MimeTypeParser.bestMatch(mimeTypes, AttributesHelper.getAcceptedResponseMediaTypes(incomingHeaders));
    if (bestMatch == null) {
      throw new NotAcceptableException();
    }
  }

  private static List<String> getResponseMimeTypes(Map<String, Response> responses) {
    String status = getSuccessStatus(responses);
    Response response = responses.get(status);
    if (response != null && response.hasBody()) {
      return new ArrayList<>(response.getBody().keySet());
    }
    return new ArrayList<>();
  }

}
