/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.deserializing;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mule.module.apikit.deserializing.ArrayHeaderDelimiter.COMMA;
import static org.mule.module.apikit.deserializing.ArrayHeaderDelimiter.NONE;

public class ArrayHeaderDelimiterTypeConverterTest {

  ArrayHeaderDelimiterTypeConverter converter = new ArrayHeaderDelimiterTypeConverter();

  @Test
  public void convertExistingDelimitersSuccessfully() {
    assertEquals(COMMA, converter.convert("COMMA"));
    assertEquals(NONE, converter.convert("NONE"));
  }

  @Test
  public void convertingInvalidDelimiterReturnsNone() {
    assertEquals(NONE, converter.convert("SEMICOLON"));
    assertEquals(NONE, converter.convert("a"));
    assertEquals(NONE, converter.convert("&"));
    assertEquals(NONE, converter.convert("this is a long delimiter"));
  }
}
