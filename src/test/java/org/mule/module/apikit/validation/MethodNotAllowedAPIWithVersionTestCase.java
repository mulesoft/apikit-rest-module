/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation;

import org.junit.Test;
import org.mule.module.apikit.api.exception.MethodNotAllowedException;
import org.mule.module.apikit.api.exception.MuleRestException;

import static org.hamcrest.CoreMatchers.equalTo;

public class MethodNotAllowedAPIWithVersionTestCase extends AbstractRequestValidatorTestCase {

  @Test
  public void throwMethodNotAllowedException() throws MuleRestException {
    expectedException.expect(MethodNotAllowedException.class);
    expectedException.expectMessage(equalTo("HTTP Method post not allowed for : /path/{version}/job/{jobId}"));
    testRestRequestValidatorBuilder
        .withApiLocation("unit/validation/api-with-version.raml")
        .withRequestPath("/api/path/1.0/job/123")
        .withRelativePath("/path/1.0/job/123")
        .withMethod("POST")
        .build()
        .validateRequest();
  }
}
