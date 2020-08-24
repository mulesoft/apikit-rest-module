/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.parsing;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mule.module.apikit.parsing.ArrayHeaderDelimiter.COMMA;
import static org.mule.module.apikit.parsing.ArrayHeaderDelimiter.NONE;

public class ArrayHeaderDelimiterTypeConverterTest {

  ArrayHeaderDelimiterTypeConverter converter = new ArrayHeaderDelimiterTypeConverter();

  @Test
  public void convertExistingDelimitersSuccessfully() {
    assertEquals(COMMA, converter.convert(","));
    assertEquals(NONE, converter.convert(""));
  }

  @Test
  public void convertingInvalidDelimiterReturnsNone() {
    assertEquals(NONE, converter.convert("a"));
    assertEquals(NONE, converter.convert("&"));
    assertEquals(NONE, converter.convert("this is a long delimiter"));
  }
}
