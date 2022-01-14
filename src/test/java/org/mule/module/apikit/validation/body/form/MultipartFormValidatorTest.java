/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form;

import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mule.module.apikit.StreamUtils;
import org.mule.module.apikit.input.stream.RewindableInputStream;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.metadata.TypedValue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;

import org.mule.runtime.api.streaming.CursorProvider;
import org.mule.runtime.api.streaming.bytes.CursorStream;
import org.mule.runtime.api.streaming.bytes.CursorStreamProvider;

import static org.junit.Assert.assertEquals;

public class MultipartFormValidatorTest {

  public static final String BOUNDARY = "test";
  public static final String MULTIPART_BODY =
      "--test\r\n" +
          "Content-Disposition: form-data; name=\"file\" filename=\"fileName\"\r\n" +
          "Content-Transfer-Encoding: 8bit\r\n" +
          "Content-Type: text/plain; charset=ISO-8859-1\r\n" +
          "\r\n" +
          "hello world\r\n" +
          "--test\r\n" +
          "Custom-Header: customValue; customAttribute=customAttrValue\r\n" +
          "Content-Disposition: form-data; name=\"part1\"\r\n" +
          "Content-Transfer-Encoding: 8bit\r\n" +
          "Content-Type: text/plain; charset=ISO-8859-1\r\n" +
          "\r\n" +
          "hello world\r\n" +
          "--test--\r\n";

  @Test
  public void validateCursor() throws Exception {
    validateTypedValue(getTypedValue(getCursorStreamProvider()));
  }

  @Test
  public void validateInputStream() throws Exception {
    validateTypedValue(getTypedValue(new RewindableInputStream(new ByteArrayInputStream(MULTIPART_BODY.getBytes()))));
  }

  public void validateTypedValue(TypedValue typedValue) throws Exception {
    MultipartFormValidator multipartFormValidator = new MultipartFormValidator(Collections.emptyMap());
    TypedValue validatedTypedValue = multipartFormValidator.validate(typedValue);
    InputStream validatedInputStream = StreamUtils.unwrapCursorStream(TypedValue.unwrap(validatedTypedValue));
    Assert.assertEquals(MULTIPART_BODY, IOUtils.toString(validatedInputStream));
  }

  private TypedValue getTypedValue(Object value) {
    DataType dataType = DataType.builder(DataType.INPUT_STREAM)
        .mediaType(MediaType.parse("multipart/form-data; boundary=\"" + BOUNDARY + "\"")).build();
    return new TypedValue(value, dataType);
  }

  private CursorStreamProvider getCursorStreamProvider() {
    return new CursorStreamProvider() {

      @Override
      public CursorStream openCursor() {
        return new CursorStream() {

          private final InputStream content = new ByteArrayInputStream(MULTIPART_BODY.getBytes());

          @Override
          public int read() throws IOException {
            return content.read();
          }

          @Override
          public long getPosition() {
            return 0;
          }

          @Override
          public void seek(long position) {

          }

          @Override
          public void release() {

          }

          @Override
          public boolean isReleased() {
            return false;
          }

          @Override
          public CursorProvider getProvider() {
            return null;
          }
        };
      }

      @Override
      public void close() {

      }

      @Override
      public void releaseResources() {

      }

      @Override
      public boolean isClosed() {
        return false;
      }
    };
  }

}
