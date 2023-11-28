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
import org.mule.module.apikit.api.deserializing.ArrayHeaderDelimiter;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mule.module.apikit.api.deserializing.ArrayHeaderDelimiter.COMMA;
import static org.mule.module.apikit.deserializing.DeserializerTestBuilder.when;

@RunWith(Parameterized.class)
public class AttributeDeserializerTest {

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
  @Parameterized.Parameter(1)
  public AttributeDeserializer deserializer;

  @Parameterized.Parameters(name = "Delimiter = {0} - {1}")
  public static Iterable<Object[]> data() throws Exception {
    ArrayHeaderDelimiter comma = COMMA;
    return asList(new Object[][] {
        {comma, new ArrayHeaderAttributeDeserializer(comma)},
        {comma, new DummyAttributeDeserializer()}
        // {semicolon, new ArrayHeaderAttributeDeserializer(semicolon)} uncomment when semicolon value included in
        // ArrayHeaderDelimiter
    });
  }

  protected List<String> listOfArrayHeaderValues;
  protected String delimiter;

  @Before
  public void init() {
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
    String value = "   ";
    String expected = "";
    when()
        .deserializer(deserializer)
        .headerValue(value)
        .then()
        .on(DummyAttributeDeserializer.class)
        .assertValues(expected)
        .on(ArrayHeaderAttributeDeserializer.class)
        .assertValues(expected);

    value = delimiter;
    when()
        .deserializer(deserializer)
        .headerValue(value)
        .then()
        .on(DummyAttributeDeserializer.class)
        .assertValues(value)
        .on(ArrayHeaderAttributeDeserializer.class)
        .assertValues("", "");

    value = delimiter + delimiter;
    when()
        .deserializer(deserializer)
        .headerValue(value)
        .then()
        .on(DummyAttributeDeserializer.class)
        .assertValues(value)
        .on(ArrayHeaderAttributeDeserializer.class)
        .assertValues("", "", "");

    value = delimiter + " ";
    when()
        .deserializer(deserializer)
        .headerValue(value)
        .then()
        .on(DummyAttributeDeserializer.class)
        .assertValues(value)
        .on(ArrayHeaderAttributeDeserializer.class)
        .assertValues("", "");

    value = "\"\"" + delimiter + "\"  \"";
    when()
        .deserializer(deserializer)
        .headerValue(value)
        .then()
        .on(DummyAttributeDeserializer.class)
        .assertValues(value)
        .on(ArrayHeaderAttributeDeserializer.class)
        .assertValues("", "  ");

  }

  @Test
  public void testSingleValueBetweenEnclosingQuotes() {
    String expected = "This is a unique value";
    String value = "\"" + expected + "\"";
    when()
        .deserializer(deserializer)
        .headerValue(value)
        .then()
        .on(DummyAttributeDeserializer.class)
        .assertValues(expected)
        .on(ArrayHeaderAttributeDeserializer.class)
        .assertValues(expected);
  }

  @Test
  public void testValueWithDelimitersBetweenEnclosingQuotes() {
    String expected = "This " + delimiter + " is a unique" + delimiter + " value";
    String value = "\"" + expected + "\"";
    when()
        .deserializer(deserializer)
        .headerValue(value)
        .then()
        .on(DummyAttributeDeserializer.class)
        .assertValues(expected)
        .on(ArrayHeaderAttributeDeserializer.class)
        .assertValues(expected);
  }

  @Test
  public void allowEscapedDoubleQuotesInsideDoubleQuotesInArrayHeaders() {
    String value = "\"This is a \\\"string\\\" with \\\"quotes\\\" inside it\"";
    String expected = "This is a \"string\" with \"quotes\" inside it";
    when()
        .deserializer(deserializer)
        .headerValue(value)
        .then()
        .on(DummyAttributeDeserializer.class)
        .assertValues(expected)
        .on(ArrayHeaderAttributeDeserializer.class)
        .assertValues(expected);

    value = "\"commas, between, quotes\\\"" + delimiter + "\\\"semicolon; between; quotes\"";
    expected = "commas, between, quotes\"" + delimiter + "\"semicolon; between; quotes";
    when()
        .deserializer(deserializer)
        .headerValue(value)
        .then()
        .on(DummyAttributeDeserializer.class)
        .assertValues(expected)
        .on(ArrayHeaderAttributeDeserializer.class)
        .assertValues(expected);
  }

