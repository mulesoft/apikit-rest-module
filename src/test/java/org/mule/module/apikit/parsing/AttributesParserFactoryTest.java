/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.parsing;

import org.junit.Test;

import static org.junit.Assert.assertTrue;


public class AttributesParserFactoryTest {

  @Test
  public void getParserByStrategy() {
    AttributeParser attributeParser = AttributesParserFactory.INSTANCE.buildParserByStrategy(new ArrayHeaderParsingStrategy());
    assertTrue(ArrayHeaderAttributeParser.class.isInstance(attributeParser));
    attributeParser = AttributesParserFactory.INSTANCE.buildParserByStrategy(new NoneAttributeParsingStrategy());
    assertTrue(DummyAttributeParser.class.isInstance(attributeParser));
  }

}
