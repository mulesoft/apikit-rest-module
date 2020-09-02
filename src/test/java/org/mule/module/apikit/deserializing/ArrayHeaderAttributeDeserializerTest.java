/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.deserializing;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mule.module.apikit.MockingUtils.createEnumValue;
import static org.mule.module.apikit.deserializing.ArrayHeaderDelimiter.COMMA;

@RunWith(Parameterized.class)
public class ArrayHeaderAttributeDeserializerTest {

  private static final String TWO_LEVEL_OBJECT_WITH_LINE_FEEDS = "{\n" +
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

  private ArrayHeaderAttributeDeserializer deserializer;
  private List<String> listOfArrayHeaderValues;
  private String delimiter;
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

  @Before
  public void init() {
    deserializer = new ArrayHeaderAttributeDeserializer(arrayHeaderDelimiter);
    delimiter = arrayHeaderDelimiter.getDelimiterValue();
    listOfArrayHeaderValues = new ArrayList<>();
  }

  @Test
  public void deserializeEmptyArrayHeader() {
    List<String> result = deserializer.deserializeListOfValues(listOfArrayHeaderValues);
    assertNotNull(result);
    assertEquals(0, result.size());
  }

  @Test
  public void ignoreBlankValuesInArrayHeaders() {
    listOfArrayHeaderValues.add("   ");
    listOfArrayHeaderValues.add("" + delimiter + " ");
    listOfArrayHeaderValues.add("\"\"" + delimiter + "\"  \"");
    List<String> result = deserializer.deserializeListOfValues(listOfArrayHeaderValues);
    assertNotNull(result);
    assertEquals(0, result.size());
  }

  @Test
  public void testSingleValueBetweenEnclosingQuotes() {
    List<String> result = deserializer.deserializeValue("\"This is a unique value\"");
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("This is a unique value", result.get(0));
  }

  @Test
  public void testValueWithDelimitersBetweenEnclosingQuotes() {
    List<String> result = deserializer.deserializeValue("\"This " + delimiter + " is a unique" + delimiter + " value\"");
    assertEquals(1, result.size());
    assertEquals("This " + delimiter + " is a unique" + delimiter + " value", result.get(0));
  }

  @Test
  public void allowDoubleQuotesInsideDoubleQuotesInArrayHeaders() {
    List<String> result = deserializer.deserializeValue("\"\"\"\"\"\"\"");
    assertEquals(1, result.size());
    assertEquals("\"\"\"", result.get(0));
  }

  @Test
  public void deserializeObjectBetweenQuotes() {
    listOfArrayHeaderValues.add(TWO_LEVEL_OBJECT_BETWEEN_QUOTES);
    List<String> result = deserializer.deserializeListOfValues(listOfArrayHeaderValues);
    assertEquals(1, result.size());
    assertEquals(TWO_LEVEL_OBJECT, result.get(0));
  }

  @Test
  public void lineFeedOutsideQuotesIsIgnored() {
    List<String> result = deserializer
        .deserializeValue("\"This is one result\"" + "\n" + delimiter
            + "\"This is in a different line and should not be in result\"");
    assertEquals(2, result.size());
    assertEquals("This is one result", result.get(0));
    assertEquals("This is in a different line and should not be in result", result.get(1));
  }

  @Test
  public void lineFeedBetweenQuotesIsIncludedInResult() {
    List<String> result =
        deserializer.deserializeValue("\"This is one result\n with two lines\"" + delimiter + "\"This is\n another one\"");
    assertEquals(2, result.size());
    assertEquals("This is one result\n with two lines", result.get(0));
    assertEquals("This is\n another one", result.get(1));
  }

  @Test
  public void lineFeedOutsideCurlyBracesIsIgnored() {
    List<String> result = deserializer.deserializeValue("{\"key\": \"value1\"}" + "\n" + delimiter + "{\"key\": \"value2\"}");
    assertEquals(2, result.size());
    assertEquals("{\"key\": \"value1\"}", result.get(0));
    assertEquals("{\"key\": \"value2\"}", result.get(1));
  }

  @Test
  public void lineFeedBetweenCurlyBracesIsIncludedInResult() {
    List<String> result =
        deserializer.deserializeValue(TWO_LEVEL_OBJECT_WITH_LINE_FEEDS + delimiter + TWO_LEVEL_OBJECT_WITH_LINE_FEEDS);
    assertEquals(2, result.size());
    assertEquals(TWO_LEVEL_OBJECT_WITH_LINE_FEEDS, result.get(0));
    assertEquals(TWO_LEVEL_OBJECT_WITH_LINE_FEEDS, result.get(1));
  }

  @Test
  public void carriageReturnOutsideQuotesIsIgnored() {
    List<String> result = deserializer
        .deserializeValue("\"This is one result\"" + "\r" + delimiter + "\"This is another one\"");
    assertEquals(2, result.size());
    assertEquals("This is one result", result.get(0));
    assertEquals("This is another one", result.get(1));
  }

