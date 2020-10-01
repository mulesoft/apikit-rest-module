/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.attributes;

import com.google.common.net.MediaType;
import org.mule.apikit.model.Action;
import org.mule.apikit.model.Response;
import org.mule.apikit.model.parameter.Parameter;
import org.mule.module.apikit.HeaderName;
import org.mule.module.apikit.api.deserializing.AttributesDeserializingStrategies;
import org.mule.module.apikit.api.exception.InvalidHeaderException;
import org.mule.module.apikit.deserializing.AttributeDeserializer;
import org.mule.module.apikit.deserializing.AttributesDeserializerFactory;
import org.mule.module.apikit.exception.NotAcceptableException;
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
import static org.mule.module.apikit.deserializing.AttributesDeserializingStrategyIdentifier.ARRAY_HEADER_DESERIALIZING_STRATEGY;
import static org.mule.module.apikit.deserializing.MimeTypeParser.bestMatchForAcceptHeader;
import static org.mule.module.apikit.helpers.AttributesHelper.copyImmutableMap;
import static org.mule.module.apikit.helpers.AttributesHelper.getAcceptedResponseMediaTypes;
import static org.mule.module.apikit.helpers.AttributesHelper.getParamValues;
import static org.mule.runtime.api.util.MultiMap.emptyMultiMap;


public class HeadersValidator {

  private static final String PLACEHOLDER_TOKEN = "{?}";
  private final Action action;
  private final List<String> mimeTypes;

  public HeadersValidator(Action action) {
    this.action = action;
    this.mimeTypes = getResponseMimeTypes(action);
  }

  public MultiMap<String, String> validateAndAddDefaults(MultiMap<String, String> incomingHeaders,
                                                         boolean headersStrictValidation,
                                                         AttributesDeserializingStrategies attributesDeserializingStrategy)
      throws InvalidHeaderException, NotAcceptableException {
    MultiMap<String, String> headersWithDefaults =
        analyseRequestHeaders(incomingHeaders, headersStrictValidation, attributesDeserializingStrategy);
    analyseAcceptHeader(headersWithDefaults);
    return headersWithDefaults;
  }

  private MultiMap<String, String> analyseRequestHeaders(MultiMap<String, String> incomingHeaders,
                                                         boolean headersStrictValidation,
                                                         AttributesDeserializingStrategies attributesDeserializingStrategy)
      throws InvalidHeaderException {
    if (headersStrictValidation) {
      validateHeadersStrictly(incomingHeaders);
    }
    MultiMap<String, String> copyIncomingHeaders = emptyMultiMap();

    for (Map.Entry<String, Parameter> entry : action.getHeaders().entrySet()) {
      final String ramlHeader = entry.getKey();
      final Parameter ramlType = entry.getValue();

      if (ramlHeader.contains(PLACEHOLDER_TOKEN)) {
        validateHeadersWithPlaceholderToken(incomingHeaders, ramlHeader,
                                            ramlType);
      } else {
        List<String> values = getParamValues(incomingHeaders, ramlHeader);
        if (values.isEmpty() && ramlType.isRequired()) {
          throw new InvalidHeaderException("Required header '" + ramlHeader + "' not specified");
        }
        if (values.isEmpty() && ramlType.getDefaultValue() != null) {
          copyIncomingHeaders = getMutableCopy(incomingHeaders, copyIncomingHeaders);
          copyIncomingHeaders.put(ramlHeader, ramlType.getDefaultValue());
        }
        if (!values.isEmpty() && ramlType.isArray()) {
          values = deserializeValues(values, attributesDeserializingStrategy);
          copyIncomingHeaders = getMutableCopy(incomingHeaders, copyIncomingHeaders);
          copyIncomingHeaders.remove(ramlHeader);
          copyIncomingHeaders.put(ramlHeader, values);
        }
        validateHeader(values, ramlHeader, ramlType);
      }
    }
    return copyIncomingHeaders.isEmpty() ? incomingHeaders : copyIncomingHeaders;

  }

  private MultiMap<String, String> getMutableCopy(MultiMap<String, String> incomingHeaders,
                                                  MultiMap<String, String> copyIncomingHeaders) {
    if (copyIncomingHeaders.isEmpty()) {
      return copyImmutableMap(incomingHeaders);
    }
    return copyIncomingHeaders;
  }

  private void validateHeadersWithPlaceholderToken(MultiMap<String, String> copyIncomingHeaders, String ramlHeader,
                                                   Parameter ramlType)
      throws InvalidHeaderException {
    final String regex = ramlHeader.replace(PLACEHOLDER_TOKEN, ".*");
    for (String incomingHeader : copyIncomingHeaders.keySet()) {
      if (incomingHeader.matches(regex)) {
        validateHeader(copyIncomingHeaders.getAll(incomingHeader), ramlHeader, ramlType);
      }
    }
  }

  private void validateHeadersStrictly(Map<String, String> headers) throws InvalidHeaderException {
    //checks that headers are defined in the RAML
    final Set<String> ramlHeaders = action.getHeaders().keySet().stream()
        .map(String::toLowerCase)
        .collect(toSet());

    final Set<String> templateHeaders = ramlHeaders.stream()
        .filter(header -> header.contains(PLACEHOLDER_TOKEN))
        .map(header -> header.replace(PLACEHOLDER_TOKEN, ".*"))
        .collect(toSet());

    final Set<String> unmatchedHeaders = headers.keySet().stream()
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

  private void validateHeader(List<String> values, String name, Parameter type)
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

  private List<String> deserializeValues(List<String> listOfDelimitedValues,
                                         AttributesDeserializingStrategies deserializingStrategies) {
    AttributeDeserializer deserializer =
        AttributesDeserializerFactory.INSTANCE.getDeserializer(ARRAY_HEADER_DESERIALIZING_STRATEGY, deserializingStrategies);
    return deserializer.deserializeListOfValues(listOfDelimitedValues);
  }

  private void validateTypeArrayValues(String name, List<String> values, Parameter type) throws InvalidHeaderException {
    String yamlArrayValue = values.stream().collect(Collectors.joining("\n- ", "- ", ""));
    validateTypeValue(name, yamlArrayValue, type);
  }

  private void validateTypeValue(String name, String value, Parameter type) throws InvalidHeaderException {
    if (!type.validate(value)) {
      throw new InvalidHeaderException(format("Invalid value '%s' for header '%s'", value, name));
    }
  }

  private void analyseAcceptHeader(MultiMap<String, String> incomingHeaders) throws NotAcceptableException {
    if (action == null || action.getResponses() == null || mimeTypes.isEmpty()) {
      //no response media-types defined, return no body
      return;
    }
    MediaType bestMatch = bestMatchForAcceptHeader(mimeTypes, getAcceptedResponseMediaTypes(incomingHeaders));
    if (bestMatch == null) {
      throw new NotAcceptableException();
    }
  }

  private List<String> getResponseMimeTypes(Action action) {
    String status = getSuccessStatus(action);
    Response response = action.getResponses().get(status);
    if (response != null && response.hasBody()) {
      return new ArrayList<>(response.getBody().keySet());
    }
    return new ArrayList<>();
  }

  protected String getSuccessStatus(Action action) {
    for (String status : action.getResponses().keySet()) {
      if ("default".equalsIgnoreCase(status)) {
        return "200";
      }
      int code = Integer.parseInt(status);
      if (code >= 200 && code < 300) {
        return status;
      }
    }
    //default success status
    return "200";
  }

}
