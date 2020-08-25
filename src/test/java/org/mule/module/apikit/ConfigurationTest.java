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
import org.mule.module.apikit.deserializing.ArrayHeaderDeserializingStrategy;
import org.mule.module.apikit.deserializing.AttributesDeserializingStrategies;
import org.mule.module.apikit.deserializing.NoneAttributeDeserializingStrategy;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mule.module.apikit.api.deserializing.AttributesDeserializingStrategyIdentifier.ARRAY_HEADER_DESERIALIZING_STRATEGY;
import static org.mule.module.apikit.api.deserializing.AttributesDeserializingStrategyIdentifier.NONE_DESERIALIZING_STRATEGY;

public class ConfigurationTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void getAttributeDeserializingStrategyByIdentifier() {
    AttributesDeserializingStrategies strategies = new AttributesDeserializingStrategies();
    strategies.setAttributesDeserializingStrategies(asList(new ArrayHeaderDeserializingStrategy(),
                                                           new NoneAttributeDeserializingStrategy()));
    Configuration configuration = new Configuration();
    configuration.setAttributesDeserializingStrategies(strategies);
    AttributesDeserializingStrategy deserializingStrategy =
        configuration.getAttributesDeserializingStrategy(ARRAY_HEADER_DESERIALIZING_STRATEGY);
    assertNotNull(deserializingStrategy);
    assertTrue(ArrayHeaderDeserializingStrategy.class.isInstance(deserializingStrategy));
    deserializingStrategy = configuration.getAttributesDeserializingStrategy(NONE_DESERIALIZING_STRATEGY);
    assertNotNull(deserializingStrategy);
    assertTrue(NoneAttributeDeserializingStrategy.class.isInstance(deserializingStrategy));
  }

  @Test
  public void getDummyStrategyIfNoStrategies() {
    Configuration configuration = new Configuration();
    configuration.setAttributesDeserializingStrategies(new AttributesDeserializingStrategies());
    AttributesDeserializingStrategy deserializingStrategy =
        configuration.getAttributesDeserializingStrategy(ARRAY_HEADER_DESERIALIZING_STRATEGY);
    assertNotNull(deserializingStrategy);
    assertTrue(NoneAttributeDeserializingStrategy.class.isInstance(deserializingStrategy));
  }

  @Test
  public void getDummyStrategyIfNullStrategies() {
    Configuration configuration = new Configuration();
    AttributesDeserializingStrategy deserializingStrategy =
        configuration.getAttributesDeserializingStrategy(ARRAY_HEADER_DESERIALIZING_STRATEGY);
    assertNotNull(deserializingStrategy);
    assertTrue(NoneAttributeDeserializingStrategy.class.isInstance(deserializingStrategy));
  }

  @Test
  public void exceptionIfStrategyDoesNotExist() {
    expectedException.expect(RuntimeException.class);
    expectedException.expectMessage("No deserializer found for the strategy identifier provided.");
    AttributesDeserializingStrategies strategies = new AttributesDeserializingStrategies();
    strategies.setAttributesDeserializingStrategies(asList(new NoneAttributeDeserializingStrategy()));
    Configuration configuration = new Configuration();
    configuration.setAttributesDeserializingStrategies(strategies);
    configuration.getAttributesDeserializingStrategy(ARRAY_HEADER_DESERIALIZING_STRATEGY);
  }
}
