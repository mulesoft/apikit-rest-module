/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.uri;

import org.mule.module.apikit.api.exception.ApikitRuntimeException;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * An encoder/decoder for use by URI templates.
 * <p/>
 * Only unreserved characters according to RFC 3986 do not need to be encoded within a variable:
 * <p/>
 * 
 * <pre>
 * unreserved = ALPHA / DIGIT / '-' / '.' / '_' / '&tilde;';
 * </pre>
 * <p/>
 * <p/>
 * This encoder/decoder should be designed so that URI which contain only unreserved characters are processed faster.
 *
 * @author Christophe Lauret
 * @version 11 June 2009
 * @see <a href="http://tools.ietf.org/html/rfc3986">RFC 3986 - Uniform Resource Identifier (URI): Generic Syntax<a/>
 * @see <a href="http://tools.ietf.org/html/rfc3986#appendix-A">RFC 3986 - Uniform Resource Identifier (URI): Generic Syntax -
 *      Appendix A. Collected ABNF for URI</a>
 * @see <a href="http://www.unicode.org/unicode/reports/tr15/tr15-23.html#Specification">UAX #15: Unicode Normalization</a>
 */
public class URICoder {

  /**
   * The UTF8 character set for reuse - Always defined.
   */
  private final static Charset UTF8 = Charset.forName("UTF-8");

