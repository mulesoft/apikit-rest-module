/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form.transformation;

import org.mule.runtime.api.streaming.CursorProvider;

import java.util.OptionalLong;

/**
 * Wraps input CursorProvider
 */
public class MultipartWithoutDefaults implements Multipart {

  private final String contentType;
  private final CursorProvider content;
  private final OptionalLong contentLength;

  public MultipartWithoutDefaults(String contentType, CursorProvider content, OptionalLong contentLength) {
    this.contentType = contentType;
    this.content = content;
    this.contentLength = contentLength;
  }

  @Override
  public CursorProvider content() {
    return content;
  }

  @Override
  public String contentType() {
    return contentType;
  }

  @Override
  public OptionalLong getLength() {
    return contentLength;
  }
}
