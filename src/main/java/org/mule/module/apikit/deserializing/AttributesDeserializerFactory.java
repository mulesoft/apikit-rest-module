/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.deserializing;

import org.mule.module.apikit.api.deserializing.AttributesDeserializingStrategy;

/**
 * Attributes Deserializer Factory. Builds the deserializer according to the requested strategy.
 * Defaults to {@link DummyAttributeDeserializer} if no deserializing strategy match found.
 */
public class AttributesDeserializerFactory {

  public static final AttributesDeserializerFactory INSTANCE = new AttributesDeserializerFactory();

  private AttributesDeserializerFactory() {}

  public AttributeDeserializer getDeserializerByStrategy(AttributesDeserializingStrategy deserializingStrategy) {
    if (deserializingStrategy == null) {
      return new DummyAttributeDeserializer();
    }
    switch (deserializingStrategy.getStrategyIdentifier()) {
      case ARRAY_HEADER_DESERIALIZING_STRATEGY:
        return new ArrayHeaderAttributeDeserializer((ArrayHeaderDeserializingStrategy) deserializingStrategy);
      default:
        return new DummyAttributeDeserializer();
    }
  }
}
