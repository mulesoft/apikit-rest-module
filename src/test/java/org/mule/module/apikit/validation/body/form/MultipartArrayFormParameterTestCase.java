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
import org.mule.module.apikit.validation.TestRestRequestValidatorBuilder;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.util.MultiMap;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.OptionalLong;

import static org.junit.Assert.assertEquals;
import static org.mule.runtime.api.metadata.DataType.INPUT_STREAM;

public class MultipartArrayFormParameterTestCase extends AbstractMultipartRequestValidatorTestCase {

  @Test
  public void arrayAsJsonTest() throws MuleRestException {
    multipartTestBuilder
        .withApiLocation("munit/body/form/multipart-form-array/sample-system-api.raml")
        .withRelativePath("/students/101")
        .withTextPart("Details",
                      "[{ \"name\": \"class\", \"value\": \"8th\" }, { \"name\": \"section\", \"value\": \"3A\" }, { \"name\": \"DOB\", \"value\": \"08/28/1970\"} ]")
        .build()
        .validateRequest();
  }

  @Test
  public void arrayAsRepeatedFormParamTest() throws MuleRestException {
    multipartTestBuilder
        .withApiLocation("munit/body/form/multipart-form-array/sample-system-api.raml")
        .withRelativePath("/students/101")
        .withTextPart("Details", "{ \"name\": \"class\", \"value\": \"8th\" }")
        .withTextPart("Details", "{ \"name\": \"section\", \"value\": \"3A\" }")
        .build()
        .validateRequest();
  }

}
