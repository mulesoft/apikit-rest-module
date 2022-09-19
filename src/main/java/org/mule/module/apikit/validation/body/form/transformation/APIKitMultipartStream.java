/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form.transformation;

import org.apache.commons.fileupload.MultipartStream;
import org.mule.module.apikit.api.exception.InvalidFormParameterException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static java.lang.System.arraycopy;

/**
 * Extends {@link MultipartStream} to keep preamble and epilogue and to handle they back to the builder.
 */
class APIKitMultipartStream extends MultipartStream {

  private boolean readingPreamble;
  private byte[] preamble;
  private final long maxBytes;

  APIKitMultipartStream(InputStream input, byte[] boundary, int bufSize, long maxBytes) {
    super(input, boundary, bufSize);
    this.preamble = new byte[0];
    this.readingPreamble = false;
    this.maxBytes = maxBytes;
  }

  /**
   * Wraps {@link MultipartStream#skipPreamble()} in order to keep the preamble and handle it back to the {@link MultipartEntityBuilder}.
   *
   * @param multipartEntityBuilder
   * @return true if an encapsulation was found in the stream
   * @throws InvalidFormParameterException
   */
  public boolean readPreamble(MultipartEntityBuilder multipartEntityBuilder) throws InvalidFormParameterException {
    this.readingPreamble = true;
    boolean isNextPart = false;
    try {
      isNextPart = super.skipPreamble();
    } catch (IOException e) {
    }
    if (preamble.length > 0) {
      multipartEntityBuilder.handlePreamble(preamble);
      this.preamble = new byte[0];
    }
    this.readingPreamble = false;
    return isNextPart;
  }

  /**
   * Overrides {@link MultipartStream#discardBodyData()} in order to keep the payload when invoked by {@link MultipartStream#skipPreamble()}
   *
   * @return The amount of data discarded
   * @throws IOException
   */
  @Override
  public int discardBodyData() throws IOException {
    if (!readingPreamble) {
      LimitedByteArrayOutputStream content = new LimitedByteArrayOutputStream(maxBytes);
      return readBodyData(content);
    }
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    int length = super.readBodyData(os);
    this.preamble = os.toByteArray();
    return length;
  }

  /**
   * Reads the remaining bytes of the stream in order to keep the epilogue and handle it back to the {@link MultipartEntityBuilder}.
   *
   * @param multipartEntityBuilder
   * @throws InvalidFormParameterException
   */
  public void readEpilogue(MultipartEntityBuilder multipartEntityBuilder) throws InvalidFormParameterException {
    LimitedByteArrayOutputStream outputStream = new LimitedByteArrayOutputStream(maxBytes);
    int count = 0;
    try {
      while (true) {
        count++;
        outputStream.write(readByte());
      }
    } catch (IOException e) {
      byte[] epilogue = new byte[count - 1];
      try {
        outputStream.flush();
        arraycopy(outputStream.toByteArray(), 0, epilogue, 0, count - 1);
        multipartEntityBuilder.handleEpilogue(epilogue);
      } catch (IOException ex) {
      }
    }
  }
}
