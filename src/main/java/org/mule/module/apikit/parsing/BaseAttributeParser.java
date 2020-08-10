/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.parsing;

import org.mule.module.apikit.api.parsing.AttributesParsingStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for attribute parsers.
 *
 * @param <ParsingStrategy> The parsing strategy used for the parser.
 */
public abstract class BaseAttributeParser<ParsingStrategy extends AttributesParsingStrategy> implements AttributeParser {

  protected ParsingStrategy parsingStrategy;

  public BaseAttributeParser(ParsingStrategy parsingStrategy) {
    this.parsingStrategy = parsingStrategy;
  }

  @Override
  public List<String> parseListOfValues(List<String> attributeValues) {
    List<String> parsedValues = new ArrayList<>();
    for (String arrayValue : attributeValues) {
      parsedValues.addAll(parseValue(arrayValue));
    }
    return parsedValues;
  }

}
