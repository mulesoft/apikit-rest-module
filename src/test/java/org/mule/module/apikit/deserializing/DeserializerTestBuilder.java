/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.deserializing;

import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DeserializerTestBuilder {

  private List<String> listOfArrayHeaderValues = new ArrayList<>();
  private AttributeDeserializer deserializer;
  private List<String> resultValues = new ArrayList<>();
  private Class deserializerClazz;

  public static DeserializerTestBuilder when() {
    return new DeserializerTestBuilder();
  }

  public DeserializerTestBuilder deserializer(AttributeDeserializer deserializer) {
    this.deserializer = deserializer;
    return this;
  }

  public DeserializerTestBuilder headerValue(String value) {
    this.listOfArrayHeaderValues.add(value);
    return this;
  }

  public DeserializerTestBuilder then() {
    this.resultValues = deserializer.deserializeListOfValues(listOfArrayHeaderValues);
    return this;
  }

  public DeserializerTestBuilder on(Class<? extends AttributeDeserializer> deserializerClazz) {
    this.deserializerClazz = deserializerClazz;
    return this;
  }

  public DeserializerTestBuilder assertValues(String... expected) {
    if (!deserializerClazz.equals(deserializer.getClass())) {
      return this;
    }
    if (resultValues.size() < expected.length) {
      Assert.fail("Number of results are lower than expected");
    } else if (resultValues.size() > expected.length) {
      Assert.fail("Number of results are greater than expected");
    }
    for (int i = 0; i < expected.length; i++) {
      assertEquals(expected[i], resultValues.get(i));
    }
    return this;
  }

}
