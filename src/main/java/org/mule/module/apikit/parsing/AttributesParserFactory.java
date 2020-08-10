/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.parsing;

import org.mule.module.apikit.api.parsing.AttributesParsingStrategy;

/**
 * Attributes Parser Factory. Builds the parser according to the indicated strategy.
 */
public class AttributesParserFactory {

  public static final AttributesParserFactory INSTANCE = new AttributesParserFactory();

  private AttributesParserFactory() {}

  public AttributeParser buildParserByStrategy(AttributesParsingStrategy parsingStrategy) {
    switch (parsingStrategy.getStrategyIdentifier()) {
      case NONE_STRATEGY:
        return new DummyAttributeParser(parsingStrategy);
      case ARRAY_HEADER_PARSING_STRATEGY:
        return new ArrayHeaderAttributeParser((ArrayHeaderParsingStrategy) parsingStrategy);
      default:
        throw new RuntimeException("No Parser found for the attribute strategy provided.");
    }
  }
}