  @Test
  public void deserializeObjectBetweenQuotes() {
    String value = TWO_LEVEL_OBJECT_BETWEEN_QUOTES;
    String expected = TWO_LEVEL_OBJECT;
    when()
        .deserializer(deserializer)
        .headerValue(value)
        .then()
        .on(DummyAttributeDeserializer.class)
        .assertValues(expected)
        .on(ArrayHeaderAttributeDeserializer.class)
        .assertValues(expected);
  }

  @Test
  public void deserializeUnquotedObject() {
    String value = TWO_LEVEL_OBJECT;
    String expected = value;
    when()
        .deserializer(deserializer)
        .headerValue(value)
        .then()
        .on(DummyAttributeDeserializer.class)
        .assertValues(expected)
        .on(ArrayHeaderAttributeDeserializer.class)
        .assertValues(expected);
  }

  @Test
  public void lineFeedOutsideQuotesIsIgnored() {
    String value = "\"This is one result\"" + "\n" + delimiter
        + "\"This is in a different line and should not be in result\"";
    when()
        .deserializer(deserializer)
        .headerValue(value)
        .then()
        .on(DummyAttributeDeserializer.class)
        .assertValues(value)
        .on(ArrayHeaderAttributeDeserializer.class)
        .assertValues("This is one result", "This is in a different line and should not be in result");
  }

  @Test
  public void lineFeedBetweenQuotesIsIncludedInResult() {
    String value = "\"This is one result\n with two lines\"" + delimiter + "\"This is\n another one\"";
    when()
        .deserializer(deserializer)
        .headerValue(value)
        .then()
        .on(DummyAttributeDeserializer.class)
        .assertValues(value)
        .on(ArrayHeaderAttributeDeserializer.class)
        .assertValues("This is one result\n with two lines", "This is\n another one");
  }

  @Test
  public void lineFeedOutsideCurlyBracesIsIgnored() {
    String value = "{\"key\": \"value1\"}" + "\n" + delimiter + "{\"key\": \"value2\"}";
    when()
        .deserializer(deserializer)
        .headerValue(value)
        .then()
        .on(DummyAttributeDeserializer.class)
        .assertValues(value)
        .on(ArrayHeaderAttributeDeserializer.class)
        .assertValues("{\"key\": \"value1\"}", "{\"key\": \"value2\"}");
  }

  @Test
  public void lineFeedBetweenCurlyBracesIsIncludedInResult() {
    String value = TWO_LEVEL_OBJECT_WITH_LINE_FEEDS + delimiter + TWO_LEVEL_OBJECT_WITH_LINE_FEEDS;
    when()
        .deserializer(deserializer)
        .headerValue(value)
        .then()
        .on(DummyAttributeDeserializer.class)
        .assertValues(value)
        .on(ArrayHeaderAttributeDeserializer.class)
        .assertValues(TWO_LEVEL_OBJECT_WITH_LINE_FEEDS, TWO_LEVEL_OBJECT_WITH_LINE_FEEDS);
  }

  @Test
  public void carriageReturnOutsideQuotesIsIgnored() {
    String value = "\"This is one result\"" + "\r" + delimiter + "\"This is another one\"";
    when()
        .deserializer(deserializer)
        .headerValue(value)
        .then()
        .on(DummyAttributeDeserializer.class)
        .assertValues(value)
        .on(ArrayHeaderAttributeDeserializer.class)
        .assertValues("This is one result", "This is another one");
  }

  @Test
  public void carriageReturnBetweenQuotesIsIncludedInResult() {
    String value = "\"This is one result\r with carriage\r returns\"" + delimiter + "\"This is\r another one\"";
    when()
        .deserializer(deserializer)
        .headerValue(value)
        .then()
        .on(DummyAttributeDeserializer.class)
        .assertValues(value)
        .on(ArrayHeaderAttributeDeserializer.class)
        .assertValues("This is one result\r with carriage\r returns", "This is\r another one");
  }

