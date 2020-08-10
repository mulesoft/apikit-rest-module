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
import org.mule.module.apikit.api.parsing.AttributesParsingStrategy;
import org.mule.module.apikit.parsing.ArrayHeaderParsingStrategy;
import org.mule.module.apikit.parsing.AttributesParsingStrategies;
import org.mule.module.apikit.parsing.NoneAttributeParsingStrategy;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mule.module.apikit.api.parsing.AttributesParsingStrategyIdentifier.ARRAY_HEADER_PARSING_STRATEGY;
import static org.mule.module.apikit.api.parsing.AttributesParsingStrategyIdentifier.NONE_STRATEGY;

public class ConfigurationTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void getAttributeParsingStrategyByIdentifier() {
    AttributesParsingStrategies strategies = new AttributesParsingStrategies();
    strategies.setAttributesParsingStrategies(asList(new ArrayHeaderParsingStrategy(), new NoneAttributeParsingStrategy()));
    Configuration configuration = new Configuration();
    configuration.setAttributesParsingStrategies(strategies);
    AttributesParsingStrategy parsingStrategy = configuration.getAttributesParsingStrategy(ARRAY_HEADER_PARSING_STRATEGY);
    assertNotNull(parsingStrategy);
    assertTrue(ArrayHeaderParsingStrategy.class.isInstance(parsingStrategy));
    parsingStrategy = configuration.getAttributesParsingStrategy(NONE_STRATEGY);
    assertNotNull(parsingStrategy);
    assertTrue(NoneAttributeParsingStrategy.class.isInstance(parsingStrategy));
  }

  @Test
  public void getDummyStrategyIfNoStrategies() {
    Configuration configuration = new Configuration();
    configuration.setAttributesParsingStrategies(new AttributesParsingStrategies());
    AttributesParsingStrategy parsingStrategy = configuration.getAttributesParsingStrategy(ARRAY_HEADER_PARSING_STRATEGY);
    assertNotNull(parsingStrategy);
    assertTrue(NoneAttributeParsingStrategy.class.isInstance(parsingStrategy));
  }

  @Test
  public void getDummyStrategyIfNullStrategies() {
    Configuration configuration = new Configuration();
    AttributesParsingStrategy parsingStrategy = configuration.getAttributesParsingStrategy(ARRAY_HEADER_PARSING_STRATEGY);
    assertNotNull(parsingStrategy);
    assertTrue(NoneAttributeParsingStrategy.class.isInstance(parsingStrategy));
  }

  @Test
  public void exceptionIfStrategyDoesNotExist() {
    expectedException.expect(RuntimeException.class);
    expectedException.expectMessage("No parser found for the parsing strategy identifier provided.");
    AttributesParsingStrategies strategies = new AttributesParsingStrategies();
    strategies.setAttributesParsingStrategies(asList(new NoneAttributeParsingStrategy()));
    Configuration configuration = new Configuration();
    configuration.setAttributesParsingStrategies(strategies);
    configuration.getAttributesParsingStrategy(ARRAY_HEADER_PARSING_STRATEGY);
  }
}
