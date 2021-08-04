/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form.transformation;

import static java.util.regex.Pattern.compile;
import static org.mule.module.apikit.StreamUtils.BUFFER_SIZE;
import static org.mule.runtime.api.metadata.MediaType.TEXT;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.fileupload.MultipartStream;
import org.apache.http.entity.mime.MIME;
import org.mule.apikit.model.parameter.Parameter;
import org.mule.module.apikit.api.exception.InvalidFormParameterException;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.streaming.bytes.CursorStreamProvider;

public class MultipartBuilder {

  private final String boundary;
  private final String contentType;
  private final CursorStreamProvider cursorProvider;
  private final Map<String, String> defaultValues = new HashMap<>();
  private final Map<String, Parameter> formParameters = new HashMap<>();

  private static Pattern NAME_PATTERN = compile("Content-Disposition:\\s*form-data;[^\\n]*\\sname=([^\\n;]*?)[;\\n\\s]");
  private static Pattern FILE_NAME_PATTERN = compile("filename=\"([^\"]+)\"");
  private static Pattern CONTENT_TYPE_PATTERN = compile("Content-Type:\\s*([^\\n]+)");

  public MultipartBuilder(CursorStreamProvider cursorProvider, String contentType, String boundary) {
    this.cursorProvider = cursorProvider;
    this.boundary = boundary;
    this.contentType = contentType;
  }

  public MultipartBuilder withDefaultValue(String key, String value) {
    defaultValues.put(key, value);
    return this;
  }

  public MultipartBuilder withExpectedParameter(String expectedKey, Parameter parameter) {
    formParameters.put(expectedKey, parameter);
    return this;
  }

  /**
   * Create the Multipart Content processing the incoming multipart
   * @return @Multipart
   * @throws InvalidFormParameterException
   */
  public Multipart build() throws InvalidFormParameterException {
    try {
      MultipartStream multipartStream =
          new MultipartStream(cursorProvider.openCursor(), boundary.getBytes(MIME.UTF8_CHARSET), BUFFER_SIZE);

      boolean nextPart = multipartStream.skipPreamble();

      Set<String> parametersInPayload = new HashSet<>();

      MultipartEntityBuilder multipartEntityBuilder =
          defaultValues.size() == 0 ? new MultipartEntityBuilderWithoutDefaults(contentType, cursorProvider)
              : new MultipartEntityBuilderWithDefaults(boundary);

      while (nextPart) {

        String headers = multipartStream.readHeaders();
        String name = getName(headers);
        String fileName = getFileName(headers);
        String contentType = getContentType(headers);

        parametersInPayload.add(name);

        Parameter parameter = formParameters.get(name);
        if (parameter != null && parameter.getFileProperties().isPresent()) {
          multipartEntityBuilder.handleBinaryPart(multipartStream, parameter, name, contentType, fileName, headers);
        } else {
          multipartEntityBuilder.handleTextPart(multipartStream, parameter, name, contentType, fileName, headers);
        }

        nextPart = multipartStream.readBoundary();
      }

      for (Entry<String, String> defaultValue : defaultValues.entrySet()) {
        if (!parametersInPayload.contains(defaultValue.getKey())) {
          multipartEntityBuilder.addDefault(defaultValue.getKey(), defaultValue.getValue());
        }
      }

      return multipartEntityBuilder.getOutput();
    } catch (IOException e) {
      throw new InvalidFormParameterException(e);
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