  /**
   * The hexadecimal digits for use by the encoder.
   */
  private final static char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A',
      'B', 'C', 'D', 'E', 'F'};

  /**
   * extra capacity, reserved for escape chars
   */
  private final static int ENCODE_INITIAL_CAPACITY = 16;

  /**
   * Reserved chars, not considered when detecting bad encoding
   */
  private final static Set<Character> skipChars = new HashSet<>(Arrays.asList('/', '{', '}', '%', ':'));

  /**
   * Reserved chars, not considered when encoding request path
   */
  private static final Set<Character> escapeChars = new HashSet<>(Arrays.asList('/', '{', '}'));

  private URICoder() {}

  // Encoder
  // ==========================================================================

  /**
   * @param rawRequestPath request path with original encoding /api/myEndpoint
   * @return @rawRequestPath with encoded special characters if found
   * @throws ApikitRuntimeException when bad encoding detected
   */
  public static String encodeRequestPath(String rawRequestPath) throws ApikitRuntimeException {
    boolean encoded = !rawRequestPath.equals(decode(rawRequestPath));
    if (encoded && containsSpecialChars(rawRequestPath, skipChars)) {
      throw new ApikitRuntimeException("Request path contains special characters not encoded");
    }
    return encoded ? rawRequestPath : encode(rawRequestPath, escapeChars);
  }

  /**
   * Encodes the string as valid URI fragment.
   * <p/>
   * <p/>
   * This encoder will percent-encode all but <em>unreserved</em> characters.
   *
   * @param s The string to encode.
   * @param chars An ASCII set of characters that should not be encoded if found in the string.
   * @return The corresponding encoded string.
   */
  public static String encode(String s, Set<Character> chars) {
    if (s.length() == 0) {
      return s;
    }
    // Check whether we need to use UTF-8 encoder
    boolean ascii = isASCII(s);
    return ascii ? encode_ASCII(s, chars) : encode_UTF8(s, chars);
  }

  /**
   * @param path The path to check if contains special characters
   * @param skipChars An ASCII set of characters that should not be considered if found in the string.
   * @return If the path contains special characters not encoded
   */
  private static boolean containsSpecialChars(String path, Set<Character> skipChars) {
    if (path == null || path.length() == 0) {
      return false;
    }
    for (int index = 0; index < path.length(); index++) {
      char character = path.charAt(index);
      if (!skipChars.contains(character) && !isUnreserved((int) character)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Encodes a string containing only ASCII characters.
   *
   * @param s The string the encode (assuming ASCII characters only)
   * @param chars A set of characters that does not require encoding if found in the string.
   */
  private static String encode_ASCII(String s, Set<Character> chars) {
    StringBuilder sb = new StringBuilder(s.length() + ENCODE_INITIAL_CAPACITY);
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (isUnreserved((int) c) || chars.contains(c)) {
        sb.append(c);
      } else {
        appendEscape(sb, c);
      }
    }
    return sb.toString();
  }

  /**
   * Encodes a string containing non ASCII characters using an UTF-8 encoder.
   *
   * @param s The string the encode (assuming ASCII characters only)
   * @param chars A set of characters that does not require encoding if found in the string.
   */
  private static String encode_UTF8(String s, Set<Character> chars) {
    // TODO: Normalizer requires Java 6!
    String n = (Normalizer.isNormalized(s, Form.NFKC)) ? s : Normalizer.normalize(s, Form.NFKC);
    // convert String to UTF-8
    ByteBuffer bb = UTF8.encode(n);
    // URI encode
    StringBuffer sb = new StringBuffer();
    while (bb.hasRemaining()) {
      int b = bb.get() & 0xff;
      if (isUnreserved(b) || chars.contains((char) b)) {
        sb.append((char) b);
      } else {
        appendEscape(sb, (byte) b);
      }
    }
    return sb.toString();
  }

  // Decoder
  // ==========================================================================

  /**
   * Decode the string as valid URI fragment.
   *
   * @param s The string to decode.
   * @return The corresponding decoded string.
   */
  public static String decode(String s) {
    if (s.length() == 0 || (s.indexOf('%') < 0 && s.indexOf('+') < 0)) {
      return s;
    }
    // Check whether we need to convert to UTF-8 encoder
    boolean ascii = isEncodedASCII(s);
    return ascii ? decode_ASCII(s) : decode_UTF8(s);
  }

  /**
   * Decodes a string containing only ASCII characters.
   */
  private static String decode_ASCII(String s) {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '%') {
        if (i < s.length() - 2) {
          String hex = String.copyValueOf(new char[] {s.charAt(++i), s.charAt(++i)});
          char x = (char) Integer.parseInt(hex, 16);
          sb.append(x);
        }
        // TODO: handle error condition
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  /**
   * Decodes a string containing non ASCII characters using an UTF-8 decoder.
   */
  private static String decode_UTF8(String s) {
    // URI decode
    ByteBuffer bb = ByteBuffer.allocate(s.length());
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '%') {
        if (i < s.length() - 2) {
          String hex = "" + s.charAt(++i) + s.charAt(++i);
          byte b = (byte) (Integer.parseInt(hex, 16));
          bb.put(b);
        }
      } else {
        // TODO: could there be also non-ASCII characters that should have been encoded?
        bb.put((byte) c);
      }
    }
    bb.limit(bb.position());
    bb.position(0);
    return UTF8.decode(bb).toString();
  }

  /**
   * Appends the escape sequence for the given byte to the specified string buffer.
   *
   * @param sb The string buffer.
   * @param b The byte to escape.
   */
  private static void appendEscape(StringBuffer sb, byte b) {
    sb.append('%');
    sb.append(HEX_DIGITS[(b >> 4) & 0x0f]);
    sb.append(HEX_DIGITS[(b >> 0) & 0x0f]);
  }

  /**
   * Appends the escape sequence for the given byte to the specified string buffer.
   *
   * @param sb The string buffer.
   * @param c The byte to escape.
   */
  private static void appendEscape(StringBuilder sb, char c) {
    sb.append('%');
    sb.append(HEX_DIGITS[(c >> 4) & 0x0f]);
    sb.append(HEX_DIGITS[(c >> 0) & 0x0f]);
  }

  /**
   * Indicates whether the character is unreserved of not.
   *
   * @param c The character to test.
   * @return <code>true</code> if it is unreserved; <code>false</code> otherwise.
   */
  private static boolean isUnreserved(int c) {
    // ALPHA (lower)
    if (c >= 'a' && c <= 'z') {
      return true;
      // ALPHA (UPPER)
    } else if (c >= 'A' && c <= 'Z') {
      return true;
      // DIGIT
    } else if (c >= '0' && c <= '9') {
      return true;
    } else if (c == '.' || c == '_' || c == '-' || c == '~') {
      return true;
    }
    return false;
  }

  /**
   * Indicates whether the string contains non-ASCII characters.
   */
  private static boolean isASCII(String s) {
    for (int i = 0; i < s.length(); i++) {
      if (s.charAt(i) >= 0x80) {
        return false;
      }
    }
    return true;
  }

  /**
   * Indicates whether the encoded string contains non-ASCII characters.
   */
  private static boolean isEncodedASCII(String s) {
    for (int i = 0; i < s.length(); i++) {
      if (s.charAt(i) == '%' && i < s.length() - 1 && s.charAt(i + 1) > '7') {
        return false;
      }
    }
    return true;
  }

}
