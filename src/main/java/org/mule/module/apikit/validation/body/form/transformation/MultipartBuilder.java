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

  private static Pattern NAME_PATTERN = compile("Content-Disposition:\\s*form-data;[^\\n]*\\sname=([^\\n;]*?)[;\\n\\s]");
  private static Pattern FILE_NAME_PATTERN = compile("filename=\"([^\"]+)\"");
  private static Pattern CONTENT_TYPE_PATTERN = compile("Content-Type:\\s*([^\\n]+)");
  private CursorStreamProvider cursorProvider;
  private InputStream inputStream;
  private final long sizeLimit;


  public MultipartBuilder(String contentType, String boundary) {
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

      Set<String> parametersInPayload = new HashSet<>();
      MultipartEntityBuilder multipartEntityBuilder =
          defaultValues.isEmpty() && cursorProvider != null
              ? new MultipartEntityBuilderWithoutDefaults(contentType, cursorProvider, boundary, sizeLimit)
              : new MultipartEntityBuilderWithDefaults(boundary, sizeLimit);

      boolean nextPart = multipartStream.readPreamble(multipartEntityBuilder);
      multipartEntityBuilder.handleBoundary(true);

      while (nextPart) {
        String headers = multipartStream.readHeaders();
        String name = getName(headers);
        String fileName = getFileName(headers);
        String contentType = getContentType(headers);

        parametersInPayload.add(name);

        multipartEntityBuilder.handlePart(multipartStream, formParameters.get(name), name, contentType, fileName, headers);

        nextPart = multipartStream.readBoundary();
        multipartEntityBuilder.handleBoundary(false);
      }

      for (Entry<String, String> defaultValue : defaultValues.entrySet()) {
        if (!parametersInPayload.contains(defaultValue.getKey())) {
          multipartEntityBuilder.addDefault(defaultValue.getKey(), defaultValue.getValue());
          multipartEntityBuilder.handleBoundary(false);
        }
      }

      multipartEntityBuilder.handleStreamTermination();
      multipartStream.readEpilogue(multipartEntityBuilder);

      for (Entry<String, Parameter> formParameter : formParameters.entrySet()) {
        if (!parametersInPayload.contains(formParameter.getKey()) && formParameter.getValue().isRequired()
            && formParameter.getValue().getDefaultValues().isEmpty()) {
          throw new InvalidFormParameterException("Required form parameter " + formParameter.getKey() + " not specified");
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
