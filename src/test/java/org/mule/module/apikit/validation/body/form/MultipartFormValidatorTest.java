/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mule.apikit.model.parameter.FileProperties;
import org.mule.apikit.model.parameter.Parameter;
import org.mule.module.apikit.StreamUtils;
import org.mule.module.apikit.api.exception.InvalidFormParameterException;
import org.mule.module.apikit.input.stream.RewindableInputStream;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.streaming.CursorProvider;
import org.mule.runtime.api.streaming.bytes.CursorStream;
import org.mule.runtime.api.streaming.bytes.CursorStreamProvider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Optional.of;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static java.util.Collections.emptyMap;

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

  public static final String MULTIPART_BODY_WITH_DEFAULT =
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
          "--test\r\n" +
          "Content-Disposition: form-data; name=\"part2\"\r\n" +
          "Content-Type: text/plain; charset=ISO-8859-1\r\n" +
          "Content-Transfer-Encoding: 8bit\r\n" +
          "\r\n" +
          "test\r\n" +
          "--test--\r\n";


  @Test
  public void validateCursor() throws Exception {
    validateTypedValue(getTypedValue(getCursorStreamProvider()), emptyMap(), false);
  }

  @Test
  public void validateInputStream() throws Exception {
    validateTypedValue(getTypedValue(new RewindableInputStream(new ByteArrayInputStream(MULTIPART_BODY.getBytes()))),
                       emptyMap(), false);
  }

  @Test
  public void validateRequiredParameters() throws Exception {
    Map<String, List<Parameter>> formParameters = mockFormParameters(false, null);
    validateTypedValue(getTypedValue(new RewindableInputStream(new ByteArrayInputStream(MULTIPART_BODY.getBytes()))),
                       formParameters, false);
  }

  @Test(expected = InvalidFormParameterException.class)
  public void validateMissingRequiredParameters() throws Exception {
    Map<String, List<Parameter>> formParameters = mockFormParameters(true, null);
    validateTypedValue(getTypedValue(new RewindableInputStream(new ByteArrayInputStream(MULTIPART_BODY.getBytes()))),
                       formParameters, false);
  }

  @Test
  public void validateMissingRequiredParametersWithDefault() throws Exception {
    Map<String, List<Parameter>> formParameters = mockFormParameters(true, "test");
    validateTypedValue(getTypedValue(new RewindableInputStream(new ByteArrayInputStream(MULTIPART_BODY.getBytes()))),
                       formParameters, true);
  }

  public void validateTypedValue(TypedValue typedValue, Map<String, List<Parameter>> formParameters, boolean withDefaults)
      throws Exception {
    MultipartFormValidator multipartFormValidator = new MultipartFormValidator(formParameters);
    TypedValue validatedTypedValue = multipartFormValidator.validate(typedValue);
    InputStream validatedInputStream = StreamUtils.unwrapCursorStream(TypedValue.unwrap(validatedTypedValue));
    if (withDefaults) {
      Assert.assertEquals(MULTIPART_BODY_WITH_DEFAULT, IOUtils.toString(validatedInputStream));
    } else {
      Assert.assertEquals(MULTIPART_BODY, IOUtils.toString(validatedInputStream));
    }
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

  private Map<String, List<Parameter>> mockFormParameters(boolean allRequired, String defaultValue) {

    Parameter part1 = mock(Parameter.class);
    Parameter part2 = mock(Parameter.class);

    when(part1.getFileProperties()).thenReturn(
                                               of(new FileProperties(0, 0,
                                                                     ImmutableSet.of("*/*"))));
    when(part2.getFileProperties()).thenReturn(
                                               of(new FileProperties(0, 0,
                                                                     ImmutableSet.of("*/*"))));

    when(part1.isRequired()).thenReturn(true);
    when(part2.isRequired()).thenReturn(allRequired);

    when(part2.getDefaultValue()).thenReturn(defaultValue);

    Map<String, List<Parameter>> formParameters = new HashMap<>();
    formParameters.put("part1", Collections.singletonList(part1));
    formParameters.put("part2", Collections.singletonList(part2));

    return formParameters;
  }

}
