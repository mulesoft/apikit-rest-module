/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.parsing;

import org.mule.module.apikit.api.parsing.AttributesDeserializingStrategy;
import org.mule.module.apikit.api.parsing.AttributesDeserializingStrategyIdentifier;

import static org.mule.module.apikit.api.parsing.AttributesDeserializingStrategyIdentifier.NONE_DESERIALIZING_STRATEGY;

/**
 * Default configuration strategy used for identify the lack of deserializing strategy.
 */
public class NoneAttributeDeserializingStrategy implements AttributesDeserializingStrategy {

  @Override
  public AttributesDeserializingStrategyIdentifier getStrategyIdentifier() {
    return NONE_DESERIALIZING_STRATEGY;
  }
}
