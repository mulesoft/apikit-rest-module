/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form;

import org.apache.commons.io.IOUtils;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.mule.module.apikit.StreamUtils;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.metadata.TypedValue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import static org.junit.Assert.*;

public class MultipartFormValidatorTest {

  public static final String BOUNDARY = "test";
  public static final String MULTIPART_BODY = "--test--\r\n";

  @Test
  public void validate() throws Exception {
    MultipartFormValidator multipartFormValidator = new MultipartFormValidator(Collections.emptyMap());
    TypedValue typedValue = getTypedValue();
    TypedValue validatedTypedValue = multipartFormValidator.validate(typedValue);
    InputStream validatedInputStream = StreamUtils.unwrapCursorStream(TypedValue.unwrap(validatedTypedValue));
    Assert.assertEquals(MULTIPART_BODY, IOUtils.toString(validatedInputStream));
  }

  private TypedValue getTypedValue() {
    DataType dataType = DataType.builder(DataType.INPUT_STREAM)
        .mediaType(MediaType.parse("multipart/form-data; boundary=\"" + BOUNDARY + "\"")).build();
    InputStream in = new ByteArrayInputStream(MULTIPART_BODY.getBytes());
    return new TypedValue(in, dataType);
  }

}
