/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mule.apikit.ApiType;
import org.mule.module.apikit.exception.NotAcceptableException;

public class ConfigurationTestCase {

  @Test
  public void avoidNullPointerWhenConfigNotInitialised() {
    Configuration configuration = new Configuration();

    assertThat(configuration.getType(), equalTo(ApiType.AMF));
  }

  @Test
  public void testNotAcceptableException() {
    NotAcceptableException notAcceptableException = new NotAcceptableException();
    Assertions.assertEquals(notAcceptableException.getStringRepresentation(), NotAcceptableException.STRING_REPRESENTATION);
  }
}
