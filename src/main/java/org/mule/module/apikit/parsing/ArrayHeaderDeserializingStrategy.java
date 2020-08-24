/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.parsing;

import org.mule.module.apikit.api.parsing.AttributesDeserializingStrategy;
import org.mule.module.apikit.api.parsing.AttributesDeserializingStrategyIdentifier;
import org.mule.runtime.extension.api.annotation.param.Parameter;

import static org.mule.module.apikit.api.parsing.AttributesDeserializingStrategyIdentifier.ARRAY_HEADER_DESERIALIZING_STRATEGY;
import static org.mule.module.apikit.parsing.ArrayHeaderDelimiter.NONE;

/**
 * Configuration that defines the delimiter character used for separate array header values.
 */
public class ArrayHeaderDeserializingStrategy implements AttributesDeserializingStrategy {

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
  public AttributesDeserializingStrategyIdentifier getStrategyIdentifier() {
    return ARRAY_HEADER_DESERIALIZING_STRATEGY;
  }
}
