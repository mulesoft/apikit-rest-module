/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.deserializing;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mule.module.apikit.api.deserializing.ArrayHeaderDelimiter;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static org.mule.module.apikit.MockingUtils.createEnumValue;
import static org.mule.module.apikit.api.deserializing.ArrayHeaderDelimiter.COMMA;

@RunWith(Parameterized.class)
public abstract class AbstractDeserializerTest {

  protected static final String TWO_LEVEL_OBJECT =
      "{\"color\": \"RED\", \"manufacturer\": {\"brand\": \"Ferrari\"}, \"reseller\": {\"name\": \"YourCar\"}}";
  protected static final String ESCAPED_TWO_LEVEL_OBJECT =
      "{\\\"color\\\": \\\"RED\\\", \\\"manufacturer\\\": {\\\"brand\\\": \\\"Ferrari\\\"}, \\\"reseller\\\": {\\\"name\\\": \\\"YourCar\\\"}}";

  protected static final String TWO_LEVEL_OBJECT_WITH_LINE_FEEDS = "{\n" +
      "  \"color\": \"RED\",\n" +
      "  \"manufacturer\": {\n" +
      "    \"brand\": \"Ferrari\"\n" +
      "  }\n" +
      "  \"reseller\": {\n" +
      "    \"name\": \"YourCar\"\n" +
      "  }\n" +
      "}";

  protected static final String TWO_LEVEL_OBJECT_BETWEEN_QUOTES = "\"" + ESCAPED_TWO_LEVEL_OBJECT + "\"";

  @Parameterized.Parameter
  public ArrayHeaderDelimiter arrayHeaderDelimiter;

  @Parameterized.Parameters(name = "Delimiter = {0}")
  public static Iterable<Object[]> data() throws Exception {
    ArrayHeaderDelimiter comma = COMMA;
    ArrayHeaderDelimiter semicolon = createEnumValue(ArrayHeaderDelimiter.class, "SEMICOLON", 2, ";");
    return asList(new Object[][] {
        {comma},
        {semicolon},
    });
  }

  protected AttributeDeserializer deserializer;
  protected List<String> listOfArrayHeaderValues;
  protected String delimiter;

  protected List<String> getDeserializedValuesFor(String value, String... others) {
    listOfArrayHeaderValues = new ArrayList<>();
    listOfArrayHeaderValues.add(value);
    if (others.length != 0) {
      stream(others).forEach(listOfArrayHeaderValues::add);
    }
    return deserializer.deserializeListOfValues(listOfArrayHeaderValues);
  }
}
