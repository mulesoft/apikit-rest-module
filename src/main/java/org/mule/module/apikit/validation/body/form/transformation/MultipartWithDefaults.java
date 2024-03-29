/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form.transformation;

import org.apache.http.HttpEntity;
import org.mule.module.apikit.api.exception.InvalidFormParameterException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.OptionalLong;

import static java.nio.ByteBuffer.allocate;
import static org.mule.module.apikit.StreamUtils.CRLF;

/**
 * New Multipart generated from input multipart, adding default values
 */
public class MultipartWithDefaults implements Multipart {

  private final byte[] preamble;
  private final byte[] epilogue;
  private final HttpEntity multipartFormEntity;
  private final long contentLength;

  public MultipartWithDefaults(HttpEntity multipartFormEntity, byte[] preamble, byte[] epilogue, long contentLength) {
    this.preamble = preamble;
    this.epilogue = epilogue;
    this.multipartFormEntity = multipartFormEntity;
    this.contentLength = contentLength;
  }

  @Override
  public InputStream content() throws InvalidFormParameterException {
    try {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream((int) contentLength);
      multipartFormEntity.writeTo(byteArrayOutputStream);
      byte[] rawContent = byteArrayOutputStream.toByteArray();
      return new ByteArrayInputStream(allocate((int) contentLength)
          .put(preamble)
          .put(rawContent, 0, rawContent.length - CRLF.length)
          .put(epilogue)
          .array());
    } catch (IOException e) {
      throw new InvalidFormParameterException(e);
    }
  }

  @Override
  public String contentType() {
    return multipartFormEntity.getContentType().getValue();
  }

  @Override
  public OptionalLong getLength() {
    return OptionalLong.of(contentLength);
  }

}
