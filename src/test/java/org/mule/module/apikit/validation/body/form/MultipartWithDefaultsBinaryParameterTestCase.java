/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form;

import static java.util.Optional.of;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mule.runtime.api.metadata.MediaType.parse;

import com.google.common.collect.ImmutableSet;
import java.util.Optional;

import org.junit.Test;
import org.mule.apikit.model.parameter.FileProperties;
import org.mule.apikit.model.parameter.Parameter;
import org.mule.module.apikit.api.exception.InvalidFormParameterException;
import org.mule.module.apikit.validation.body.form.transformation.MultipartFormDataBinaryParameter;

public class MultipartWithDefaultsBinaryParameterTestCase {

  @Test(expected = InvalidFormParameterException.class)
  public void invalidContentType() throws Exception {
    Parameter parameter = getParameter(0, 0, "image/png");
    validate(parameter, "image/jpeg");
  }

  @Test
  public void parameterWithoutFileProperties() throws Exception {
    Parameter parameter = mock(Parameter.class);
    when(parameter.getFileProperties()).thenReturn(Optional.empty());
    validate(parameter, "image/jpeg");
  }

  @Test
  public void validContentTypeAndNoSizeRestriction() throws Exception {
    Parameter parameter = getParameter(0, 0, "image/png", "image/jpeg");
    validate(parameter, "image/jpeg");
  }

  @Test(expected = InvalidFormParameterException.class)
  public void sizeLowerThanMinLength() throws Exception {
    Parameter parameter = getParameter(16, 20, "image/png", "image/jpeg");
    validate(parameter, "image/jpeg");
  }

  @Test(expected = InvalidFormParameterException.class)
  public void sizeBiggerThanMaxLength() throws Exception {
    Parameter parameter = getParameter(10, 14, "image/png", "image/jpeg");
    validate(parameter, "image/jpeg");
  }

  @Test
  public void validSize() throws Exception {
    Parameter parameter = getParameter(15, 15, "image/png", "image/jpeg");
    validate(parameter, "image/jpeg");
  }

  @Test
  public void anyFileTypeAllowed() throws Exception {
    Parameter parameter = getParameter(0, 0, "*/*");
    validate(parameter, "image/jpeg");
  }

  @Test
  public void emptyFileTypes() throws Exception {
    Parameter parameter = getParameter(0, 0);
    validate(parameter, "image/jpeg");
  }

  @Test
  public void subtypeWildcard() throws Exception {
    Parameter parameter = getParameter(0, 0, "image/*");
    validate(parameter, "image/jpeg");
  }

  @Test
  public void extraParametersAreIgnored() throws Exception {
    Parameter parameter = getParameter(0, 0, "text/plain");
    validate(parameter, "text/plain; charset=utf8");
  }

  @Test
  public void parameterQuotingIsIgnored() throws Exception {
    Parameter parameter = getParameter(0, 0, "text/plain; my-param=\"my-value\"; another-param=another-value");
    validate(parameter, "text/plain; my-param=my-value; another-param=\"another-value\"");
  }

  @Test
  public void parameterOrderIsNotImportant() throws Exception {
    Parameter parameter = getParameter(0, 0, "text/plain; a=b; b=c");
    validate(parameter, "text/plain; b=c; a=b");
  }

  @Test
  public void parametersAreValidatedAsRequired() throws Exception {
    Parameter parameter = getParameter(0, 0, "text/plain; myParam=test");
    // Parameter missing
    assertThrows(InvalidFormParameterException.class, () -> validate(parameter, "text/plain"));
    // Parameter has wrong value
    assertThrows(InvalidFormParameterException.class, () -> validate(parameter, "text/plain; myparam=toast"));
    // Parameter is ok
    validate(parameter, "text/plain; myparam=\"test\"");
  }

  @Test
  public void charsetParametersAreValidatedRespectingAliases() throws Exception {
    Parameter parameter = getParameter(0, 0, "text/plain; charset=utf8");
    // Parameter missing
    assertThrows(InvalidFormParameterException.class, () -> validate(parameter, "text/plain"));
    // Parameter has wrong value
    assertThrows(InvalidFormParameterException.class, () -> validate(parameter, "text/plain; charset=ascii"));
    // Parameter is ok
    validate(parameter, "text/plain; CharSet=\"utf8\"");
    validate(parameter, "text/plain; CharSet=\"utf-8\"");
    validate(parameter, "text/plain; CharSet=\"UTF-8\"");
  }

  @Test
  public void wildcardTypesValidateParameters() throws Exception {
    Parameter parameter = getParameter(0, 0, "*/*; loves-mule=yes");

    validate(parameter, "text/plain; loves-mule=yes");
    validate(parameter, "image/png; loves-mule=yes; extra-param=ignored");
    assertThrows(InvalidFormParameterException.class, () -> validate(parameter, "text/plain; loves-mule=no"));
    assertThrows(InvalidFormParameterException.class, () -> validate(parameter, "image/png"));
  }

  @Test
  public void wildcardSubtypesValidateParameters() throws Exception {
    Parameter parameter = getParameter(0, 0, "image/*; loves-mule=yes");

    validate(parameter, "image/webp; loves-mule=yes");
    validate(parameter, "image/png; loves-mule=yes; extra-param=ignored");
    assertThrows(InvalidFormParameterException.class, () -> validate(parameter, "image/webp; loves-mule=no"));
    assertThrows(InvalidFormParameterException.class, () -> validate(parameter, "image/png"));
    assertThrows(InvalidFormParameterException.class, () -> validate(parameter, "text/plain; loves-mule=yes"));
  }

  @Test(expected = InvalidFormParameterException.class)
  public void illegalCharsetsFailValidation() throws Exception {
    Parameter parameter = getParameter(0, 0, "text/plain; charset=invalid-charset!");
    validate(parameter, "text/plain; charset=ascii");
  }

  @Test(expected = InvalidFormParameterException.class)
  public void illegalMimeFailValidation() throws Exception {
    Parameter parameter = getParameter(0, 0, "this is not a valid mime!");
    validate(parameter, "text/plain; charset=ascii");
  }

  private Parameter getParameter(int minLength, int maxLength, String ...mediaTypes) {
    Parameter parameter = mock(Parameter.class);
    FileProperties properties = new FileProperties(minLength, maxLength, ImmutableSet.copyOf(mediaTypes));
    when(parameter.getFileProperties()).thenReturn(of(properties));
    return parameter;
  }

  private void validate(Parameter parameter, String mediaType) throws InvalidFormParameterException {
    MultipartFormDataBinaryParameter parameterValidator =
            new MultipartFormDataBinaryParameter("testing payload".getBytes().length, parse(mediaType));
    parameterValidator.validate(parameter);
  }
}
