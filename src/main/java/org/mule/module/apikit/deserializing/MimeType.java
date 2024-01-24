/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.deserializing;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MimeType {

  private String type;
  private String subtype;
  private final List<Parameter> parameters;

  public static MimeType from(String mimeType) throws MimeTypeParseException {
    Parser parser = new Parser(mimeType);
    MimeType mime = parser.nextMimeType();
    parser.assertAtEnd();
    return mime;
  }

  public static List<MimeType> listFrom(String mimeTypeList, char listDelimiter) throws MimeTypeParseException {
    Parser parser = new Parser(mimeTypeList);
    List<MimeType> mimes = parser.nextMimeTypeList(listDelimiter);
    parser.assertAtEnd();
    return mimes;
  }

  private MimeType() {
    this.parameters = new ArrayList<>();
  }

  public MimeType(String type, String subtype, List<Parameter> parameters) {
    Objects.requireNonNull(type);
    Objects.requireNonNull(subtype);
    Objects.requireNonNull(parameters);
    this.type = type;
    this.subtype = subtype;
    this.parameters = parameters;
  }

  public String getType() {
    return type;
  }

  public String getSubtype() {
    return subtype;
  }

  public List<Parameter> getParameters() {
    return parameters;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    MimeType mimeType = (MimeType) o;
    return type.equals(mimeType.type) && subtype.equals(mimeType.subtype) && parameters.equals(mimeType.parameters);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, subtype);
  }

  @Override
  public String toString() {
    String parameterList = parameters.stream()
        .map(param -> "; " + param)
        .collect(Collectors.joining());
    return type + '/' + subtype + parameterList;
  }

  public static class Parameter {

    private final String attribute;
    private final String value;

    public Parameter(String attribute, String value) {
      Objects.requireNonNull(attribute);
      Objects.requireNonNull(value);
      this.attribute = attribute;
      this.value = value;
    }

    public String getAttribute() {
      return attribute;
    }

    public String getValue() {
      return value;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;
      Parameter parameter = (Parameter) o;
      return attribute.equals(parameter.attribute) && value.equals(parameter.value);
    }

    @Override
    public int hashCode() {
      return Objects.hash(attribute, value);
    }

    @Override
    public String toString() {
      if (value.chars().allMatch(c -> Parser.isTokenChar((char) c))) {
        return attribute + '=' + value;
      }
      return attribute + "=\"" + escape(value) + "\"";
    }

    private String escape(String value) {
      return value.chars().flatMap(c -> Parser.isQdtext((char) c) || Parser.isObsText((char) c)
          // This is so dumb, I feel ashamed
          ? IntStream.of(c)
          : ("\\" + (char) c).chars())
          .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
          .toString();
    }
  }

  public static class Parser {

    private int i;
    private final CharSequence input;

    public Parser(CharSequence input) {
      this.i = 0;
      this.input = input;
    }

    private List<MimeType> nextMimeTypeList(char listDelimiter) throws MimeTypeParseException {
      List<MimeType> mimes = new ArrayList<>();
      do {
        mimes.add(nextMimeType());
      } while (trySkip(listDelimiter));
      return mimes;
    }

    private MimeType nextMimeType() throws MimeTypeParseException {
      MimeType mime = new MimeType();

      skipWhitespace();
      mime.type = nextToken();
      skipWhitespace();
      boolean hasSubtype = trySkip('/');
      if (hasSubtype) {
        skipWhitespace();
        mime.subtype = nextToken();
      } else {
        // Java URLConnection sends an `Accept` header that's just an '*'.
        // We have decided to interpret any malformed mimes without
        // subtypes as `<type>/*`.
        mime.subtype = "*";
      }
      skipWhitespace();
      if (!atEnd() && trySkip(';')) {
        mime.parameters.addAll(nextParameterList());
      }
      skipWhitespace();

      return mime;
    }

    private Parameter nextParameter() throws MimeTypeParseException {
      skipWhitespace();
      String attribute = nextToken();
      skipWhitespace();
      skip('=');
      skipWhitespace();
      assertNotAtEnd();
      String value = peek() == '"'
          ? nextQuotedString()
          : nextToken();
      skipWhitespace();
      return new Parameter(attribute, value);
    }

    private List<Parameter> nextParameterList() throws MimeTypeParseException {
      List<Parameter> parameter = new ArrayList<>();
      do {
        parameter.add(nextParameter());
      } while (!atEnd() && trySkip(';'));
      return parameter;
    }

    private String nextToken() throws MimeTypeParseException {
      assertNotAtEnd();
      int start = i;
      while (!atEnd() && isTokenChar(peek())) {
        skip();
      }
      if (start == i) {
        throw new MimeTypeParseException("Expected token at column " + (i + 1) + " but got `" + peek() + "`");
      }
      return input.subSequence(start, i).toString();
    }

    private String nextQuotedString() throws MimeTypeParseException {
      skip('"');
      StringBuilder s = new StringBuilder();
      while (!atEnd() && isValidStringChar(input, i)) {
        if (peek() == '\\') {
          skip();
        }
        assertNotAtEnd();
        s.append(peek());
        skip();
      }
      skip('"');
      return s.toString();
    }

    private boolean trySkip(char expected) {
      if (atEnd() || peek() != expected) {
        return false;
      }
      skip();
      return true;
    }

    private void skip(char expected) throws MimeTypeParseException {
      assertNotAtEnd();
      char got = peek();
      if (got != expected) {
        throw new MimeTypeParseException("Expected " + expected + " at column " + (i + 1) + " but got `" + got + "`");
      }
      skip();
    }

    private void skipWhitespace() {
      while (!atEnd() && Character.isSpaceChar(peek())) {
        skip();
      }
    }

    private void skip() {
      i++;
    }

    private char peek() {
      return input.charAt(i);
    }

    private boolean atEnd() {
      return input.length() <= i;
    }

    private void assertAtEnd() throws MimeTypeParseException {
      if (!atEnd()) {
        throw new MimeTypeParseException("Trailing non-whitespace text at column " + (i + 1));
      }
    }

    private void assertNotAtEnd() throws MimeTypeParseException {
      if (atEnd()) {
        throw new MimeTypeParseException("Unexpected end of input");
      }
    }

    private static boolean isTokenChar(char c) {
      return c != ' ' && !isCtl(c) && !isTspecial(c);
    }

    private static boolean isCtl(char c) {
      return 0x00 <= c && c <= 0x1F || c == 0x7F;
    }

    private static boolean isTspecial(char c) {
      return c == '(' || c == ')' || c == '<' || c == '>' || c == '@' || c == ',' || c == ';' || c == ':'
          || c == '\\' || c == '\"' || c == '/' || c == '[' || c == ']' || c == '?' || c == '=';
    }

    // As defined by https://www.rfc-editor.org/rfc/rfc7230
    private static boolean isValidStringChar(CharSequence input, int i) {
      char c = input.charAt(i);
      return isQdtext(c) || isQuotedPair(input, i) || isObsText(c);
    }

    private static boolean isQdtext(char c) {
      return c == '\t' || c == ' ' || c == '!' || ('#' <= c && c <= '[') || (']' <= c && c <= '~');
    }

    private static boolean isQuotedPair(CharSequence input, int i) {
      if (input.charAt(i) == '\\' && i + 1 < input.length()) {
        char c = input.charAt(i + 1);
        return c == '\t' || c == ' ' || isVchar(c) || isObsText(c);
      }
      return false;
    }

    private static boolean isObsText(char c) {
      // The real definition should be `0x80 <= c && c <= 0xFF` but because CharSequences are UTF16-based we take
      // BMP codepoints and surrogates as part of obs-text.
      return 0x80 <= c;
    }

    private static boolean isVchar(char c) {
      return '!' < c && c < '~';
    }
  }

  public static class MimeTypeParseException extends Exception {

    private MimeTypeParseException(String message) {
      super(message);
    }
  }
}
