/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.deserializing;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * Dummy attribute deserializer that returns the same values that receives without modifying it.
 * Used for keeping backward compatibility with the default behaviour.
 */
public class DummyAttributeDeserializer extends BaseAttributeDeserializer {

  @Override
  public List<String> deserializeValue(String attributeValue) {
    return asList(attributeValue);
  }
}
