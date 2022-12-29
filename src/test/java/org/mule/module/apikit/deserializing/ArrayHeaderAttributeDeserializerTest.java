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

public class ArrayHeaderAttributeDeserializerTest extends AbstractDeserializerTest {

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
  public void blankValuesAsEmptyStringInArrayHeaders() {
    List<String> result = getDeserializedValuesFor("   ");
    assertEquals("", result.get(0));

    // Should we consider a blank value on the left side of delimiter??
    result = getDeserializedValuesFor(delimiter + " ");
    assertEquals("", result.get(0));

    result = getDeserializedValuesFor("\"\"" + delimiter + "\"  \"");
    assertEquals("", result.get(0));
    assertEquals("  ", result.get(1));
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
    List<String> result = getDeserializedValuesFor("\"This " + delimiter + " is a unique" + delimiter + " value\"");
    assertEquals(1, result.size());
    assertEquals("This " + delimiter + " is a unique" + delimiter + " value", result.get(0));
  }

  @Test
  public void allowEscapedDoubleQuotesInsideDoubleQuotesInArrayHeaders() {
    List<String> result = getDeserializedValuesFor("\"This is a \\\"string\\\" with \\\"quotes\\\" inside it\"");
    assertEquals(1, result.size());
    assertEquals("This is a \"string\" with \"quotes\" inside it", result.get(0));
  }

  @Test
  public void deserializeObjectBetweenQuotes() {
    List<String> result = getDeserializedValuesFor(TWO_LEVEL_OBJECT_BETWEEN_QUOTES);
    assertEquals(1, result.size());
    assertEquals(TWO_LEVEL_OBJECT, result.get(0));
  }

  @Test
  public void deserializeUnquotedObject() {
    List<String> result = getDeserializedValuesFor(TWO_LEVEL_OBJECT);
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
        getDeserializedValuesFor("\"This is one result\n with two lines\"" + delimiter + "\"This is\n another one\"");
    assertEquals(2, result.size());
    assertEquals("This is one result\n with two lines", result.get(0));
    assertEquals("This is\n another one", result.get(1));
  }

  @Test
  public void lineFeedOutsideCurlyBracesIsIgnored() {
    List<String> result = getDeserializedValuesFor("{\"key\": \"value1\"}" + "\n" + delimiter + "{\"key\": \"value2\"}");
    assertEquals(2, result.size());
    assertEquals("{\"key\": \"value1\"}", result.get(0));
    assertEquals("{\"key\": \"value2\"}", result.get(1));
  }

  @Test
  public void lineFeedBetweenCurlyBracesIsIncludedInResult() {
    List<String> result =
        getDeserializedValuesFor(TWO_LEVEL_OBJECT_WITH_LINE_FEEDS + delimiter + TWO_LEVEL_OBJECT_WITH_LINE_FEEDS);
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
    List<String> result = getDeserializedValuesFor("{\"key\":\r \"value1\"}" + delimiter + "{\"key\":\r \"value2\"}");
    assertEquals(2, result.size());
    assertEquals("{\"key\":\r \"value1\"}", result.get(0));
    assertEquals("{\"key\":\r \"value2\"}", result.get(1));
  }

  @Test
  public void deserializeValidObjectArrayHeaders() {
    List<String> result = getDeserializedValuesFor("{\"type\": \"username\", \"value\": \"testvalue\"}" +
        delimiter +
        "{\"type\": \"password\", \"value\": \"testvalue; second\"}");
    assertEquals("{\"type\": \"password\", \"value\": \"testvalue; second\"}", result.get(1));

    result = getDeserializedValuesFor("{\"type\": \"fullname\", \"value\": \"Jhon Doe\"}");
    assertEquals("{\"type\": \"fullname\", \"value\": \"Jhon Doe\"}", result.get(0));

    result = getDeserializedValuesFor(TWO_LEVEL_OBJECT + delimiter + TWO_LEVEL_OBJECT);
    assertEquals(TWO_LEVEL_OBJECT, result.get(1));

    result = getDeserializedValuesFor(TWO_LEVEL_OBJECT_WITH_LINE_FEEDS + delimiter + TWO_LEVEL_OBJECT_WITH_LINE_FEEDS);
    assertEquals(TWO_LEVEL_OBJECT_WITH_LINE_FEEDS, result.get(1));

    result = getDeserializedValuesFor("\"" + ESCAPED_TWO_LEVEL_OBJECT + delimiter + ESCAPED_TWO_LEVEL_OBJECT + "\"");
    assertEquals(TWO_LEVEL_OBJECT + delimiter + TWO_LEVEL_OBJECT, result.get(0));

    result = getDeserializedValuesFor(TWO_LEVEL_OBJECT_BETWEEN_QUOTES + delimiter + TWO_LEVEL_OBJECT_BETWEEN_QUOTES);
    assertEquals(TWO_LEVEL_OBJECT, result.get(0));
  }

