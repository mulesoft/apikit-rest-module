/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.deserializing;

import java.util.List;

/**
 * Interface to be implemented by attribute deserializers.
 */
public interface AttributeDeserializer {

  List<String> deserializeValue(String attributeValue);

  List<String> deserializeListOfValues(List<String> attributeValues);
}
