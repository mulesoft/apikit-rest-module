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

public class DummyAttributeDeserializerTest extends AbstractDeserializerTest {

  @Before
  public void init() {
    deserializer = new DummyAttributeDeserializer();
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
  public void blankValuesAsEmptyStringInArrayHeaders() {
    List<String> result = getDeserializedValuesFor("   ");
    assertEquals("", result.get(0));

    result = getDeserializedValuesFor("");
    assertEquals("", result.get(0));
  }

  @Test
  public void unbalancedQuotesResultInSameString() {
    List<String> result = getDeserializedValuesFor(delimiter + " ");
    assertEquals(delimiter + " ", result.get(0));

    result = getDeserializedValuesFor("\"\"" + delimiter + "\"  \"");
    assertEquals("\"\"" + delimiter + "\"  \"", result.get(0));
  }

  @Test
  public void testSingleValueBetweenEnclosingQuotes() {
    List<String> result = getDeserializedValuesFor("\"This is a unique value\"");
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("This is a unique value", result.get(0));
  }

  @Test
  public void testValueWithDelimitersBetweenEnclosingQuotes() {
    List<String> result =
        getDeserializedValuesFor("\"This " + delimiter + " is a unique" + delimiter + " value\"");
    assertEquals(1, result.size());
    assertEquals("This " + delimiter + " is a unique" + delimiter + " value", result.get(0));
  }

  @Test
  public void allowEscapedDoubleQuotesInsideDoubleQuotesInArrayHeaders() {
    List<String> result = getDeserializedValuesFor("\"This is a \\\"string\\\" with \\\"quotes\\\" inside it\"");
    assertEquals(1, result.size());
    assertEquals("This is a \"string\" with \"quotes\" inside it", result.get(0));

    result = getDeserializedValuesFor("\"commas, between, quotes\\\"" + delimiter + "\\\"semicolon; between; quotes\"");
    assertEquals("commas, between, quotes\"" + delimiter + "\"semicolon; between; quotes", result.get(0));
  }

  @Test
  public void deserializeObjectBetweenQuotes() {
    List<String> result = getDeserializedValuesFor(TWO_LEVEL_OBJECT_BETWEEN_QUOTES);
    assertEquals(1, result.size());
    assertEquals(TWO_LEVEL_OBJECT, result.get(0));
  }

  @Test
  public void deserializeValidObjectArrayHeaders() {
    List<String> result = getDeserializedValuesFor(TWO_LEVEL_OBJECT + delimiter + TWO_LEVEL_OBJECT);
    assertEquals(TWO_LEVEL_OBJECT + delimiter + TWO_LEVEL_OBJECT, result.get(0));

    result = getDeserializedValuesFor(TWO_LEVEL_OBJECT_WITH_LINE_FEEDS + delimiter + TWO_LEVEL_OBJECT_WITH_LINE_FEEDS);
    assertEquals(TWO_LEVEL_OBJECT_WITH_LINE_FEEDS + delimiter + TWO_LEVEL_OBJECT_WITH_LINE_FEEDS, result.get(0));
  }

  @Test
  public void deserializeValidArrayHeaders() {
    List<String> result = getDeserializedValuesFor("123" + delimiter + "456" + delimiter + "789");
    assertEquals("123" + delimiter + "456" + delimiter + "789", result.get(0));

    result = getDeserializedValuesFor("1.213" + delimiter + "456" + delimiter + "\"7,123.213\"");
    assertEquals("1.213" + delimiter + "456" + delimiter + "\"7,123.213\"", result.get(0));

    result = getDeserializedValuesFor("first" + delimiter + "second" + delimiter + "third");
    assertEquals("first" + delimiter + "second" + delimiter + "third", result.get(0));

    result = getDeserializedValuesFor("\"commas, between, quotes\"" + delimiter + "\"semicolon; between; quotes\"");
    assertEquals("\"commas, between, quotes\"" + delimiter + "\"semicolon; between; quotes\"", result.get(0));

    result = getDeserializedValuesFor("1985-04-12T23:20:50.52Z" + delimiter + "\"1996-12-19T16:39:57-08:00\"" + delimiter
        + "1937-01-01T12:00:27.87+00:20");
    assertEquals("1985-04-12T23:20:50.52Z" + delimiter + "\"1996-12-19T16:39:57-08:00\"" + delimiter
        + "1937-01-01T12:00:27.87+00:20", result.get(0));
  }

  @Test
  public void deserializeMalformedObjectArrayHeaders() {
    List<String> result = getDeserializedValuesFor("{\"type\": \"username\"{" + delimiter + "\"testvalue: second\"}");
    assertEquals("{\"type\": \"username\"{" + delimiter + "\"testvalue: second\"}", result.get(0));

    result = getDeserializedValuesFor("{{ }}}}");
    assertEquals("{{ }}}}", result.get(0));

    result = getDeserializedValuesFor("{{{{ " + delimiter + "}}");
    assertEquals("{{{{ " + delimiter + "}}", result.get(0));

    result = getDeserializedValuesFor("asd\"{{{\"asd}}}");
    assertEquals("asd\"{{{\"asd}}}", result.get(0));

    result = getDeserializedValuesFor("\"\\{\\{\"");
    assertEquals("\\{\\{", result.get(0));

    result = getDeserializedValuesFor("texto,t\"ext\"o,t{ext}o,{}texto,texto{},\"texto\"{},{}\"texto\"");
    assertEquals("texto,t\"ext\"o,t{ext}o,{}texto,texto{},\"texto\"{},{}\"texto\"", result.get(0));

    result = getDeserializedValuesFor("\"texto,t\\\"ext\\\"o,t{ext}o,{}texto,texto{},\\\"texto\\\"{},{}\\\"texto\\\"\"");
    assertEquals("texto,t\"ext\"o,t{ext}o,{}texto,texto{},\"texto\"{},{}\"texto\"", result.get(0));
  }
}