  @Test
  public void deserializeValidArrayHeaders() {
    List<String> result = getDeserializedValuesFor("123" + delimiter + "456" + delimiter + "789");
    assertEquals(1, result.stream().filter(v -> "456".equals(v)).count());

    result = getDeserializedValuesFor("1.213" + delimiter + "456" + delimiter + "\"7,123.213\"");
    assertEquals(1, result.stream().filter(v -> "456".equals(v)).count());

    result = getDeserializedValuesFor("first" + delimiter + "second" + delimiter + "third");
    assertEquals("first", result.get(0));

    result = getDeserializedValuesFor("\"commas, between, quotes\"" + delimiter + "\"semicolon; between; quotes\"");
    assertEquals("commas, between, quotes", result.get(0));
    assertEquals("semicolon; between; quotes", result.get(1));

    result = getDeserializedValuesFor("1985-04-12T23:20:50.52Z" + delimiter + "\"1996-12-19T16:39:57-08:00\"" + delimiter
        + "1937-01-01T12:00:27.87+00:20");
    assertEquals("1996-12-19T16:39:57-08:00", result.get(1));
    assertEquals("1937-01-01T12:00:27.87+00:20", result.get(2));

    result = getDeserializedValuesFor("\""
        + "texto" + delimiter
        + "t\\\"ext\\\"o" + delimiter
        + "t{ext}o" + delimiter
        + "{}texto" + delimiter
        + "texto{}" + delimiter
        + "\\\"texto\\\"{}" + delimiter
        + "{}\\\"texto\\\""
        + "\"");
    assertEquals("texto" + delimiter
        + "t\"ext\"o" + delimiter
        + "t{ext}o" + delimiter
        + "{}texto" + delimiter
        + "texto{}" + delimiter
        + "\"texto\"{}" + delimiter
        + "{}\"texto\"",
                 result.get(0));
  }

  @Test
  public void deserializeMalformedObjectArrayHeaders() {
    List<String> result = getDeserializedValuesFor("{\"type\": \"username\"{" + delimiter + "\"testvalue: second\"}");
    assertEquals("{\"type\": \"username\"{" + delimiter + "\"testvalue: second\"}", result.get(0));

    result = getDeserializedValuesFor("{\"type\": }\"username\"" + delimiter + "\"testvalue: second\"}");
    assertEquals("{\"type\": }\"username\"", result.get(0));
    assertEquals("\"testvalue: second\"}", result.get(1));

    result = getDeserializedValuesFor("{{{{ }}");
    assertEquals("{{{{ }}", result.get(0));

    result = getDeserializedValuesFor("{{ }}}}");
    assertEquals("{{ }}}}", result.get(0));

    result = getDeserializedValuesFor("{{{{ " + delimiter + "}}");
    assertEquals("{{{{ " + delimiter + "}}", result.get(0));

    result = getDeserializedValuesFor("asd\"{{{\"asd}}}");
    assertEquals("asd\"{{{\"asd}}}", result.get(0));

    result = getDeserializedValuesFor("asd\\\"{{{\\\"asd}}}");
    assertEquals("asd\\\"{{{\\\"asd}}}", result.get(0));

    result = getDeserializedValuesFor("\"\\{\\{\"");
    assertEquals("\\{\\{", result.get(0));

    result = getDeserializedValuesFor(
                                      "texto" + delimiter
                                          + "t\"ext\"o" + delimiter
                                          + "t{ext}o" + delimiter
                                          + "{}texto" + delimiter
                                          + "texto{}" + delimiter
                                          +
                                          "\"texto\"{}" + delimiter
                                          +
                                          "{}\"texto\"" + delimiter
                                          +
                                          "\"te\\\"x\\\"to\"{}" + delimiter
                                          +
                                          "{}\"te\\\"x\\\"to\"");
    assertEquals("texto", result.get(0));
    assertEquals("t\"ext\"o", result.get(1));
    assertEquals("t{ext}o", result.get(2));
    assertEquals("{}texto", result.get(3));
    assertEquals("texto{}", result.get(4));
    assertEquals("\"texto\"{}", result.get(5));
    assertEquals("{}\"texto\"", result.get(6));
    assertEquals("\"te\\\"x\\\"to\"{}", result.get(7));
    assertEquals("{}\"te\\\"x\\\"to\"", result.get(8));
  }

}
