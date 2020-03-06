/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.mule.module.apikit.exception.UnsupportedMediaTypeException;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.util.MultiMap;

import java.nio.charset.Charset;
import java.util.Optional;

import static org.mule.module.apikit.helpers.AttributesHelper.getContentType;

public class CharsetUtils {

  private CharsetUtils() {}

  public static String getCharset(MultiMap<String, String> headers, Object payload) throws UnsupportedMediaTypeException {
    String charset = getHeaderCharset(headers);
    if (charset == null) {
      if (payload instanceof TypedValue) {
        return normalizeCharset(getEncoding((TypedValue) payload));
      }
    }
    return normalizeCharset(charset);
  }

  private static String getHeaderCharset(MultiMap<String, String> headers) throws UnsupportedMediaTypeException {
    return getCharset(getContentType(headers));
  }

  public static String getCharset(String contentType) {
    if (contentType == null) {
      return null;
    }
    MediaType mediaType = MediaType.parse(contentType);
    Optional<Charset> charset = mediaType.getCharset();
    return charset.map(Charset::name).orElse(null);
  }

  private static String normalizeCharset(String encoding) {
    if (encoding != null && encoding.matches("(?i)UTF-16.+")) {
      return "UTF-16";
    }
    return encoding;
  }

  /**
   * Tries to figure out the encoding of the request in the following order
   *  - Determine what type is payload
   *  - detects the payload encoding using BOM, or tries to auto-detect it
   *  - return the mule message encoding
   *
   * @param typedValue mule typed value
  
   * @return payload encoding
   */
  private static <T> String getEncoding(TypedValue<T> typedValue) {
    return typedValue.getDataType().getMediaType().getCharset().orElse(Charset.defaultCharset()).toString();
  }

  /**
   * Removes BOM from byte array if present
   *
   * @param content byte array
   * @return BOM-less byte array
   */
  public static byte[] trimBom(byte[] content) {
    int bomSize = 0;
    if (content.length > 4) {
      // check for UTF_32BE and UTF_32LE BOMs
      if (content[0] == 0x00 && content[1] == 0x00 && content[2] == (byte) 0xFE && content[3] == (byte) 0xFF ||
          content[0] == (byte) 0xFF && content[1] == (byte) 0xFE && content[2] == 0x00 && content[3] == 0x00) {
        bomSize = 4;
      }
    }
    if (content.length > 3 && bomSize == 0) {
      // check for UTF-8 BOM
      if (content[0] == (byte) 0xEF && content[1] == (byte) 0xBB && content[2] == (byte) 0xBF) {
        bomSize = 3;
      }
    }
    if (content.length > 2 && bomSize == 0) {
      // check for UTF_16BE and UTF_16LE BOMs
      if (content[0] == (byte) 0xFE && content[1] == (byte) 0xFF || content[0] == (byte) 0xFF && content[1] == (byte) 0xFE) {
        bomSize = 2;
      }
    }

    if (bomSize > 0) {
      int trimmedSize = content.length - bomSize;
      byte[] trimmedArray = new byte[trimmedSize];
      System.arraycopy(content, bomSize, trimmedArray, 0, trimmedSize);
      return trimmedArray;
    }
    return content;
  }

}