  @Test
  public void carriageReturnBetweenQuotesIsIncludedInResult() {
    List<String> result =
        deserializer
            .deserializeValue("\"This is one result\r with carriage\r returns\"" + delimiter + "\"This is\r another one\"");
    assertEquals(2, result.size());
    assertEquals("This is one result\r with carriage\r returns", result.get(0));
    assertEquals("This is\r another one", result.get(1));
  }

  @Test
  public void carriageReturnBetweenCurlyBracesIsIncludedInResult() {
    List<String> result = deserializer.deserializeValue("{\"key\":\r \"value1\"}" + delimiter + "{\"key\":\r \"value2\"}");
    assertEquals(2, result.size());
    assertEquals("{\"key\":\r \"value1\"}", result.get(0));
    assertEquals("{\"key\":\r \"value2\"}", result.get(1));
  }

  @Test
  public void deserializeValidObjectArrayHeaders() {
    listOfArrayHeaderValues.add("{\"type\": \"username\", \"value\": \"testvalue\"}" +
        delimiter +
        "{\"type\": \"password\", \"value\": \"testvalue; second\"}");
    listOfArrayHeaderValues.add("{\"type\": \"fullname\", \"value\": \"Jhon Doe\"}");
    listOfArrayHeaderValues.add(TWO_LEVEL_OBJECT + delimiter + TWO_LEVEL_OBJECT);
    listOfArrayHeaderValues.add(TWO_LEVEL_OBJECT_WITH_LINE_FEEDS + delimiter + TWO_LEVEL_OBJECT_WITH_LINE_FEEDS);
    listOfArrayHeaderValues.add("\"" + TWO_LEVEL_OBJECT + delimiter + TWO_LEVEL_OBJECT + "\"");
    listOfArrayHeaderValues.add(TWO_LEVEL_OBJECT_BETWEEN_QUOTES + delimiter + TWO_LEVEL_OBJECT_BETWEEN_QUOTES);
    List<String> result = deserializer.deserializeListOfValues(listOfArrayHeaderValues);
    assertEquals(10, result.size());
    assertEquals(TWO_LEVEL_OBJECT, result.get(4));
    assertEquals(TWO_LEVEL_OBJECT_WITH_LINE_FEEDS, result.get(5));
    assertEquals(TWO_LEVEL_OBJECT + delimiter + TWO_LEVEL_OBJECT, result.get(7));
    assertEquals(TWO_LEVEL_OBJECT, result.get(9));
  }

  @Test
  public void deserializeValidArrayHeaders() {
    listOfArrayHeaderValues.add("123" + delimiter + "456" + delimiter + "789");
    listOfArrayHeaderValues.add("1.213" + delimiter + "456" + delimiter + "\"7,123.213\"");
    listOfArrayHeaderValues.add("first" + delimiter + "second" + delimiter + "third");
    listOfArrayHeaderValues.add("\"commas, between, quotes\"" + delimiter + "\"semicolon; between; quotes\"");
    listOfArrayHeaderValues.add("1985-04-12T23:20:50.52Z" + delimiter + "\"1996-12-19T16:39:57-08:00\"" + delimiter
        + "1937-01-01T12:00:27.87+00:20");
    List<String> result = deserializer.deserializeListOfValues(listOfArrayHeaderValues);
    assertEquals(14, result.size());
    assertEquals(2, result.stream().filter(v -> "456".equals(v)).count());
    assertEquals("commas, between, quotes", result.get(9));
    assertEquals("semicolon; between; quotes", result.get(10));
    assertEquals("1996-12-19T16:39:57-08:00", result.get(12));
    assertEquals("1937-01-01T12:00:27.87+00:20", result.get(13));
  }

  @Test
  public void deserializeMalformedObjectArrayHeaders() {
    listOfArrayHeaderValues.add("{\"type\": \"username\"{" + delimiter + "\"testvalue: second\"}");
    listOfArrayHeaderValues.add("{\"type\": }\"username\"" + delimiter + "\"testvalue: second\"}");
    listOfArrayHeaderValues.add("{{{{ }}");
    listOfArrayHeaderValues.add("{{ }}}}");
    listOfArrayHeaderValues.add("{{{{ " + delimiter + "}}");
    List<String> result = deserializer.deserializeListOfValues(listOfArrayHeaderValues);
    assertEquals(6, result.size());
    assertEquals("{\"type\": \"username\"{" + delimiter + "\"testvalue: second\"}", result.get(0));
    assertEquals("{\"type\": }username", result.get(1));
    assertEquals("testvalue: second}", result.get(2));
    assertEquals("{{{{ }}", result.get(3));
    assertEquals("{{ }}}}", result.get(4));
    assertEquals("{{{{ " + delimiter + "}}", result.get(5));
  }
}
