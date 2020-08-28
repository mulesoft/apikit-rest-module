/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.helpers;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mule.module.apikit.exception.UnsupportedMediaTypeException;

import static org.junit.Assert.assertEquals;

public class AttributesHelperTestCase {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void correctContentType() throws UnsupportedMediaTypeException {
    String mediaType = AttributesHelper.getMediaType("application/json");
    assertEquals(mediaType, "application/json");
  }

  @Test
  public void contentTypeWithSuffixAfterSlash() throws UnsupportedMediaTypeException {
    String mediaType = AttributesHelper.getMediaType("application/incorrectContentType");
    assertEquals(mediaType, "application/incorrectcontenttype");
  }

  @Test
  public void invalidContentTypeWithoutSuffixAfterSlash() throws UnsupportedMediaTypeException {
    expectedException.expect(UnsupportedMediaTypeException.class);
    expectedException.expectMessage("MediaType cannot be parsed: application/");
    AttributesHelper.getMediaType("application/");
  }

  @Test
  public void invalidContentTypeWithoutSuffix() throws UnsupportedMediaTypeException {
    expectedException.expect(UnsupportedMediaTypeException.class);
    expectedException.expectMessage("MediaType cannot be parsed: application");
    AttributesHelper.getMediaType("application");
  }

  @Test
  public void invalidContentTypeWEmpty() throws UnsupportedMediaTypeException {
    expectedException.expect(UnsupportedMediaTypeException.class);
    expectedException.expectMessage("MediaType cannot be parsed: ");
    AttributesHelper.getMediaType("");
  }

  @Test
  public void invalidContentTypeNull() throws UnsupportedMediaTypeException {
    expectedException.expect(UnsupportedMediaTypeException.class);
    expectedException.expectMessage("MediaType is null");
    AttributesHelper.getMediaType(null);
  }

}
