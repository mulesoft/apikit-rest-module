/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form;

import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.mule.module.apikit.validation.TestRestRequestValidator;
import org.mule.module.apikit.validation.TestRestRequestValidatorBuilder;
import org.mule.parser.service.ParserMode;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.streaming.CursorProvider;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.core.api.streaming.bytes.ByteBufferManager;
import org.mule.runtime.core.api.streaming.bytes.InMemoryCursorStreamConfig;
import org.mule.runtime.core.api.streaming.bytes.InMemoryCursorStreamProvider;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.OptionalLong;

public class MultipartTestBuilder {

  private final MultipartEntityBuilder multipartEntityBuilder;
  private final TestRestRequestValidatorBuilder testRestRequestValidatorBuilder;

  public MultipartTestBuilder() {
    this.multipartEntityBuilder = MultipartEntityBuilder.create();
    this.testRestRequestValidatorBuilder = new TestRestRequestValidatorBuilder();
  }

  MultipartTestBuilder withTextPart(String name, String content) {
    multipartEntityBuilder.addTextBody(name, content);
    return this;
  }

  MultipartTestBuilder withApiLocation(String apiLocation) {
    testRestRequestValidatorBuilder.withApiLocation(apiLocation);
    return this;
  }

  MultipartTestBuilder withRelativePath(String relativePath) {
    testRestRequestValidatorBuilder.withRelativePath(relativePath);
    return this;
  }

  public MultipartTestBuilder withParser(ParserMode parser) {
    testRestRequestValidatorBuilder.withParser(parser);
    return this;
  }


  public TestRestRequestValidator build() {
    HttpEntity multipart = multipartEntityBuilder.build();
    String contentType = multipart.getContentType().getValue();
    TypedValue typedValue = getTypedValue(multipart, contentType);

    return testRestRequestValidatorBuilder
        .withMethod("POST")
        .withHeaders(new MultiMap<>(Collections.singletonMap("Content-Type", contentType)))
        .withBody(typedValue)
        .build();
  }

  private static TypedValue getTypedValue(HttpEntity multipart, String contentType) {
    MediaType mediaType = MediaType.parse(contentType);

    CursorProvider cursorProvider = new InMemoryCursorStreamProvider(getContent(multipart),
                                                                     InMemoryCursorStreamConfig.getDefault(),
                                                                     new DummyByteBufferManager());

    DataType dataType = DataType
        .builder(DataType.CURSOR_STREAM_PROVIDER)
        .mediaType(mediaType)
        .build();
    TypedValue typedValue = new TypedValue<>(cursorProvider, dataType, OptionalLong.of(multipart.getContentLength()));
    return typedValue;
  }

  private static InputStream getContent(HttpEntity multipart) {
    try {
      return multipart.getContent();
    } catch (IOException e) {
      throw new RuntimeException("Error generating multipart content", e);
    }
  }

  private static class DummyByteBufferManager implements ByteBufferManager {

    @Override
    public ByteBuffer allocate(int capacity) {
      return ByteBuffer.allocate(capacity);
    }

    @Override
    public void deallocate(ByteBuffer byteBuffer) {

    }
  }
}
