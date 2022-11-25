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
    boolean inQuotes = false;
    int curlyBracesStackCount = 0;
    boolean startCollectChar = false;
    boolean escapedChar = false;

    List<String> values = new ArrayList<>();
    for (char ch : chars) {
      if (inQuotes) {
        startCollectChar = true;
        if (ch == '\\') {
          escapedChar = true;
        } else if (ch == DOUBLE_QUOTES) {
          if (escapedChar) {
            curVal.append(ch);
          } else {
            inQuotes = false;
          }
          escapedChar = false;
        } else if (ch == OPENING_CURLY_BRACE) {
          curlyBracesStackCount++;
          curVal.append(ch);
          escapedChar = false;
        } else if (ch == CLOSING_CURLY_BRACE) {
          curlyBracesStackCount--;
          curVal.append(ch);
          escapedChar = false;
        } else {
          curVal.append(ch);
          escapedChar = false;
        }
      } else if (curlyBracesStackCount > 0) {
        if (ch == CLOSING_CURLY_BRACE) {
          curlyBracesStackCount--;
        } else if (ch == OPENING_CURLY_BRACE) {
          curlyBracesStackCount++;
        }
        curVal.append(ch);
      } else {
        if (ch == DOUBLE_QUOTES) {
          inQuotes = curlyBracesStackCount > 0 ? false : true;
          if (startCollectChar) {
            curVal.append(ch);
          }
        } else if (ch == OPENING_CURLY_BRACE) {
          curlyBracesStackCount++;
          curVal.append(ch);
        } else if (ch == delimiter) {
          addValueToList(values, curVal);
          curVal = new StringBuffer();
          startCollectChar = false;
        } else if (ch == '\r') {
        } else if (ch == '\n') {
        } else {
          curVal.append(ch);
        }
      }
    }
    addValueToList(values, curVal);
    return values;
  }
}
