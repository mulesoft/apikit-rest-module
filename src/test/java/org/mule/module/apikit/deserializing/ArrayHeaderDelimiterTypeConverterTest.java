/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.deserializing;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.mule.module.apikit.deserializing.ArrayHeaderDelimiter.COMMA;

public class ArrayHeaderDelimiterTypeConverterTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  ArrayHeaderDelimiterTypeConverter converter = new ArrayHeaderDelimiterTypeConverter();

  @Test
  public void convertExistingDelimitersSuccessfully() {
    assertEquals(COMMA, converter.convert(","));
  }

  @Test
  public void convertingInvalidDelimiterReturnsNone() {
    expectedException.expect(RuntimeException.class);
    expectedException.expectMessage("Delimiter value not supported.");
    converter.convert(";");
  }
}
