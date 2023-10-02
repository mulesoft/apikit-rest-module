/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mule.parser.service.ParserMode;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public abstract class AbstractMultipartRequestValidatorTestCase {

  protected MultipartTestBuilder multipartTestBuilder;

  @Parameterized.Parameter(0)
  public ParserMode parser;

  @Parameterized.Parameters(name = "{0}")
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
        {ParserMode.AMF},
        {ParserMode.RAML}
    });
  }

  @Before
  public void setup() {
    this.multipartTestBuilder = new MultipartTestBuilder();
    multipartTestBuilder.withParser(parser);
  }
}
