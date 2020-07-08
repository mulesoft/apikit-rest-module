/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation;

import static org.hamcrest.core.IsEqual.equalTo;

import java.io.InputStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mule.module.apikit.api.exception.MethodNotAllowedException;
import org.mule.module.apikit.api.exception.MuleRestException;

public class MethodNotAllowedAPIWithVersionTestCase extends AbstractRequestValidatorTestCase {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Override
  protected String getPath() {
    return "/api/path/1.0/job/123";
  }

  protected String getRelativePath() {
    return "/path/1.0/job/123";
  }

  @Override
  protected String getMethod() {
    return "POST";
  }

  @Override
  protected String getApiLocation() {
    return "unit/validation/api-with-version.raml";
  }

  @Override
  protected InputStream getBody() {
    return null;
  }

  @Test
  public void throwMethodNotAllowedException() throws MuleRestException {
    expectedException.expect(MethodNotAllowedException.class);
    expectedException.expectMessage(equalTo("HTTP Method post not allowed for : /path/{version}/job/{jobId}"));
    super.validateRequest();
  }
}
