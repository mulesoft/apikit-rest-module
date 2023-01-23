/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.deserializing;

import com.google.common.collect.ImmutableSet;
import org.mule.module.apikit.api.deserializing.ArrayHeaderDelimiter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * Deserializer for array header attributes.
 */
public class ArrayHeaderAttributeDeserializer extends BaseAttributeDeserializer {

  private static final Set<Character> SPACE_CHARS = ImmutableSet.of(' ', '\n', '\t', '\r', '\f');
  private ArrayHeaderDelimiter arrayHeaderDelimiter;

  public ArrayHeaderAttributeDeserializer(ArrayHeaderDelimiter arrayHeaderDelimiter) {
    this.arrayHeaderDelimiter = arrayHeaderDelimiter;
  }

  @Override
  public List<String> deserializeValue(String attributeValue) {
    final char delimiter = arrayHeaderDelimiter.getDelimiterValue().charAt(0);
    char[] chars = attributeValue.toCharArray();
    List<String> values = new ArrayList<>();
    int current = 0;
    while (current < chars.length) {
      current = skipWhiteSpace(current, chars);
      if (current == chars.length) {
        // Blank was found
        values.add("");
      } else if (chars[current] == delimiter) {
        // Delimiter was found, considering left side as a blank value
        values.add("");
        current++;
        if (current == chars.length) {
          // End of string reached after a delimiter was found, considering right side as a blank value
          values.add("");
        }
      } else if (chars[current] == DOUBLE_QUOTES) {
        current = tryParseQuotedString(current, chars, delimiter, values);
      } else if (chars[current] == OPENING_CURLY_BRACE || chars[current] == OPENING_SQUARE_BRACKET) {
        current = tryParseJsonObject(current, chars, delimiter, values);
      } else {
        current = tryParseRawString(current, chars, delimiter, values);
      }
    }
    return values;
  }

  private static int skipWhiteSpace(int start, char[] chars) {
    int current = start;
    while (current < chars.length && SPACE_CHARS.contains(chars[current])) {
      current++;
    }
    return current;
  }

  private int tryParseQuotedString(int start, char[] chars, char delimiter, List<String> values) {
    StringBuffer curVal = new StringBuffer();
    // Skip initial quotes from being added to value
    int current = start + 1;

    // While unescaped quotes not found
    while (current < chars.length && chars[current] != DOUBLE_QUOTES) {
      if (chars[current] == '\\' && current < chars.length - 1 && chars[current + 1] == DOUBLE_QUOTES) {
        // Found '\' followed by double quotes, skipping '\' character
        current++;
      }
      curVal.append(chars[current]);
      current++;
    }

    // Skip end quotes from being added to value
    current++;
    current = skipWhiteSpace(current, chars);

    // End of string or delimiter found, adding value to result
    if (current == chars.length || chars[current] == delimiter) {
      values.add(curVal.toString());
      return current + 1;
    }

    // Otherwise try parsing as Raw value
    return tryParseRawString(start, chars, delimiter, values);
  }

  private int tryParseJsonObject(int start, char[] chars, char delimiter, List<String> values) {
    StringBuffer curVal = new StringBuffer();
    int current = start;
    Stack<Character> stack = new Stack<>();
    Character top;

    do {
      if (chars[current] == DOUBLE_QUOTES) {
        // Consider everything until next quotes as a string with escaped quotes
        do {
          if (chars[current] == '\\') {
            curVal.append(chars[current]);
            current++;
          }
          if (current < chars.length) {
            curVal.append(chars[current]);
            current++;
          }
        } while (current < chars.length && chars[current] != DOUBLE_QUOTES);
      } else if (chars[current] == CLOSING_CURLY_BRACE) {
        top = stack.pop();
        // If no opening curly brace in top of stack, unbalanced JSON object was found, try parsing as raw string
        if (top != OPENING_CURLY_BRACE) {
          return tryParseRawString(start, chars, delimiter, values);
        }
      } else if (chars[current] == CLOSING_SQUARED_BRACKET) {
        top = stack.pop();
        // If no opening square bracket in top of stack, unbalanced JSON array was found, try parsing as raw string
        if (top != OPENING_SQUARE_BRACKET) {
          return tryParseRawString(start, chars, delimiter, values);
        }
      } else if (chars[current] == OPENING_SQUARE_BRACKET || chars[current] == OPENING_CURLY_BRACE) {
        stack.push(chars[current]);
      }

      if (current < chars.length) {
        curVal.append(chars[current]);
        current++;
      }
    } while (current < chars.length && !stack.isEmpty());

    current = skipWhiteSpace(current, chars);

    // End of string or delimiter found, adding value to result
    if (current == chars.length || chars[current] == delimiter) {
      values.add(curVal.toString());
      return current + 1;
    }

    // Otherwise try parsing as Raw value
    return tryParseRawString(start, chars, delimiter, values);
  }

  private int tryParseRawString(int start, char[] chars, char delimiter, List<String> values) {
    int current = start;
    current = skipWhiteSpace(current, chars);
    int indexFirstNonWhitespace = current;
    int indexLastNonWhitespace = current;
    while (current < chars.length && chars[current] != delimiter) {
      if (!SPACE_CHARS.contains(chars[current])) {
        indexLastNonWhitespace = current;
      }
      current++;
    }
    // Trim string
    values.add(new String(chars, indexFirstNonWhitespace, indexLastNonWhitespace - indexFirstNonWhitespace + 1));
    return current + 1;
  }
}
