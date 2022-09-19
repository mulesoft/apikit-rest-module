/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form;

import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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
import java.util.Optional;

import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MultipartFormValidatorTest {

  private static final String MULTIPART_SIZE_LIMIT_PROP_NAME = "apikit.multipart.size.limit";

  public static final String BOUNDARY = "test";

  public static final String PREAMBLE = "This is the preamble.  It is to be ignored, though it \r\n" +
      "     is a handy place for mail composers to include an \r\n" +
      "     explanatory note to non-MIME compliant readers.\r\n";

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

  private static final String EPILOGUE = "This is the epilogue.  It is also to be ignored.\r\n";

  private static final String FULL_MULTIPART = PREAMBLE + MULTIPART_BODY + EPILOGUE;

  private static final String FULL_MULTIPART_WITH_DEFAULTS = PREAMBLE + MULTIPART_BODY_WITH_DEFAULT + EPILOGUE;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @After
  public void after() {
    System.clearProperty(MULTIPART_SIZE_LIMIT_PROP_NAME);
  }

  @Test
  public void validateCursor() throws Exception {
    validateTypedValue(getTypedValue(getCursorStreamProvider(FULL_MULTIPART)), emptyMap(), FULL_MULTIPART);
    validateTypedValue(getTypedValue(getCursorStreamProvider(MULTIPART_BODY)), emptyMap(), MULTIPART_BODY);
  }

  @Test
  public void validateInputStream() throws Exception {
    validateTypedValue(getTypedValue(new RewindableInputStream(new ByteArrayInputStream(FULL_MULTIPART.getBytes()))),
                       emptyMap(), FULL_MULTIPART);
    validateTypedValue(getTypedValue(new RewindableInputStream(new ByteArrayInputStream(MULTIPART_BODY.getBytes()))),
                       emptyMap(), MULTIPART_BODY);
  }

  @Test
  public void validateDefaultParameters() throws Exception {
    Map<String, List<Parameter>> formParameters = mockFormParameters(true, "test");
    validateTypedValue(getTypedValue(getCursorStreamProvider(FULL_MULTIPART)), formParameters, FULL_MULTIPART_WITH_DEFAULTS);
    validateTypedValue(getTypedValue(getCursorStreamProvider(MULTIPART_BODY)), formParameters, MULTIPART_BODY_WITH_DEFAULT);
    validateTypedValue(getTypedValue(new RewindableInputStream(new ByteArrayInputStream(FULL_MULTIPART.getBytes()))),
                       formParameters, FULL_MULTIPART_WITH_DEFAULTS);
    validateTypedValue(getTypedValue(new RewindableInputStream(new ByteArrayInputStream(MULTIPART_BODY.getBytes()))),
                       formParameters, MULTIPART_BODY_WITH_DEFAULT);
  }

  @Test
  public void validateRequiredParameters() throws Exception {
    Map<String, List<Parameter>> formParameters = mockFormParameters(false, null);
    validateTypedValue(getTypedValue(new RewindableInputStream(new ByteArrayInputStream(FULL_MULTIPART.getBytes()))),
                       formParameters, FULL_MULTIPART);
    validateTypedValue(getTypedValue(new RewindableInputStream(new ByteArrayInputStream(MULTIPART_BODY.getBytes()))),
                       formParameters, MULTIPART_BODY);
  }

  @Test
  public void validateMissingRequiredParameters() throws Exception {
    expectedException.expect(InvalidFormParameterException.class);
    expectedException.expectMessage(Matchers.equalTo("Required form parameter part2 not specified"));
    Map<String, List<Parameter>> formParameters = mockFormParameters(true, null);
    validateTypedValue(getTypedValue(new RewindableInputStream(new ByteArrayInputStream(FULL_MULTIPART.getBytes()))),
                       formParameters, FULL_MULTIPART);
    validateTypedValue(getTypedValue(new RewindableInputStream(new ByteArrayInputStream(MULTIPART_BODY.getBytes()))),
                       formParameters, MULTIPART_BODY);
  }

  @Test
  public void validateMissingRequiredParametersWithDefault() throws Exception {
    Map<String, List<Parameter>> formParameters = mockFormParameters(true, "test");
    validateTypedValue(getTypedValue(new RewindableInputStream(new ByteArrayInputStream(FULL_MULTIPART.getBytes()))),
                       formParameters, FULL_MULTIPART_WITH_DEFAULTS);
    validateTypedValue(getTypedValue(new RewindableInputStream(new ByteArrayInputStream(MULTIPART_BODY.getBytes()))),
                       formParameters, MULTIPART_BODY_WITH_DEFAULT);
  }

  @Test
  public void sizeLimitExceededTest() throws Exception {
    expectedException.expect(InvalidFormParameterException.class);
    expectedException.expectMessage(Matchers.equalTo("Multipart content exceeded the maximum size supported"));
    System.setProperty(MULTIPART_SIZE_LIMIT_PROP_NAME, "250");
    validateTypedValue(getTypedValue(getCursorStreamProvider(FULL_MULTIPART)), emptyMap(), FULL_MULTIPART);
  }

  public void validateTypedValue(TypedValue typedValue, Map<String, List<Parameter>> formParameters, String expectedPayload)
      throws Exception {
    MultipartFormValidator multipartFormValidator = new MultipartFormValidator(formParameters);
    TypedValue validatedTypedValue = multipartFormValidator.validate(typedValue);
    long length = validatedTypedValue.getByteLength().orElse(0);
    InputStream validatedInputStream = StreamUtils.unwrapCursorStream(TypedValue.unwrap(validatedTypedValue));
    assertEquals(expectedPayload, IOUtils.toString(validatedInputStream));
    assertEquals(expectedPayload.getBytes().length, length);
  }

  private TypedValue getTypedValue(Object value) {
    DataType dataType = DataType.builder(DataType.INPUT_STREAM)
        .mediaType(MediaType.parse("multipart/form-data; boundary=\"" + BOUNDARY + "\"")).build();
    return new TypedValue(value, dataType);
  }

  private CursorStreamProvider getCursorStreamProvider(String multipartContent) {
    return new CursorStreamProvider() {

      @Override
      public CursorStream openCursor() {
        return new CursorStream() {

          private final InputStream content = new ByteArrayInputStream(multipartContent.getBytes());

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

    when(part1.getFileProperties()).thenReturn(Optional.empty());
    when(part2.getFileProperties()).thenReturn(Optional.empty());

    when(part1.isRequired()).thenReturn(true);
    when(part2.isRequired()).thenReturn(allRequired);

    when(part2.getDefaultValue()).thenReturn(defaultValue);

    when(part1.validate(anyString())).thenReturn(true);
    when(part2.validate(anyString())).thenReturn(true);

    Map<String, List<Parameter>> formParameters = new HashMap<>();
    formParameters.put("part1", Collections.singletonList(part1));
    formParameters.put("part2", Collections.singletonList(part2));

    return formParameters;
  }

}
