/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.uri;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class URICoderTestCase {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void encodeCorrectEncodedURI() {
    assertThat(URICoder.encodeRequestPath("api/uri-param/AA%2F11%2F000000%2A70/test"),
               equalTo("api/uri-param/AA%2F11%2F000000%2A70/test"));
  }

  @Test
  public void encodeNotEncodedRequestPath() {
    assertThat(URICoder.encodeRequestPath("api/uri-param/AA:11 000000 70/test"),
               equalTo("api/uri-param/AA%3A11%20000000%2070/test"));
  }

  @Test
  public void halfEncodingIsIncorrect() {
    expectedException.expectMessage("Request path contains special characters not encoded");
    URICoder.encodeRequestPath("api/uri-param/AA11%20*70/test");
  }

  @Test
  public void colonInEncodedIsCorrect() {
    assertThat(URICoder.encodeRequestPath("api/uri-param/AA:11%2070/test"),
               equalTo("api/uri-param/AA:11%2070/test"));
  }

  @Test
  public void encodedColonIsCorrect() {
    assertThat(URICoder.encodeRequestPath("api/uri-param/AA%3A11%2070/test"),
               equalTo("api/uri-param/AA%3A11%2070/test"));
  }

  @Test
  public void encodeRequestPathWithoutReservedChars() {
    assertThat(URICoder.encodeRequestPath("api/uri-param/AA00000070/test"), equalTo("api/uri-param/AA00000070/test"));
  }

}