  @Test
  public void carriageReturnBetweenCurlyBracesIsIncludedInResult() {
    String value = "{\"key\":\r \"value1\"}" + delimiter + "{\"key\":\r \"value2\"}";
    when()
        .deserializer(deserializer)
        .headerValue(value)
        .then()
        .on(DummyAttributeDeserializer.class)
        .assertValues(value)
        .on(ArrayHeaderAttributeDeserializer.class)
        .assertValues("{\"key\":\r \"value1\"}", "{\"key\":\r \"value2\"}");
  }

  @Test
  public void deserializeValidObjectArrayHeaders() {
    String value = "{\"type\": \"username\", \"value\": \"testvalue\"}"
        + delimiter
        + "{\"type\": \"password\", \"value\": \"testvalue; second\"}";
    when()
        .deserializer(deserializer)
        .headerValue(value)
        .then()
        .on(DummyAttributeDeserializer.class)
        .assertValues(value)
        .on(ArrayHeaderAttributeDeserializer.class)
        .assertValues("{\"type\": \"username\", \"value\": \"testvalue\"}",
                      "{\"type\": \"password\", \"value\": \"testvalue; second\"}");


    value = "{\"type\": \"fullname\", \"value\": \"Jhon Doe\"}";
    when()
        .deserializer(deserializer)
        .headerValue(value)
        .then()
        .on(DummyAttributeDeserializer.class)
        .assertValues(value)
        .on(ArrayHeaderAttributeDeserializer.class)
        .assertValues("{\"type\": \"fullname\", \"value\": \"Jhon Doe\"}");


    value = TWO_LEVEL_OBJECT + delimiter + TWO_LEVEL_OBJECT;
    when()
        .deserializer(deserializer)
        .headerValue(value)
        .then()
        .on(DummyAttributeDeserializer.class)
        .assertValues(value)
        .on(ArrayHeaderAttributeDeserializer.class)
        .assertValues(TWO_LEVEL_OBJECT, TWO_LEVEL_OBJECT);

    value = TWO_LEVEL_OBJECT_WITH_LINE_FEEDS + delimiter + TWO_LEVEL_OBJECT_WITH_LINE_FEEDS;
    when()
        .deserializer(deserializer)
        .headerValue(value)
        .then()
        .on(DummyAttributeDeserializer.class)
        .assertValues(value)
        .on(ArrayHeaderAttributeDeserializer.class)
        .assertValues(TWO_LEVEL_OBJECT_WITH_LINE_FEEDS, TWO_LEVEL_OBJECT_WITH_LINE_FEEDS);

    value = "\"" + ESCAPED_TWO_LEVEL_OBJECT + delimiter + ESCAPED_TWO_LEVEL_OBJECT + "\"";
    String expected = TWO_LEVEL_OBJECT + delimiter + TWO_LEVEL_OBJECT;
    when()
        .deserializer(deserializer)
        .headerValue(value)
        .then()
        .on(DummyAttributeDeserializer.class)
        .assertValues(expected)
        .on(ArrayHeaderAttributeDeserializer.class)
        .assertValues(expected);

    value = TWO_LEVEL_OBJECT_BETWEEN_QUOTES + delimiter + TWO_LEVEL_OBJECT_BETWEEN_QUOTES;
    when()
        .deserializer(deserializer)
        .headerValue(value)
        .then()
        .on(DummyAttributeDeserializer.class)
        .assertValues(value)
        .on(ArrayHeaderAttributeDeserializer.class)
        .assertValues(TWO_LEVEL_OBJECT, TWO_LEVEL_OBJECT);
  }

