/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form.transformation;

import org.apache.http.HttpEntity;
import org.mule.module.apikit.api.exception.InvalidFormParameterException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * New Multipart generated from input multipart, adding default values
 */
public class MultipartWithDefaults implements Multipart {

  private final HttpEntity multipartFormEntity;

  public MultipartWithDefaults(HttpEntity multipartFormEntity) {
    this.multipartFormEntity = multipartFormEntity;
  }

  @Override
  public InputStream content() throws InvalidFormParameterException {
    try {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      multipartFormEntity.writeTo(byteArrayOutputStream);
      return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    } catch (IOException e) {
      throw new InvalidFormParameterException(e);
    }
  }

  @Override
  public String contentType() {
    return multipartFormEntity.getContentType().getValue();
  }
}
