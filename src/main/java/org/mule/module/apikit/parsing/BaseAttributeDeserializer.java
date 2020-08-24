/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.parsing;

import org.mule.module.apikit.api.parsing.AttributesDeserializingStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for Attribute Deserializers.
 *
 * @param <DeserializingStrategy> The deserializing strategy used for the deserializer.
 */
public abstract class BaseAttributeDeserializer<DeserializingStrategy extends AttributesDeserializingStrategy>
    implements AttributeDeserializer {

  protected DeserializingStrategy deserializingStrategy;

  public BaseAttributeDeserializer(DeserializingStrategy deserializingStrategy) {
    this.deserializingStrategy = deserializingStrategy;
  }

  @Override
  public List<String> deserializeListOfValues(List<String> attributeValues) {
    List<String> deserializedValues = new ArrayList<>();
    for (String arrayValue : attributeValues) {
      deserializedValues.addAll(deserializeValue(arrayValue));
    }
    return deserializedValues;
  }

}
