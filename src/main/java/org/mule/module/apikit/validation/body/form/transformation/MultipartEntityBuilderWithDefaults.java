/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form.transformation;

import static org.apache.http.entity.ContentType.parse;
import static org.apache.http.entity.mime.FormBodyPartBuilder.create;

import java.io.IOException;
import org.apache.commons.fileupload.MultipartStream;
import org.apache.http.entity.mime.FormBodyPartBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.mule.apikit.model.parameter.Parameter;
import org.mule.module.apikit.api.exception.InvalidFormParameterException;
import org.mule.runtime.api.metadata.MediaType;

public class MultipartEntityBuilderWithDefaults extends MultipartEntityBuilder {

  private final org.apache.http.entity.mime.MultipartEntityBuilder entityBuilder;

  public MultipartEntityBuilderWithDefaults(String boundary) {
    this.entityBuilder = org.apache.http.entity.mime.MultipartEntityBuilder.create().setBoundary(boundary);
  }


  @Override
  public void handleBinaryPart(MultipartStream multipartStream, Parameter parameter, String name,
                               String contentType, String fileName, String headers)
      throws InvalidFormParameterException {
    try {
      byte[] buf = partToByteArray(multipartStream);

      if (parameter != null) {
        new MultipartFormDataBinaryParameter(buf.length, MediaType.parse(contentType))
            .validate(parameter);
      }

      addPart(name, buf, contentType, fileName, headers);
    } catch (IOException e) {
      throw new InvalidFormParameterException(e);
    }
  }

  @Override
  public void addDefault(String key, String value) {
    entityBuilder.addTextBody(key, value);
  }

  @Override
  public Multipart getOutput() {
    return new MultipartWithDefaults(entityBuilder.build());
  }

  @Override
  protected void addPart(String name, byte[] buf, String contentType, String fileName, String headers) {
    FormBodyPartBuilder formBodyPartBuilder = create(name,
                                                     new ByteArrayBody(buf, parse(contentType), fileName));
    getHeaders(headers).forEach(formBodyPartBuilder::addField);
    entityBuilder.addPart(formBodyPartBuilder.build());

  }
}
