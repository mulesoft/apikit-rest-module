/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.deserializing;

import org.mule.module.apikit.api.deserializing.ArrayHeaderDelimiter;

import java.util.ArrayList;
import java.util.List;

/**
 * Deserializer for array header attributes.
 */
public class ArrayHeaderAttributeDeserializer extends BaseAttributeDeserializer {

  private ArrayHeaderDelimiter arrayHeaderDelimiter;

  public ArrayHeaderAttributeDeserializer(ArrayHeaderDelimiter arrayHeaderDelimiter) {
    this.arrayHeaderDelimiter = arrayHeaderDelimiter;
  }

  @Override
  public List<String> deserializeValue(String attributeValue) {
    final char delimiter = arrayHeaderDelimiter.getDelimiterValue().charAt(0);
    char[] chars = attributeValue.toCharArray();
    StringBuffer curVal = new StringBuffer();
    int curlyBracesStackCount = 0; // The amount of opening curly braces that are remaining to be closed by its counterpart
    boolean insideQuotes = false; // Whether the cursor is reading a value that started with an open quote
    boolean quotesClosed = false; // Whether the value had quotes that were closed
    char lastCharacter = '\n'; // Stores the last character read
    List<String> values = new ArrayList<>();

    for (int i = 0; i < chars.length; i++) {
      switch (chars[i]) {
        case '\r':
        case '\n':
        case ' ': {
          if (curlyBracesStackCount > 0 || insideQuotes) {
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
          }
          curVal.append(chars[i]);
          lastCharacter = chars[i];
          break;
        }
        case OPENING_CURLY_BRACE: {
          curlyBracesStackCount++;
          curVal.append(chars[i]);
          lastCharacter = chars[i];
          break;
        }
        case CLOSING_CURLY_BRACE: {
          curlyBracesStackCount--;
          curVal.append(chars[i]);
          lastCharacter = chars[i];
          break;
        }
        default: {
          if (chars[i] == delimiter) {
            if (curlyBracesStackCount > 0 || (insideQuotes && lastCharacter != DOUBLE_QUOTES)) {
              // Delimiter character inside an object or quotes, just append it
              curVal.append(chars[i]);
            } else {
              if (quotesClosed && lastCharacter == DOUBLE_QUOTES) {
                // Quotes already closed, remove surrounding quotes and remove escaping characters inside value
                curVal = new StringBuffer(curVal.substring(1, curVal.lastIndexOf("\"")).replace("\\\"", "\""));
              }
              addValueToList(values, curVal);
              curVal = new StringBuffer();
              quotesClosed = false;
            }
          } else {
            curVal.append(chars[i]);
            lastCharacter = chars[i];
          }
        }
      }
    }
    // Value scan completed, update value if it was enclosed with quotes
    if (lastCharacter == DOUBLE_QUOTES && quotesClosed && curlyBracesStackCount > 0) {
      // Quotes were balanced but brackets unbalanced, remove surrounding quotes
      curVal = new StringBuffer(curVal.substring(1, curVal.lastIndexOf("\"")));
    } else if (lastCharacter == DOUBLE_QUOTES && quotesClosed && curlyBracesStackCount == 0) {
      // Quotes and brackets were balanced, remove surrounding quotes and remove escaping characters inside value
      curVal = new StringBuffer(curVal.substring(1, curVal.lastIndexOf("\"")).replace("\\\"", "\""));
    }
    // Add the value to result list
    addValueToList(values, curVal);
    return values;
  }

}
