/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.deserializing;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Deserializer of array header attributes based on the strategy defined by {@link ArrayHeaderDeserializingStrategy}
 */
public class ArrayHeaderAttributeDeserializer extends BaseAttributeDeserializer<ArrayHeaderDeserializingStrategy> {

  private static final char DOUBLE_QUOTES = '"';
  private static final char OPENING_CURLY_BRACE = '{';
  private static final char CLOSING_CURLY_BRACE = '}';

  public ArrayHeaderAttributeDeserializer(ArrayHeaderDeserializingStrategy deserializingStrategy) {
    super(deserializingStrategy);
  }

  @Override
  public List<String> deserializeValue(String attributeValue) {
    if (isBlank(attributeValue)) {
      return emptyList();
    }
    final char delimiter = deserializingStrategy.getDelimiter().getDelimiterValue().charAt(0);
    char[] chars = attributeValue.toCharArray();
    StringBuffer curVal = new StringBuffer();
    boolean inQuotes = false;
    int curlyBracesStackCount = 0;
    boolean startCollectChar = false;

    List<String> headerValues = new ArrayList<>();
    for (char ch : chars) {
      if (inQuotes) {
        startCollectChar = true;
        if (ch == DOUBLE_QUOTES) {
          inQuotes = curlyBracesStackCount > 0 ? true : false;
          if (inQuotes) {
            curVal.append(ch);
          }
        } else if (ch == OPENING_CURLY_BRACE) {
          curlyBracesStackCount++;
          curVal.append(ch);
        } else if (ch == CLOSING_CURLY_BRACE) {
          curlyBracesStackCount--;
          curVal.append(ch);
        } else {
          curVal.append(ch);
        }
      } else if (curlyBracesStackCount > 0) {
        if (ch == CLOSING_CURLY_BRACE) {
          curlyBracesStackCount--;
        } else if (ch == OPENING_CURLY_BRACE) {
          curlyBracesStackCount++;
        }
        if (curlyBracesStackCount >= 0) {
          curVal.append(ch);
        }
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
          addValueToList(headerValues, curVal);
          curVal = new StringBuffer();
          startCollectChar = false;
        } else if (ch == '\r') {
          continue;
        } else if (ch == '\n') {
          continue;
        } else {
          curVal.append(ch);
        }
      }
    }
    addValueToList(headerValues, curVal);
    return headerValues;
  }

  private void addValueToList(List<String> headerValues, StringBuffer curVal) {
    String value = curVal.toString();
    if (isNotBlank(value)) {
      headerValues.add(value);
    }
  }

}
