/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form.transformation;

import org.apache.commons.fileupload.MultipartStream;
import org.mule.apikit.model.parameter.Parameter;
import org.mule.module.apikit.api.exception.InvalidFormParameterException;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.streaming.CursorProvider;

import java.io.IOException;
import java.util.OptionalLong;

import static org.mule.module.apikit.StreamUtils.CRLF;

public class MultipartEntityBuilderWithoutDefaults extends MultipartEntityBuilder {

  private final String contentType;
  private final CursorProvider content;
  private final OptionalLong byteLength;


  public MultipartEntityBuilderWithoutDefaults(String contentType, CursorProvider content, String boundary, long sizeLimit,
                                               OptionalLong byteLength) {
    super(boundary, sizeLimit);
    this.contentType = contentType;
    this.content = content;
    this.byteLength = byteLength;
  }

  @Override
  public void handleBinaryPart(MultipartStream multipartStream, Parameter parameter, String name,
                               String contentType, String fileName, String headers)
      throws InvalidFormParameterException {
    try {
      int partLength = multipartStream.discardBodyData();
      if (parameter != null) {
        new MultipartFormDataBinaryParameter(partLength,
                                             MediaType.parse(contentType)).validate(parameter);
      }
      increaseContentLength(partLength);
    } catch (IOException e) {
      throw new InvalidFormParameterException(e);
    } catch (IndexOutOfBoundsException e) {
      throw new InvalidFormParameterException(e.getMessage());
    }
  }

  @Override
  public void addDefault(String key, String value) {}

  @Override
  public Multipart getOutput() {
    return new MultipartWithoutDefaults(contentType, content, byteLength);
  }

  @Override
  protected void addPart(String name, byte[] buf, String contentType, String fileName, String headers)
      throws InvalidFormParameterException {
    increaseContentLength(CRLF.length);
    increaseContentLength(headers.length());
    increaseContentLength(buf.length);
  }
}
