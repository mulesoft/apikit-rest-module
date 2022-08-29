/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form;

import com.google.common.collect.ImmutableSet;
import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.junit.Test;
import org.mule.apikit.model.parameter.FileProperties;
import org.mule.apikit.model.parameter.Parameter;
import org.mule.module.apikit.api.exception.InvalidFormParameterException;
import org.mule.module.apikit.validation.body.form.transformation.MultipartFormDataBinaryParameter;
import org.mule.module.apikit.validation.body.form.transformation.MultipartWithDefaults;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

import static java.nio.file.Paths.get;
import static java.util.Optional.of;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mule.runtime.api.metadata.MediaType.parse;

public class MultipartWithDefaultsBinaryParameterTestCase {

  private static final Path absolutePath = get(new File("").getAbsolutePath(), "src", "test", "resources");

  @Test(expected = InvalidFormParameterException.class)
  public void invalidContentType() throws Exception {
    MultipartFormDataBinaryParameter parameterValidator =
        new MultipartFormDataBinaryParameter("testing payload".getBytes().length, parse("image/jpeg"));

    Parameter parameter = mock(Parameter.class);

    when(parameter.getFileProperties()).thenReturn(of(new FileProperties(0, 0,
                                                                         ImmutableSet.of("image/png"))));

    parameterValidator.validate(parameter);
  }

  @Test
  public void parameterWithoutFileProperties() throws Exception {
    MultipartFormDataBinaryParameter parameterValidator =
        new MultipartFormDataBinaryParameter("testing payload".getBytes().length, parse("image/jpeg"));

    Parameter parameter = mock(Parameter.class);

    when(parameter.getFileProperties()).thenReturn(Optional.empty());

    parameterValidator.validate(parameter);
  }

  @Test
  public void validContentTypeAndNoSizeRestriction() throws Exception {
    MultipartFormDataBinaryParameter parameterValidator =
        new MultipartFormDataBinaryParameter("testing payload".getBytes().length, parse("image/jpeg"));

    Parameter parameter = mock(Parameter.class);

    when(parameter.getFileProperties()).thenReturn(
                                                   of(new FileProperties(0, 0,
                                                                         ImmutableSet.of("image/png", "image/jpeg"))));

    parameterValidator.validate(parameter);
  }

  @Test(expected = InvalidFormParameterException.class)
  public void sizeLowerThanMinLength() throws Exception {
    MultipartFormDataBinaryParameter parameterValidator =
        new MultipartFormDataBinaryParameter("testing payload".getBytes().length, parse("image/jpeg"));

    Parameter parameter = mock(Parameter.class);

    when(parameter.getFileProperties()).thenReturn(
                                                   of(new FileProperties(16, 20,
                                                                         ImmutableSet.of("image/png", "image/jpeg"))));

    parameterValidator.validate(parameter);
  }

  @Test(expected = InvalidFormParameterException.class)
  public void sizeBiggerThanMaxLength() throws Exception {
    MultipartFormDataBinaryParameter parameterValidator =
        new MultipartFormDataBinaryParameter("testing payload".getBytes().length, parse("image/jpeg"));

    Parameter parameter = mock(Parameter.class);

    when(parameter.getFileProperties()).thenReturn(
                                                   of(new FileProperties(10, 14,
                                                                         ImmutableSet.of("image/png", "image/jpeg"))));

    parameterValidator.validate(parameter);
  }

  @Test
  public void validSize() throws Exception {
    MultipartFormDataBinaryParameter parameterValidator =
        new MultipartFormDataBinaryParameter("testing payload".getBytes().length, parse("image/jpeg"));

    Parameter parameter = mock(Parameter.class);

    when(parameter.getFileProperties()).thenReturn(
                                                   of(new FileProperties(15, 15,
                                                                         ImmutableSet.of("image/png", "image/jpeg"))));

    parameterValidator.validate(parameter);
  }

  @Test
  public void anyFileTypeAllowed() throws Exception {
    MultipartFormDataBinaryParameter parameterValidator =
        new MultipartFormDataBinaryParameter("testing payload".getBytes().length, parse("image/jpeg"));

    Parameter parameter = mock(Parameter.class);

    when(parameter.getFileProperties()).thenReturn(
                                                   of(new FileProperties(0, 0,
                                                                         ImmutableSet.of("*/*"))));

    parameterValidator.validate(parameter);
  }

  @Test
  public void emptyFileTypes() throws Exception {
    MultipartFormDataBinaryParameter parameterValidator =
        new MultipartFormDataBinaryParameter("testing payload".getBytes().length, parse("image/jpeg"));

    Parameter parameter = mock(Parameter.class);

    when(parameter.getFileProperties()).thenReturn(
                                                   of(new FileProperties(0, 0,
                                                                         ImmutableSet.of())));

    parameterValidator.validate(parameter);
  }

  @Test
  public void contentSizeIsNotLimited() throws Exception {
    File mockedFile = spy(new File(get(absolutePath.toString(), "munit", "body", "form", "bbva.jpg").toUri()));
    when(mockedFile.length()).thenReturn(2500000L);
    MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
    multipartEntityBuilder.addBinaryBody("testFile", mockedFile);
    HttpEntity httpEntity = multipartEntityBuilder.build();
    MultipartWithDefaults multipartWithDefaults = new MultipartWithDefaults(httpEntity);
    multipartWithDefaults.content();
  }
}
