/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.deserializing;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for Attribute Deserializer Implementations.
 */
public abstract class BaseAttributeDeserializer implements AttributeDeserializer {

  @Override
  public List<String> deserializeListOfValues(List<String> attributeValues) {
    List<String> deserializedValues = new ArrayList<>();
    for (String arrayValue : attributeValues) {
      deserializedValues.addAll(deserializeValue(arrayValue));
    }
    return deserializedValues;
  }

}
