/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form;

import org.junit.Test;
import org.mule.module.apikit.api.exception.MuleRestException;
import org.mule.module.apikit.validation.TestRestRequestValidator;

import java.util.OptionalLong;

import static org.junit.Assert.assertEquals;

public class MultipartBodyLengthTestCase extends AbstractMultipartRequestValidatorTestCase {

  @Test
  public void preserveMultipartLengthAfterValidationTest() throws MuleRestException {
    TestRestRequestValidator testRestRequestValidator = multipartTestBuilder
        .withApiLocation("unit/multipart.raml")
        .withRelativePath("/test")
        .withTextPart("zipFile", "\nsome.zip\n")
        .build();

    OptionalLong afterValidationBodyLength = testRestRequestValidator
        .validateRequest()
        .getBody()
        .getPayloadAsTypedValue().getByteLength();

    assertEquals(testRestRequestValidator.getRequestBodyLength(), afterValidationBodyLength);
  }

}
