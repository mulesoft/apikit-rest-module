/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.deserializing;

import org.mule.module.apikit.api.deserializing.AttributesDeserializingStrategy;
import org.mule.runtime.extension.api.annotation.param.Parameter;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration that encompasses all the attribute deserializing strategies implementations.
 */
public class AttributesDeserializingStrategies {

  @Parameter
  private List<AttributesDeserializingStrategy> attributesDeserializingStrategies;

  public List<AttributesDeserializingStrategy> getAttributesDeserializingStrategies() {
    if (attributesDeserializingStrategies == null) {
      return new ArrayList<>();
    }
    return attributesDeserializingStrategies;
  }

  public void setAttributesDeserializingStrategies(List<AttributesDeserializingStrategy> attributesDeserializingStrategies) {
    this.attributesDeserializingStrategies = attributesDeserializingStrategies;
  }

}
