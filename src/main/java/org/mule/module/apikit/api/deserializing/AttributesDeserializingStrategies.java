/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.api.deserializing;

import org.mule.runtime.extension.api.annotation.param.Parameter;

/**
 * Configuration that encompasses all the configuration for attributes deserializing strategies.
 */
public class AttributesDeserializingStrategies {

  @Parameter
  private ArrayHeaderDelimiter arrayHeaderDelimiter;


  public ArrayHeaderDelimiter getArrayHeaderDelimiter() {
    return arrayHeaderDelimiter;
  }

  public void setArrayHeaderDelimiter(ArrayHeaderDelimiter arrayHeaderDelimiter) {
    this.arrayHeaderDelimiter = arrayHeaderDelimiter;
  }
}
