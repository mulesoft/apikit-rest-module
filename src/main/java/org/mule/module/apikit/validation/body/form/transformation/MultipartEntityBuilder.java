/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form.transformation;

import org.apache.commons.fileupload.MultipartStream;
import org.apache.commons.io.IOUtils;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.MinimalField;
import org.mule.apikit.model.parameter.Parameter;
import org.mule.module.apikit.api.exception.InvalidFormParameterException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;
import static org.mule.module.apikit.StreamUtils.BOUNDARY_PREFIX;
import static org.mule.module.apikit.StreamUtils.CRLF;
import static org.mule.module.apikit.StreamUtils.STREAM_TERMINATOR;

public abstract class MultipartEntityBuilder {

  private static Pattern HEADERS_PATTERN = compile("([\\w-]+): (.*)");

  protected byte[] preamble;
  protected byte[] epilogue;
  protected long contentLength;
  protected final long sizeLimit;
  protected final String boundary;

  public MultipartEntityBuilder(String boundary, long sizeLimit) {
    this.boundary = boundary;
    this.preamble = new byte[0];
    this.epilogue = new byte[0];
    this.contentLength = 0;
    this.sizeLimit = sizeLimit;
  }

  public abstract void addDefault(String key, String value) throws InvalidFormParameterException;

  public abstract Multipart getOutput();

  protected abstract void addPart(String name, byte[] buf, String contentType, String fileName, String headers)
      throws InvalidFormParameterException;

  public abstract void handleBinaryPart(MultipartStream multipartStream, Parameter parameter, String name,
                                        String contentType, String fileName, String headers)
      throws InvalidFormParameterException;

  public void handleTextPart(MultipartStream multipartStream, Parameter parameter, String name,
                             String contentType, String fileName, String headers)
      throws InvalidFormParameterException {
    try {
      byte[] buf = partToByteArray(multipartStream);
      if (parameter != null) {
        String body = IOUtils.toString(new ByteArrayInputStream(buf));
        new MultipartFormDataTextParameter(body).validate(parameter);
      }
      addPart(name, buf, contentType, fileName, headers);
    } catch (IOException e) {
      throw new InvalidFormParameterException(e);
    } catch (IndexOutOfBoundsException e) {
      throw new InvalidFormParameterException(e.getMessage());
    }
  }

  protected byte[] partToByteArray(MultipartStream stream) throws IOException {
    LimitedByteArrayOutputStream content = new LimitedByteArrayOutputStream(sizeLimit);
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

  public void handlePart(MultipartStream multipartStream, Parameter parameter, String name, String contentType, String fileName,
                         String headers)
      throws InvalidFormParameterException {
    if (parameter != null && parameter.getFileProperties().isPresent()) {
      handleBinaryPart(multipartStream, parameter, name, contentType, fileName, headers);
    } else {
      handleTextPart(multipartStream, parameter, name, contentType, fileName, headers);
    }
  }

  public void handleBoundary(boolean firstOne) throws InvalidFormParameterException, IOException {
    // Cut CRLF off from prefix as it is the first one
    increaseContentLength(firstOne ? BOUNDARY_PREFIX.length - 2 : BOUNDARY_PREFIX.length);
    increaseContentLength(boundary.length());
  }

  public void handlePreamble(byte[] preamble) throws InvalidFormParameterException {
    increaseContentLength(preamble.length);
    this.preamble = preamble;
  }

  public void handleEpilogue(byte[] epilogue) throws InvalidFormParameterException {
    increaseContentLength(epilogue.length);
    this.epilogue = epilogue;
  }

  public void handleStreamTermination() throws InvalidFormParameterException {
    increaseContentLength(STREAM_TERMINATOR.length);
  }

  protected void handleFormBodyPart(FormBodyPart formBodyPart) throws InvalidFormParameterException {
    increaseContentLength(CRLF.length);
    for (MinimalField field : formBodyPart.getHeader().getFields()) {
      increaseContentLength(field.toString().length() + CRLF.length);
    }
    increaseContentLength(CRLF.length);
    increaseContentLength(formBodyPart.getBody().getContentLength());
  }

  protected void increaseContentLength(long partLength)
      throws InvalidFormParameterException {
    this.contentLength += partLength;
    if (contentLength > sizeLimit) {
      throw new InvalidFormParameterException("Multipart content exceeded the maximum size supported");
    }
  }
}
