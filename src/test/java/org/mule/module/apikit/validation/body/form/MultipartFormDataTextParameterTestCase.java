/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mule.apikit.model.parameter.Parameter;
import org.mule.module.apikit.api.exception.InvalidFormParameterException;
import org.mule.module.apikit.validation.body.form.transformation.MultipartFormDataTextParameter;

public class MultipartFormDataTextParameterTestCase {

  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  @Test
  public void validateInvalidParameter() throws InvalidFormParameterException {
    expectedEx.expect(InvalidFormParameterException.class);
    expectedEx.expectMessage("invalid value");

    Parameter parameter = mock(Parameter.class);

    when(parameter.validate(any())).thenReturn(false);

    when(parameter.message(any())).thenReturn("invalid value");

    MultipartFormDataTextParameter textParameter = new MultipartFormDataTextParameter("invalid body");

    textParameter.validate(parameter);
  }

  @Test
  public void validateValidParameter() throws InvalidFormParameterException {

    Parameter parameter = mock(Parameter.class);

    when(parameter.validate(any())).thenReturn(true);

    MultipartFormDataTextParameter textParameter = new MultipartFormDataTextParameter("valid body");

    textParameter.validate(parameter);

  }
}
