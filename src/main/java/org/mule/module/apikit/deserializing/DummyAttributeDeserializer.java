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
    char[] chars = attributeValue.substring(1, attributeValue.length() - 1).toCharArray();
    StringBuffer curVal = new StringBuffer();
    boolean escapedChar = false;

    List<String> values = new ArrayList<>();
    for (int index = 0; index < chars.length; index++) {
      if (chars[index] == '\\') {
        escapedChar = true;
      } else if (chars[index] == DOUBLE_QUOTES) {
        if (escapedChar) {
          curVal.append(chars[index]);
        } else if (index < chars.length - 1) {
          // Unbalanced quotes, found unescaped quote in the middle of the quoted string.
          // Returning the string as it is.
          return asList(attributeValue);
        }
        escapedChar = false;
      } else {
        curVal.append(chars[index]);
        escapedChar = false;
      }
    }
    addValueToList(values, curVal);
    return values;
  }
}
