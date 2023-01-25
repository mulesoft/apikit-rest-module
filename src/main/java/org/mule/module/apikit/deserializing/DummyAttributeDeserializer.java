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

    List<String> values = new ArrayList<>();
    for (int current = 1; current < chars.length - 1; current++) {
      if (chars[current] == '\\') {
        int next = current + 1;
        if (next < chars.length - 1 && chars[next] == DOUBLE_QUOTES) {
          current = next;
        }
      } else if (chars[current] == DOUBLE_QUOTES) {
        // Unbalanced quotes found
        return asList(attributeValue);
      }
      curVal.append(chars[current]);
    }
    values.add(curVal.toString());
    return values;
  }
}
