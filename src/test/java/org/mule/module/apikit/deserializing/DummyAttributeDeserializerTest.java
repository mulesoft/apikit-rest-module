/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.deserializing;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DummyAttributeDeserializerTest {

  private final static String DUMMY_DELIMITER = ",";

  private static final String TWO_LEVEL_OBJECT_WITH_RETURNS = "{\n" +
      "  \"color\": \"RED\",\n" +
      "  \"manufacturer\": {\n" +
      "    \"brand\": \"Ferrari\"\n" +
      "  }\n" +
      "  \"reseller\": {\n" +
      "    \"name\": \"YourCar\"\n" +
      "  }\n" +
      "}";
  private static final String TWO_LEVEL_OBJECT =
      "{\"color\": \"RED\", \"manufacturer\": {\"brand\": \"Ferrari\"}, \"reseller\": {\"name\": \"YourCar\"}}";
  private static final String TWO_LEVEL_OBJECT_BETWEEN_QUOTES = "\"" + TWO_LEVEL_OBJECT + "\"";

  private DummyAttributeDeserializer deserializer;
  private List<String> listOfArrayHeaderValues;


  @Before
  public void init() {
    deserializer = new DummyAttributeDeserializer();
    listOfArrayHeaderValues = new ArrayList<>();
  }

  @Test
  public void deserializeEmptyArrayHeader() {
    List<String> result = deserializer.deserializeListOfValues(listOfArrayHeaderValues);
    assertNotNull(result);
    assertEquals(0, result.size());
  }

  @Test
  public void keepBlanksInArrayHeaders() {
    listOfArrayHeaderValues.add("   ");
    listOfArrayHeaderValues.add("" + DUMMY_DELIMITER + " ");
    listOfArrayHeaderValues.add("\"\"" + DUMMY_DELIMITER + "\"  \"");
    List<String> result = deserializer.deserializeListOfValues(listOfArrayHeaderValues);
    assertNotNull(result);
    assertEquals(3, result.size());
    assertEquals("   ", result.get(0));
    assertEquals("" + DUMMY_DELIMITER + " ", result.get(1));
    assertEquals("\"\"" + DUMMY_DELIMITER + "\"  \"", result.get(2));
  }

  @Test
  public void testSingleValueBetweenEnclosingQuotes() {
    List<String> result = deserializer.deserializeValue("\"This is a unique value\"");
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("\"This is a unique value\"", result.get(0));
  }

  @Test
  public void testValueWithDelimitersBetweenEnclosingQuotes() {
    List<String> result =
        deserializer.deserializeValue("\"This " + DUMMY_DELIMITER + " is a unique" + DUMMY_DELIMITER + " value\"");
    assertEquals(1, result.size());
    assertEquals("\"This " + DUMMY_DELIMITER + " is a unique" + DUMMY_DELIMITER + " value\"", result.get(0));
  }

  @Test
  public void allowDoubleQuotesInsideDoubleQuotesInArrayHeaders() {
    List<String> result = deserializer.deserializeValue("\"\"\"\"\"\"\"");
    assertEquals(1, result.size());
    assertEquals("\"\"\"\"\"\"\"", result.get(0));
  }

  @Test
  public void deserializeObjectBetweenQuotes() {
    listOfArrayHeaderValues.add(TWO_LEVEL_OBJECT_BETWEEN_QUOTES);
    List<String> result = deserializer.deserializeListOfValues(listOfArrayHeaderValues);
    assertEquals(1, result.size());
    assertEquals(TWO_LEVEL_OBJECT_BETWEEN_QUOTES, result.get(0));
  }

  @Test
  public void deserializeValidObjectArrayHeaders() {
    listOfArrayHeaderValues.add(TWO_LEVEL_OBJECT + DUMMY_DELIMITER + TWO_LEVEL_OBJECT);
    listOfArrayHeaderValues.add(TWO_LEVEL_OBJECT_WITH_RETURNS + DUMMY_DELIMITER + TWO_LEVEL_OBJECT_WITH_RETURNS);
    List<String> result = deserializer.deserializeListOfValues(listOfArrayHeaderValues);
    assertEquals(2, result.size());
    assertEquals(TWO_LEVEL_OBJECT + DUMMY_DELIMITER + TWO_LEVEL_OBJECT, result.get(0));
    assertEquals(TWO_LEVEL_OBJECT_WITH_RETURNS + DUMMY_DELIMITER + TWO_LEVEL_OBJECT_WITH_RETURNS, result.get(1));
  }

  @Test
  public void deserializeValidArrayHeaders() {
    listOfArrayHeaderValues.add("123" + DUMMY_DELIMITER + "456" + DUMMY_DELIMITER + "789");
    listOfArrayHeaderValues.add("1.213" + DUMMY_DELIMITER + "456" + DUMMY_DELIMITER + "\"7,123.213\"");
    listOfArrayHeaderValues.add("first" + DUMMY_DELIMITER + "second" + DUMMY_DELIMITER + "third");
    listOfArrayHeaderValues.add("\"commas, between, quotes\"" + DUMMY_DELIMITER + "\"semicolon; between; quotes\"");
    listOfArrayHeaderValues.add("1985-04-12T23:20:50.52Z" + DUMMY_DELIMITER + "\"1996-12-19T16:39:57-08:00\"" + DUMMY_DELIMITER
        + "1937-01-01T12:00:27.87+00:20");
    List<String> result = deserializer.deserializeListOfValues(listOfArrayHeaderValues);
    assertEquals("123" + DUMMY_DELIMITER + "456" + DUMMY_DELIMITER + "789", result.get(0));
    assertEquals("\"commas, between, quotes\"" + DUMMY_DELIMITER + "\"semicolon; between; quotes\"", result.get(3));
    assertEquals("1985-04-12T23:20:50.52Z" + DUMMY_DELIMITER + "\"1996-12-19T16:39:57-08:00\"" + DUMMY_DELIMITER
        + "1937-01-01T12:00:27.87+00:20", result.get(4));
  }

  @Test
  public void deserializeMalformedObjectArrayHeaders() {
    listOfArrayHeaderValues.add("{\"type\": \"username\"{" + DUMMY_DELIMITER + "\"testvalue: second\"}");
    listOfArrayHeaderValues.add("{{ }}}}");
    listOfArrayHeaderValues.add("{{{{ " + DUMMY_DELIMITER + "}}");
    List<String> result = deserializer.deserializeListOfValues(listOfArrayHeaderValues);
    assertEquals(3, result.size());
    assertEquals("{\"type\": \"username\"{" + DUMMY_DELIMITER + "\"testvalue: second\"}", result.get(0));
    assertEquals("{{ }}}}", result.get(1));
    assertEquals("{{{{ " + DUMMY_DELIMITER + "}}", result.get(2));
  }
}
