/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.mule.module.apikit.helpers.EventHelper;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.metadata.TypedValue;
import org.raml.parser.utils.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;

import static org.mule.module.apikit.helpers.PayloadHelper.getPayloadAsByteArray;

public class CharsetUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(CharsetUtils.class);


  private static String normalizeCharset(String encoding) {
    if (encoding != null && encoding.matches("(?i)UTF-16.+")) {
      encoding = "UTF-16";
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
   * @param input payload that would be instrospected
   * @param logger where to log
   * @return payload encoding
   */
  public static <T> String getEncoding(TypedValue<T> typedValue, Object input, Logger logger) throws IOException {
    String encoding = getEncoding(input, logger);

    if (encoding == null) {
      encoding = normalizeCharset(EventHelper.getEncoding(typedValue).toString());
      logger.debug("Defaulting to mule message encoding: " + logEncoding(encoding));
    }

    return encoding;
  }

  /**
   * Tries to figure out the encoding of the request in the following order
   *  - Determine what type is payload
   *  - detects the payload encoding using BOM, or tries to auto-detect it
   *  - return the mule message encoding
   *
   * @param input payload that would be instrospected
   * @param logger where to log
   * @return payload encoding
   */
  public static String getEncoding(Object input, Logger logger) throws IOException {
    String encoding;

    byte[] bytes = getPayloadAsByteArray(input);

    if (bytes == null) {
      return null;
    }
    encoding = normalizeCharset(StreamUtils.detectEncoding(bytes));
    logger.debug("Detected payload encoding: " + logEncoding(encoding));

    return encoding;
  }

  public static String getCharset(String contentType) {
    if (contentType == null)
      return null;

    MediaType mediaType = MediaType.parse(contentType);

    Optional<Charset> charset = mediaType.getCharset();
    return charset.map(Charset::name).orElse(null);
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
      LOGGER.debug("Trimming {}-byte BOM", bomSize);
      int trimmedSize = content.length - bomSize;
      byte[] trimmedArray = new byte[trimmedSize];
      System.arraycopy(content, bomSize, trimmedArray, 0, trimmedSize);
      return trimmedArray;
    }
    return content;
  }


  private static String logEncoding(String encoding) {
    return encoding != null ? encoding : "not specified";
  }

}
