/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.deserializing;

import org.mule.runtime.dsl.api.component.TypeConverter;

import static org.mule.module.apikit.deserializing.ArrayHeaderDelimiter.COMMA;
import static org.mule.module.apikit.deserializing.ArrayHeaderDelimiter.NONE;

/**
 * Maps the String delimiter set in configuration with the corresponding enum value
 */
public class ArrayHeaderDelimiterTypeConverter implements TypeConverter<String, ArrayHeaderDelimiter> {

  @Override
  public ArrayHeaderDelimiter convert(String s) {
    return COMMA.getDelimiterValue().equals(s) ? COMMA : NONE;
  }
}
