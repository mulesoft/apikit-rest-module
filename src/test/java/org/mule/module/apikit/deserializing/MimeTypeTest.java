/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.deserializing;

import org.junit.Test;
import org.mule.module.apikit.deserializing.MimeType.MimeTypeParseException;
import org.mule.module.apikit.deserializing.MimeType.Parameter;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class MimeTypeTest {

  @Test
  public void canDeserializeASimpleMime() throws MimeTypeParseException {
    assertEquals(new MimeType("text", "plain", list()), MimeType.from("text/plain"));
  }

  @Test
  public void canDeserializeAMimeWithParameters() throws MimeTypeParseException {
    MimeType expected = new MimeType("text", "plain",
                                     list(new Parameter("param1", "value1")));
    assertEquals(expected, MimeType.from("text/plain; param1=value1"));
  }

  @Test
  public void canDeserializeAMimeWithMultipleParameters() throws MimeTypeParseException {
    MimeType expected = new MimeType("text", "plain",
                                     list(new Parameter("param1", "value1"),
                                          new Parameter("param2", "value2")));
    assertEquals(expected, MimeType.from("text/plain; param1=value1; param2=value2"));
  }

  @Test
  public void canDeserializeAMimeWithQuoteDelimitedParameterValues() throws MimeTypeParseException {
    MimeType expected = new MimeType("text", "plain",
                                     list(new Parameter("param1", "value 1"),
                                          new Parameter("param2", "value 2")));
    assertEquals(expected, MimeType.from("text/plain; param1=\"value 1\"; param2=\"value 2\""));
  }

  @Test
  public void supportsMalformedTypesWithoutSubtype() throws MimeTypeParseException {
    assertEquals(new MimeType("text", "*", list()), MimeType.from("text"));
  }

  @Test
  public void supportsParametersOnMalformedTypesWithoutSubtype() throws MimeTypeParseException {
    MimeType expected = new MimeType("text", "*",
                                     list(new Parameter("param1", "value1")));
    assertEquals(expected, MimeType.from("text; param1=value1"));
  }

  @Test
  public void ignoresUnimportantSpaces() throws MimeTypeParseException {
    assertEquals(MimeType.from("  image  /  png  ;  hasCat  = \"true\""), MimeType.from("image/png; hasCat=true"));
  }

  @Test
  public void supportsRepeatedParameters() throws MimeTypeParseException {
    MimeType expected = new MimeType("text", "plain",
                                     list(new Parameter("param", "value1"),
                                          new Parameter("param", "value2")));
    assertEquals(expected, MimeType.from("text/plain; param=value1; param=value2"));
  }

  @Test
  public void ignoresEscapedQuotesInsideQuotes() throws MimeTypeParseException {
    MimeType expected = new MimeType("text", "plain",
                                     list(new Parameter("param", "value \"1\"")));
    assertEquals(expected, MimeType.from("text/plain; param=\"value \\\"1\\\"\""));
  }

  @Test
  public void failsIfAdditionalTextIsAtTheEndOfAValidMime() {
    assertThrows(MimeTypeParseException.class, () -> MimeType.from("text/plain error"));
    assertThrows(MimeTypeParseException.class, () -> MimeType.from("text/plain; param=value other text"));
    assertThrows(MimeTypeParseException.class, () -> MimeType.from("text/plain; param=\"the value\" other text"));
    assertThrows(MimeTypeParseException.class, () -> MimeType.from("text/plain; ="));
    assertThrows(MimeTypeParseException.class, () -> MimeType.from("text/plain; =value"));
    assertThrows(MimeTypeParseException.class, () -> MimeType.from("text/plain; =value"));
  }

  @Test
  public void failsIfTextIsMissing() {
    assertThrows(MimeTypeParseException.class, () -> MimeType.from("text/"));
    assertThrows(MimeTypeParseException.class, () -> MimeType.from("text/plain;"));
    assertThrows(MimeTypeParseException.class, () -> MimeType.from("text/plain; param"));
    assertThrows(MimeTypeParseException.class, () -> MimeType.from("text/plain; param="));
    assertThrows(MimeTypeParseException.class, () -> MimeType.from("text/plain; param=value;"));
  }

  @Test
  public void failsOnUnexpectedCodepoints() {
    assertThrows(MimeTypeParseException.class, () -> MimeType.from("text/plain; a=a ; b;b"));
    assertThrows(MimeTypeParseException.class, () -> MimeType.from("text/plain; a=a , b=b"));
    assertThrows(MimeTypeParseException.class, () -> MimeType.from("text ^ plain; a=a ; b=b"));
  }

  @Test
  public void canParseAMimeList() throws MimeTypeParseException {
    List<MimeType> mimes = list(
                                new MimeType("text", "plain", list()),
                                new MimeType("text", "colored", list(new Parameter("lang", "es"))),
                                new MimeType("image", "png",
                                             list(new Parameter("size", "small"),
                                                  new Parameter("cute", "yes yes"))));
    assertEquals(mimes, MimeType.listFrom("text/plain, text/colored; lang=es, image/png; size=small; cute=\"yes yes\"", ','));
  }

  @Test
  public void gettersCanGet() {
    MimeType mime = new MimeType("text", "plain", list(new Parameter("charset", "utf8")));
    assertEquals("text", mime.getType());
    assertEquals("plain", mime.getSubtype());
    assertEquals(1, mime.getParameters().size());
    assertEquals("charset", mime.getParameters().get(0).getAttribute());
    assertEquals("utf8", mime.getParameters().get(0).getValue());
  }

  @Test
  public void parametersKnowWhenToEscapeThemselves() {
    Parameter param = new Parameter("attr", "a \"value\"");
    assertEquals("attr=\"a \\\"value\\\"\"", param.toString());
  }

  @Test
  public void parametersKnowWhenQuotesAreNeeded() {
    Parameter param = new Parameter("attr", "value");
    assertEquals("attr=value", param.toString());
  }

  @Test
  public void mimesHaveTheirCommonStringRepresentations() throws MimeTypeParseException {
    assertEquals("text/plain", MimeType.from("text/plain").toString());
    assertEquals("text/plain; param=value", MimeType.from("text/plain; param=value").toString());
    assertEquals("text/plain; param=value; anotherParam=anotherValue",
                 MimeType.from("text/plain; param=value; anotherParam=anotherValue").toString());
  }

  static <T> List<T> list(T... args) {
    return Arrays.asList(args);
  }
}
