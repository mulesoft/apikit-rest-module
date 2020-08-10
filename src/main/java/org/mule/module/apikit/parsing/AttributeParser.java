/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.parsing;

import java.util.List;

/**
 * Interface to be implemented by attribute parsers.
 */
public interface AttributeParser {

  List<String> parseValue(String attributeValue);

  List<String> parseListOfValues(List<String> attributeValues);
}