  @Test
  public void deserializeValidArrayHeaders() {
    String value = "123" + delimiter + "456" + delimiter + "789";
    when()
        .deserializer(deserializer)
        .headerValue(value)
        .then()
        .on(DummyAttributeDeserializer.class)
        .assertValues(value)
        .on(ArrayHeaderAttributeDeserializer.class)
        .assertValues("123", "456", "789");

    value = "1.213" + delimiter + "456" + delimiter + "\"7,123.213\"";
    when()
        .deserializer(deserializer)
        .headerValue(value)
        .then()
        .on(DummyAttributeDeserializer.class)
        .assertValues(value)
        .on(ArrayHeaderAttributeDeserializer.class)
        .assertValues("1.213", "456", "7,123.213");

    value = "first" + delimiter + "second" + delimiter + "third";
    when()
        .deserializer(deserializer)
        .headerValue(value)
        .then()
        .on(DummyAttributeDeserializer.class)
        .assertValues(value)
        .on(ArrayHeaderAttributeDeserializer.class)
        .assertValues("first", "second", "third");

    value = "\"commas, between, quotes\"" + delimiter + "\"semicolon; between; quotes\"";
    when()
        .deserializer(deserializer)
        .headerValue(value)
        .then()
        .on(DummyAttributeDeserializer.class)
        .assertValues(value)
        .on(ArrayHeaderAttributeDeserializer.class)
        .assertValues("commas, between, quotes", "semicolon; between; quotes");

    value = "1985-04-12T23:20:50.52Z" + delimiter
        + "\"1996-12-19T16:39:57-08:00\"" + delimiter
        + "1937-01-01T12:00:27.87+00:20";
    when()
        .deserializer(deserializer)
        .headerValue(value)
        .then()
        .on(DummyAttributeDeserializer.class)
        .assertValues(value)
        .on(ArrayHeaderAttributeDeserializer.class)
        .assertValues("1985-04-12T23:20:50.52Z", "1996-12-19T16:39:57-08:00", "1937-01-01T12:00:27.87+00:20");

    value = "\""
        + "texto" + delimiter
        + "t\\\"ext\\\"o" + delimiter
        + "t{ext}o" + delimiter
        + "{}texto" + delimiter
        + "texto{}" + delimiter
        + "\\\"texto\\\"{}" + delimiter
        + "{}\\\"texto\\\""
        + "\"";
    String expected = "texto" + delimiter
        + "t\"ext\"o" + delimiter
        + "t{ext}o" + delimiter
        + "{}texto" + delimiter
        + "texto{}" + delimiter
        + "\"texto\"{}" + delimiter
        + "{}\"texto\"";
    when()
        .deserializer(deserializer)
        .headerValue(value)
        .then()
        .on(DummyAttributeDeserializer.class)
        .assertValues(expected)
        .on(ArrayHeaderAttributeDeserializer.class)
        .assertValues(expected);

    value = "[{\"test\": 1},{\"test\": \"I have \\\"quotes\\\" inside\"}]";
    when()
        .deserializer(deserializer)
        .headerValue(value)
        .then()
        .on(DummyAttributeDeserializer.class)
        .assertValues(value)
        .on(ArrayHeaderAttributeDeserializer.class)
        .assertValues(value);
  }

