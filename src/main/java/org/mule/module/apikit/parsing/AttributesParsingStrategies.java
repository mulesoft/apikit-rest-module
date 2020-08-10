/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.parsing;

import org.mule.module.apikit.api.parsing.AttributesParsingStrategy;
import org.mule.runtime.extension.api.annotation.param.Parameter;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration that encompasses all the attribute parsing strategies implementations.
 */
public class AttributesParsingStrategies {

  @Parameter
  private List<AttributesParsingStrategy> attributesParsingStrategies;

  public List<AttributesParsingStrategy> getAttributesParsingStrategies() {
    if (attributesParsingStrategies == null) {
      return new ArrayList<>();
    }
    return attributesParsingStrategies;
  }

  public void setAttributesParsingStrategies(List<AttributesParsingStrategy> attributesParsingStrategies) {
    this.attributesParsingStrategies = attributesParsingStrategies;
  }

}
