/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.schema;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mule.apikit.validation.ApiValidationResult;
import org.mule.module.apikit.validation.body.schema.v2.RestSchemaV2Validator;
import org.mule.apikit.model.MimeType;
import org.mule.module.apikit.api.exception.BadRequestException;

import static java.util.Arrays.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RestSchemaV2ValidatorTestCase {

  @Rule
  public ExpectedException expected = ExpectedException.none();

  @Test
  public void buildLogMessage() throws Exception {
    expected.expect(BadRequestException.class);
    expected.expectMessage("Message 0\nMessage 1");

    MimeType mimeTypeMock = mock(MimeType.class);
    RestSchemaV2Validator validator = new RestSchemaV2Validator(mimeTypeMock);

    ApiValidationResult validationResult0 = mock(ApiValidationResult.class);
    when(validationResult0.getMessage()).thenReturn("Message 0");
    ApiValidationResult validationResult1 = mock(ApiValidationResult.class);
    when(validationResult1.getMessage()).thenReturn("Message 1");

    when(mimeTypeMock.validate(any(String.class))).thenReturn(
        asList(validationResult0, validationResult1));

    validator.validate("{\"test\" : \"test\"}");
  }

}
