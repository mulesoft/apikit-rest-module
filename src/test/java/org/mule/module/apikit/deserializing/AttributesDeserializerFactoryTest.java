/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.deserializing;

import org.junit.Test;

import static org.junit.Assert.assertTrue;


public class AttributesDeserializerFactoryTest {

  @Test
  public void getDeserializerByStrategy() {
    AttributeDeserializer attributeDeserializer =
        AttributesDeserializerFactory.INSTANCE.getDeserializerByStrategy(new ArrayHeaderDeserializingStrategy());
    assertTrue(ArrayHeaderAttributeDeserializer.class.isInstance(attributeDeserializer));
    attributeDeserializer =
        AttributesDeserializerFactory.INSTANCE.getDeserializerByStrategy(new NoneAttributeDeserializingStrategy());
    assertTrue(DummyAttributeDeserializer.class.isInstance(attributeDeserializer));
  }

}