  @Test
  public void deserializeMalformedObjectArrayHeaders() {
    String value = "{";
    when()
        .deserializer(deserializer)
        .headerValue(value)
        .then()
        .on(DummyAttributeDeserializer.class)
        .assertValues(value)
        .on(ArrayHeaderAttributeDeserializer.class)
        .assertValues(value);

    value = "[";
    when()
        .deserializer(deserializer)
        .headerValue(value)
        .then()
        .on(DummyAttributeDeserializer.class)
        .assertValues(value)
        .on(ArrayHeaderAttributeDeserializer.class)
        .assertValues(value);

    value = "[{}}";
    when()
        .deserializer(deserializer)
        .headerValue(value)
        .then()
        .on(DummyAttributeDeserializer.class)
        .assertValues(value)
        .on(ArrayHeaderAttributeDeserializer.class)
        .assertValues(value);

    value = "{[]]";
    when()
        .deserializer(deserializer)
        .headerValue(value)
        .then()
        .on(DummyAttributeDeserializer.class)
        .assertValues(value)
        .on(ArrayHeaderAttributeDeserializer.class)
        .assertValues(value);

    value = "{\"username\"}" + delimiter + "{\"testvalue: first\"}" + delimiter + "{testvalue: second}";
    when()
        .deserializer(deserializer)
        .headerValue(value)
        .then()
        .on(DummyAttributeDeserializer.class)
        .assertValues(value)
        .on(ArrayHeaderAttributeDeserializer.class)
        .assertValues("{\"username\"}", "{\"testvalue: first\"}", "{testvalue: second}");

    value = "{\"type\": \"username\"{" + delimiter + "\"testvalue: second\"}";
    when()
        .deserializer(deserializer)
        .headerValue(value)
        .then()
        .on(DummyAttributeDeserializer.class)
        .assertValues(value)
        .on(ArrayHeaderAttributeDeserializer.class)
        .assertValues(value);

    value = "{\"type\": \"username\"}}" + delimiter + "{}   } ";
    when()
        .deserializer(deserializer)
        .headerValue(value)
        .then()
        .on(DummyAttributeDeserializer.class)
        .assertValues(value)
        .on(ArrayHeaderAttributeDeserializer.class)
        .assertValues("{\"type\": \"username\"}}", "{}   }");

    value = "{\"type\": }\"username\"" + delimiter + "\"testvalue: second\"}";
    when()
        .deserializer(deserializer)
        .headerValue(value)
        .then()
        .on(DummyAttributeDeserializer.class)
        .assertValues(value)
        .on(ArrayHeaderAttributeDeserializer.class)
        .assertValues("{\"type\": }\"username\"", "\"testvalue: second\"}");

    value = "{{{{ }}";
    when()
        .deserializer(deserializer)
        .headerValue(value)
        .then()
        .on(DummyAttributeDeserializer.class)
        .assertValues(value)
        .on(ArrayHeaderAttributeDeserializer.class)
        .assertValues(value);

    value = "{{ }}}}";
    when()
        .deserializer(deserializer)
        .headerValue(value)
        .then()
        .on(DummyAttributeDeserializer.class)
        .assertValues(value)
        .on(ArrayHeaderAttributeDeserializer.class)
        .assertValues(value);

    value = "{{{{ " + delimiter + "}}";
    when()
        .deserializer(deserializer)
        .headerValue(value)
        .then()
        .on(DummyAttributeDeserializer.class)
        .assertValues(value)
        .on(ArrayHeaderAttributeDeserializer.class)
        .assertValues(value);

    value = "asd\"{{{\"asd}}}";
    when()
        .deserializer(deserializer)
        .headerValue(value)
        .then()
        .on(DummyAttributeDeserializer.class)
        .assertValues(value)
        .on(ArrayHeaderAttributeDeserializer.class)
        .assertValues(value);

    value = "asd\\\"{{{\\\"asd}}}";
    when()
        .deserializer(deserializer)
        .headerValue(value)
        .then()
        .on(DummyAttributeDeserializer.class)
        .assertValues(value)
        .on(ArrayHeaderAttributeDeserializer.class)
        .assertValues(value);

    value = "\"\\{\\{\"";
    String expected = "\\{\\{";
    when()
        .deserializer(deserializer)
        .headerValue(value)
        .then()
        .on(DummyAttributeDeserializer.class)
        .assertValues(expected)
        .on(ArrayHeaderAttributeDeserializer.class)
        .assertValues(expected);

    value = "texto" + delimiter
        + "t\"ext\"o" + delimiter
        + "t{ext}o" + delimiter
        + "{}texto" + delimiter
        + "texto{}" + delimiter
        + "\"texto\"{}" + delimiter
        + "{}\"texto\"" + delimiter
        + "\"te\\\"x\\\"to\"{}" + delimiter
        + "{}\"te\\\"x\\\"to\"";
    when()
        .deserializer(deserializer)
        .headerValue(value)
        .then()
        .on(DummyAttributeDeserializer.class)
        .assertValues(value)
        .on(ArrayHeaderAttributeDeserializer.class)
        .assertValues("texto",
                      "t\"ext\"o",
                      "t{ext}o",
                      "{}texto",
                      "texto{}",
                      "\"texto\"{}",
                      "{}\"texto\"",
                      "\"te\\\"x\\\"to\"{}",
                      "{}\"te\\\"x\\\"to\"");

    value = "[{\"test\": 1},{\"test\": 2}";
    when()
        .deserializer(deserializer)
        .headerValue(value)
        .then()
        .on(DummyAttributeDeserializer.class)
        .assertValues(value)
        .on(ArrayHeaderAttributeDeserializer.class)
        .assertValues(value);
  }

}
