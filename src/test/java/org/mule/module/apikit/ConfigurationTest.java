/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mule.module.apikit.api.deserializing.AttributesDeserializingStrategy;
import org.mule.module.apikit.api.deserializing.AttributesDeserializingStrategyIdentifier;
import org.mule.module.apikit.deserializing.ArrayHeaderDeserializingStrategy;
import org.mule.module.apikit.deserializing.AttributesDeserializingStrategies;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mule.module.apikit.MockingUtils.createEnumValue;
import static org.mule.module.apikit.api.deserializing.AttributesDeserializingStrategyIdentifier.ARRAY_HEADER_DESERIALIZING_STRATEGY;

public class ConfigurationTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void getAttributeDeserializingStrategyByIdentifier() {
    AttributesDeserializingStrategies strategies = new AttributesDeserializingStrategies();
    strategies.setAttributesDeserializingStrategies(asList(new ArrayHeaderDeserializingStrategy()));
    Configuration configuration = new Configuration();
    configuration.setAttributesDeserializingStrategies(strategies);
    AttributesDeserializingStrategy deserializingStrategy =
        configuration.getAttributesDeserializingStrategy(ARRAY_HEADER_DESERIALIZING_STRATEGY);
    assertNotNull(deserializingStrategy);
    assertTrue(ArrayHeaderDeserializingStrategy.class.isInstance(deserializingStrategy));
  }

  @Test
  public void getNullIfNoStrategiesOfIdentifierType() {
    Configuration configuration = new Configuration();
    configuration.setAttributesDeserializingStrategies(new AttributesDeserializingStrategies());
    AttributesDeserializingStrategy deserializingStrategy =
        configuration.getAttributesDeserializingStrategy(ARRAY_HEADER_DESERIALIZING_STRATEGY);
    assertNull(deserializingStrategy);
  }

  @Test
  public void getNullStrategyIfNoStrategiesAtAll() {
    Configuration configuration = new Configuration();
    AttributesDeserializingStrategy deserializingStrategy =
        configuration.getAttributesDeserializingStrategy(ARRAY_HEADER_DESERIALIZING_STRATEGY);
    assertNull(deserializingStrategy);
  }

  @Test
  public void exceptionIfThereAreStrategiesButNoneOfIdentifierType() {
    expectedException.expect(RuntimeException.class);
    expectedException.expectMessage("No deserializer found for the strategy identifier provided.");
    AttributesDeserializingStrategies strategies = new AttributesDeserializingStrategies();
    strategies.setAttributesDeserializingStrategies(asList(new MockAttributeDeserializingStrategy()));
    Configuration configuration = new Configuration();
    configuration.setAttributesDeserializingStrategies(strategies);
    configuration.getAttributesDeserializingStrategy(ARRAY_HEADER_DESERIALIZING_STRATEGY);
  }

  private class MockAttributeDeserializingStrategy implements AttributesDeserializingStrategy {

    @Override
    public AttributesDeserializingStrategyIdentifier getStrategyIdentifier() {
      try {
        return createEnumValue(AttributesDeserializingStrategyIdentifier.class, "MOCK_STRATEGY", 2, null);
      } catch (Exception e) {
        e.printStackTrace();
      }
      return null;
    }

  }
}
