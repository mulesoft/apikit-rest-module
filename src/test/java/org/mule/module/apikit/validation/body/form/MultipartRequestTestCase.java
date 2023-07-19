/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form;

import org.junit.Test;
import org.mule.module.apikit.api.exception.MuleRestException;
import org.mule.module.apikit.api.validation.ValidRequest;
import org.mule.module.apikit.validation.AbstractRequestValidatorTestCase;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.util.MultiMap;

import java.util.Collections;

public class MultipartRequestTestCase extends AbstractRequestValidatorTestCase {

  public static final String BODY = "----------------------------512757427991453558852058\n" +
      "Content-Disposition: form-data; name=\"zipFile\"\n" +
      "\n" +
      "hello\n" +
      "----------------------------512757427991453558852058--";
  public static final String CONTENT_TYPE =
      "multipart/form-data; charset=UTF-8; boundary=\"--------------------------512757427991453558852058\"";

  @Test
  public void testLength() throws MuleRestException {
    ValidRequest validRequest = testRestRequestValidatorBuilder.withApiLocation("unit/multipart.raml")
        .withRequestPath("/api/test")
        .withRelativePath("/test")
        .withMethod("POST")
        .withBodyAsTypedValue(BODY, CONTENT_TYPE)
        .withHeaders(new MultiMap<>(Collections.singletonMap("Content-Type", CONTENT_TYPE)))
        .build().validateRequest();
    TypedValue typedValue = validRequest.getBody().getPayloadAsTypedValue();
    typedValue.getByteLength();
  }

}
