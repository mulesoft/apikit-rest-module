/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.deserializing;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Base class for Attribute Deserializer Implementations.
 */
public abstract class BaseAttributeDeserializer implements AttributeDeserializer {

  protected static final char DOUBLE_QUOTES = '"';
  protected static final char OPENING_CURLY_BRACE = '{';
  protected static final char OPENING_SQUARE_BRACKET = '[';
  protected static final char CLOSING_CURLY_BRACE = '}';
  protected static final char CLOSING_SQUARED_BRACKET = ']';

  @Override
  public List<String> deserializeListOfValues(List<String> attributeValues) {
    List<String> deserializedValues = new ArrayList<>();
    for (String arrayValue : attributeValues) {
      if (isBlank(arrayValue)) {
        deserializedValues.add("");
      } else {
        deserializedValues.addAll(deserializeValue(arrayValue));
      }
    }
    return deserializedValues;
  }

}
