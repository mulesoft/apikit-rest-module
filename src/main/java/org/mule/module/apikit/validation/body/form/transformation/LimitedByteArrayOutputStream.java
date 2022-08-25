/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form.transformation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Extends {@link ByteArrayOutputStream} to limit the total written size.
 */
class LimitedByteArrayOutputStream extends ByteArrayOutputStream {

  private final long maxBytes;
  private long bytesWritten;

  LimitedByteArrayOutputStream(long maxBytes) {
    this.maxBytes = maxBytes;
  }

  @Override
  public void write(int b) {
    try {
      ensureCapacity(1);
    } catch (IOException e) {
      throw new IndexOutOfBoundsException(e.getMessage());
    }
    super.write(b);
  }

  @Override
  public void write(byte[] b) throws IOException {
    ensureCapacity(b.length);
    super.write(b);
  }

  @Override
  public void write(byte[] b, int off, int len) {
    try {
      ensureCapacity(len);
    } catch (IOException e) {
      throw new IndexOutOfBoundsException(e.getMessage());
    }
    super.write(b, off, len);
  }

  private void ensureCapacity(int len) throws IOException {
    long newBytesWritten = this.bytesWritten + len;
    if (newBytesWritten > this.maxBytes)
      throw new IOException("Multipart content exceeded the maximum size supported");
    this.bytesWritten = newBytesWritten;
  }

}
