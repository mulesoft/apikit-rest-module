/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.mule.runtime.api.streaming.CursorProvider;
import org.mule.runtime.api.streaming.bytes.CursorStreamProvider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamUtils {

  /**
   * The Carriage Return ASCII character value.
   */
  public static final byte CR = 0x0D;
  /**
   * The Line Feed ASCII character value.
   */
  public static final byte LF = 0x0A;
  /**
   * The dash (-) ASCII character value.
   */
  public static final byte DASH = 0x2D;
  /**
   * A byte sequence that follows a delimiter that will be
   * followed by an encapsulation (<code>CRLF</code>).
   */
  public static final byte[] CRLF = {CR, LF};
  /**
   * A byte sequence that that follows a delimiter of the last
   * encapsulation in the stream (<code>--</code>).
   */
  public static final byte[] STREAM_TERMINATOR = {DASH, DASH};
  /**
   * A byte sequence that precedes a boundary (<code>CRLF--</code>).
   */
  public static final byte[] BOUNDARY_PREFIX = {CR, LF, DASH, DASH};
  /**
   * The default length of the buffer used for processing a request.
   */
  public static Integer BUFFER_SIZE = 4096;

  public static long copyLarge(InputStream input, OutputStream output) throws IOException {
    byte[] buffer = new byte[BUFFER_SIZE];
    long count = 0L;

    int n1;
    for (boolean n = false; -1 != (n1 = input.read(buffer)); count += (long) n1) {
      output.write(buffer, 0, n1);
    }

    return count;
  }

  public static InputStream unwrapCursorStream(Object object) {
    return object instanceof CursorProvider ? ((CursorStreamProvider) object).openCursor() : ((InputStream) object);
  }
}
