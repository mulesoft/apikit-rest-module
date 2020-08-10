/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.parsing;

import org.mule.module.apikit.api.parsing.AttributesParsingStrategy;
import org.mule.module.apikit.api.parsing.AttributesParsingStrategyIdentifier;

import static org.mule.module.apikit.api.parsing.AttributesParsingStrategyIdentifier.NONE_STRATEGY;

/**
 * Default configuration strategy used for identify the lack of parsing strategy.
 */
public class NoneAttributeParsingStrategy implements AttributesParsingStrategy {

  @Override
  public AttributesParsingStrategyIdentifier getStrategyIdentifier() {
    return NONE_STRATEGY;
  }
}
