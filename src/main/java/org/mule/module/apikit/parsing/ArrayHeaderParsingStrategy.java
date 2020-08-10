/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.parsing;

import org.mule.module.apikit.api.parsing.AttributesParsingStrategy;
import org.mule.module.apikit.api.parsing.AttributesParsingStrategyIdentifier;
import org.mule.runtime.extension.api.annotation.param.Parameter;

import static org.mule.module.apikit.api.parsing.AttributesParsingStrategyIdentifier.ARRAY_HEADER_PARSING_STRATEGY;
import static org.mule.module.apikit.parsing.ArrayHeaderDelimiter.NONE;

/**
 * Configuration that defines the delimiter character used for separate array header values.
 */
public class ArrayHeaderParsingStrategy implements AttributesParsingStrategy {

  /**
   * Delimiter character used for separate array header values.
   */
  @Parameter
  private ArrayHeaderDelimiter delimiter = NONE;

  public ArrayHeaderDelimiter getDelimiter() {
    return delimiter;
  }

  public void setDelimiter(ArrayHeaderDelimiter delimiter) {
    this.delimiter = delimiter;
  }

  @Override
  public AttributesParsingStrategyIdentifier getStrategyIdentifier() {
    return ARRAY_HEADER_PARSING_STRATEGY;
  }
}
