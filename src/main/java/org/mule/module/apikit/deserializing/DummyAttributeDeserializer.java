/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.deserializing;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Dummy attribute deserializer that returns the same values that receives.
 * <p>
 * If value is between quotes, it gets rid of enclosing quotes and unescapes inner quotes if any.
 * If unbalanced quotes are find, the value is returned as it is.
 * <p>
 * Default deserializer when no delimiter strategy is set up.
 */
public class DummyAttributeDeserializer extends BaseAttributeDeserializer {

  @Override
  public List<String> deserializeValue(String attributeValue) {
    char[] chars = attributeValue.toCharArray();
    if (chars[0] == DOUBLE_QUOTES && chars[chars.length - 1] == DOUBLE_QUOTES) {
      return parseQuotedAttribute(attributeValue);
    }
    return asList(attributeValue);
  }

  private List<String> parseQuotedAttribute(String attributeValue) {
    char[] chars = attributeValue.toCharArray();
    StringBuffer curVal = new StringBuffer();
    boolean insideQuotes = false; // Whether the cursor is reading a value that started with an open quote
    boolean quotesClosed = false; // Whether the value had quotes that were closed
    char lastCharacter = '\n'; // Stores the last character read

    List<String> values = new ArrayList<>();
    for (int i = 0; i < chars.length; i++) {
      switch (chars[i]) {
        case '\r':
        case '\n':
        case ' ': {
          if (insideQuotes) {
            curVal.append(chars[i]);
          }
          break;
        }
        case DOUBLE_QUOTES: {
          if (curVal.length() == 0) {
            insideQuotes = true;
          } else if (insideQuotes && chars[i - 1] != '\\') {
            // Unescaped quotes found, closing value between quotes
            quotesClosed = true;
            insideQuotes = false;
            if (i < chars.length - 1) {
              // Unbalanced quotes, found unescaped quote in the middle of the quoted string.
              // Returning the string as it is.
              return asList(attributeValue);
            }
          }
          curVal.append(chars[i]);
          lastCharacter = chars[i];
          break;
        }
        default: {
          curVal.append(chars[i]);
          lastCharacter = chars[i];
        }
      }
    }
    // Value scan completed, update value if it was enclosed with quotes
    if (lastCharacter == DOUBLE_QUOTES && quotesClosed) {
      // Quotes were balanced, remove surrounding quotes and remove escaping characters inside value
      curVal = new StringBuffer(curVal.substring(1, curVal.lastIndexOf("\"")).replace("\\\"", "\""));
    }
    // Add the value to result list
    addValueToList(values, curVal);
    return values;
  }
}
