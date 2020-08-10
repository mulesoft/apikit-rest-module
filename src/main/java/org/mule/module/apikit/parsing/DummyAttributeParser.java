/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.parsing;

import org.mule.module.apikit.api.parsing.AttributesParsingStrategy;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * Dummy parser that returns the same values that receives without modifying anything.
 * Used for keeping backward compatibility with the default "no parsing" behaviour.
 */
public class DummyAttributeParser extends BaseAttributeParser<AttributesParsingStrategy> {

  public DummyAttributeParser(AttributesParsingStrategy parsingStrategy) {
    super(parsingStrategy);
  }

  @Override
  public List<String> parseValue(String attributeValue) {
    return asList(attributeValue);
  }
}
