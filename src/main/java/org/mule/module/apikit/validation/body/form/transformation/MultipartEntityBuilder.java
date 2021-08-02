/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form.transformation;

import static java.util.regex.Pattern.compile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.fileupload.MultipartStream;
import org.apache.commons.io.IOUtils;
import org.mule.apikit.model.parameter.Parameter;
import org.mule.module.apikit.api.exception.InvalidFormParameterException;

public abstract class MultipartEntityBuilder {

  private static Pattern HEADERS_PATTERN = compile("([\\w-]+): (.*)");

  public abstract void addDefault(String key, String value);

  public abstract Multipart getOutput();

  protected abstract void addPart(String name, byte[] buf, String contentType, String fileName, String headers);

  public abstract void handleBinaryPart(MultipartStream multipartStream, Parameter parameter, String name,
                                        String contentType, String fileName, String headers)
      throws InvalidFormParameterException;

  public void handleTextPart(MultipartStream multipartStream, Parameter parameter, String name,
                             String contentType, String fileName, String headers)
      throws InvalidFormParameterException {
    try {
      byte[] buf = partToByteArray(multipartStream);
      String body = IOUtils.toString(new ByteArrayInputStream(buf));
      if (parameter != null) {
        new MultipartFormDataTextParameter(body).validate(parameter);
      }
      addPart(name, buf, contentType, fileName, headers);
    } catch (IOException e) {
      throw new InvalidFormParameterException(e);
    }
  }

  protected byte[] partToByteArray(MultipartStream stream) throws IOException {
    ByteArrayOutputStream content = new ByteArrayOutputStream();
    stream.readBodyData(content);
    return content.toByteArray();
  }

  protected Map<String, String> getHeaders(String headers) {
    Map<String, String> map = new HashMap<>();
    Matcher matcher = HEADERS_PATTERN.matcher(headers);
    while (matcher.find()) {
      String name = matcher.group(1);
      String value = matcher.group(2);
      map.put(name, value);
    }
    return map;
  }
}
