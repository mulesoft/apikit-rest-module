/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form.transformation;

import org.apache.http.entity.mime.MIME;
import org.mule.apikit.model.parameter.Parameter;
import org.mule.module.apikit.api.exception.InvalidFormParameterException;
import org.mule.runtime.api.streaming.bytes.CursorStreamProvider;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Long.valueOf;
import static java.lang.System.getProperty;
import static java.util.regex.Pattern.compile;
import static org.mule.module.apikit.StreamUtils.BUFFER_SIZE;
import static org.mule.runtime.api.metadata.MediaType.TEXT;

public class MultipartBuilder {

  /**
   * Name of system property that sets up a limit size for the multipart payload.
   */
  private static String MULTIPART_SIZE_LIMIT_PROPERTY_NAME = "apikit.multipart.size.limit";
  /**
   * Default value for system property {@link MultipartBuilder#MULTIPART_SIZE_LIMIT_PROPERTY_NAME} set to 256MiB.
   */
  private static String MULTIPART_SIZE_LIMIT_DEFAULT = "268435456";
  private final String boundary;
  private final String contentType;
  private final Map<String, String> defaultValues = new HashMap<>();
  private final Map<String, Parameter> formParameters = new HashMap<>();

  private static Pattern NAME_PATTERN = compile("(?i)Content-Disposition:\\s*form-data;[^\\n]*\\sname=([^\\n;]*?)[;\\n\\s]");
  private static Pattern FILE_NAME_PATTERN = compile("(?i)filename=\"([^\"]+)\"");
  private static Pattern CONTENT_TYPE_PATTERN = compile("(?i)Content-Type:\\s*([^\\n]+)");
  private final OptionalLong byteLength;
  private CursorStreamProvider cursorProvider;
  private InputStream inputStream;
  private final long sizeLimit;


  public MultipartBuilder(String contentType, String boundary, OptionalLong byteLength) {
    this.byteLength = byteLength;
    this.boundary = boundary;
    this.contentType = contentType;
    this.sizeLimit = valueOf(getProperty(MULTIPART_SIZE_LIMIT_PROPERTY_NAME, MULTIPART_SIZE_LIMIT_DEFAULT));
  }

  public MultipartBuilder withDefaultValue(String key, String value) {
    defaultValues.put(key, value);
    return this;
  }

  public MultipartBuilder withExpectedParameter(String expectedKey, Parameter parameter) {
    formParameters.put(expectedKey, parameter);
    return this;
  }

  public MultipartBuilder withInputStream(InputStream inputStream) {
    this.inputStream = inputStream;
    return this;
  }

  public MultipartBuilder withCursorProvider(CursorStreamProvider cursorProvider) {
    this.cursorProvider = cursorProvider;
    return this;
  }

  /**
   * Create the Multipart Content processing the incoming multipart
   * 
   * @return @Multipart
   * @throws InvalidFormParameterException
   */
  public Multipart build() throws InvalidFormParameterException {
    try {
      InputStream inputStream = cursorProvider != null ? cursorProvider.openCursor() : this.inputStream;
      APIKitMultipartStream multipartStream =
          new APIKitMultipartStream(inputStream, boundary.getBytes(MIME.UTF8_CHARSET), BUFFER_SIZE, sizeLimit);

      Map<String, Integer> parametersInPayloadToCount = new HashMap<>();
      MultipartEntityBuilder multipartEntityBuilder =
          defaultValues.isEmpty() && cursorProvider != null
              ? new MultipartEntityBuilderWithoutDefaults(contentType, cursorProvider, boundary, sizeLimit, byteLength)
              : new MultipartEntityBuilderWithDefaults(boundary, sizeLimit);

      boolean nextPart = multipartStream.readPreamble(multipartEntityBuilder);
      multipartEntityBuilder.handleBoundary(true);

      while (nextPart) {
        String headers = multipartStream.readHeaders();
        String name = getName(headers);
        String fileName = getFileName(headers);
        String contentType = getContentType(headers);

        parametersInPayloadToCount.put(name, parametersInPayloadToCount.getOrDefault(name, 0) + 1);

        multipartEntityBuilder.handlePart(multipartStream, formParameters.get(name), name, contentType, fileName, headers);

        nextPart = multipartStream.readBoundary(); //Checking the next part items here
        multipartEntityBuilder.handleBoundary(false);
      }

      for (Entry<String, String> defaultValue : defaultValues.entrySet()) {
        if (!parametersInPayloadToCount.containsKey(defaultValue.getKey())) {
          multipartEntityBuilder.addDefault(defaultValue.getKey(), defaultValue.getValue());
          multipartEntityBuilder.handleBoundary(false);
        }
      }

      multipartEntityBuilder.handleStreamTermination();
      multipartStream.readEpilogue(multipartEntityBuilder);

      for (Entry<String, Parameter> formParameter : formParameters.entrySet()) {
        if (!parametersInPayloadToCount.containsKey(formParameter.getKey()) && formParameter.getValue().isRequired()
            && formParameter.getValue().getDefaultValues().isEmpty()) {
          throw new InvalidFormParameterException("Required form parameter " + formParameter.getKey() + " not specified");//We can also validate the minItems and maxItem count here
        } else if (parametersInPayloadToCount.containsKey(formParameter.getKey()) && formParameter.getValue().isRequired()) {
          Optional<Integer> minItemsCount = formParameter.getValue().getMinItems();
          Optional<Integer> maxItemsCount = formParameter.getValue().getMaxItems();
          if (minItemsCount.isPresent() && maxItemsCount.isPresent()
                  && (minItemsCount.get() > parametersInPayloadToCount.get(formParameter.getKey())
                  || parametersInPayloadToCount.get(formParameter.getKey()) > maxItemsCount.get())) {
            throw new InvalidFormParameterException("parameter does not comply with minItems and maxItems for " + formParameter.getKey());
          }
        }
      }

      return multipartEntityBuilder.getOutput();
    } catch (IOException e) {
      throw new InvalidFormParameterException(e);
    } catch (IndexOutOfBoundsException e) {
      throw new InvalidFormParameterException(e.getMessage());
    }
  }

  private String getFileName(String headers) {
    Matcher matcher = FILE_NAME_PATTERN.matcher(headers);
    return !matcher.find() ? null : matcher.group(1).replace("\"", "").replace("'", "");
  }

  private String getName(String headers) throws InvalidFormParameterException {
    Matcher matcher = NAME_PATTERN.matcher(headers);
    if (!matcher.find()) {
      throw new InvalidFormParameterException("Unable to get name from form-data");
    }
    return matcher.group(1).replace("\"", "").replace("'", "");
  }

  private String getContentType(String headers) {
    Matcher matcher = CONTENT_TYPE_PATTERN.matcher(headers);
    return !matcher.find() ? TEXT.toString() : matcher.group(1);
  }
}
