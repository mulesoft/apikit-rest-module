/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.helpers;

import org.junit.Test;
import org.mule.module.apikit.exception.UnsupportedMediaTypeException;

import static org.junit.Assert.assertEquals;

public class AttributesHelperTestCase {

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
  public void invalidContentTypeWithoutSuffixAfterSlash() {
    try {
      AttributesHelper.getMediaType("application/");
    } catch (UnsupportedMediaTypeException e) {
      assertEquals(e.getMessage(), "MediaType cannot be parsed: application/");
    }
  }

  @Test
  public void invalidContentTypeWithoutSuffix() {
    try {
      AttributesHelper.getMediaType("application");
    } catch (UnsupportedMediaTypeException e) {
      assertEquals(e.getMessage(), "MediaType cannot be parsed: application");
    }
  }

  @Test
  public void invalidContentTypeWEmpty() {
    try {
      AttributesHelper.getMediaType("");
    } catch (UnsupportedMediaTypeException e) {
      assertEquals(e.getMessage(), "MediaType cannot be parsed: ");
    }
  }

  @Test
  public void invalidContentTypeNull() {
    try {
      AttributesHelper.getMediaType(null);
    } catch (UnsupportedMediaTypeException e) {
      assertEquals(e.getMessage(), "MediaType is null");
    }
  }

}
