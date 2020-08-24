/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.parsing;

import org.mule.module.apikit.api.parsing.AttributesDeserializingStrategy;

/**
 * Attributes Deserializer Factory. Builds the deserializer according to the indicated strategy.
 */
public class AttributesDeserializerFactory {

  public static final AttributesDeserializerFactory INSTANCE = new AttributesDeserializerFactory();

  private AttributesDeserializerFactory() {}

  public AttributeDeserializer getDeserializerByStrategy(AttributesDeserializingStrategy deserializingStrategy) {
    switch (deserializingStrategy.getStrategyIdentifier()) {
      case NONE_DESERIALIZING_STRATEGY:
        return new DummyAttributeDeserializer(deserializingStrategy);
      case ARRAY_HEADER_DESERIALIZING_STRATEGY:
        return new ArrayHeaderAttributeDeserializer((ArrayHeaderDeserializingStrategy) deserializingStrategy);
      default:
        throw new RuntimeException("No Deserializer found for the attribute strategy provided.");
    }
  }
}
