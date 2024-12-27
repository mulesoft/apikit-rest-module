/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.uri;

import org.junit.jupiter.api.Test;
import org.mule.module.apikit.api.UrlUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class URICoderTest {

    private Set<Character> ESCAPE_CHARS = new HashSet<>(Arrays.asList('/', '{', '}'));

    @Test
    void testEncode() {
        String input = "https://example.com/path?query=value";
        String expectedOutput = "https%3A//example.com/path%3Fquery%3Dvalue";
        assertEquals(expectedOutput, URICoder.encode(input, ESCAPE_CHARS));
    }

    @Test
    void testDecode() {
        String input = "https%3A%2F%2Fexample.com%2Fpath%3Fquery%3Dvalue";
        String expectedOutput = "https://example.com/path?query=value";
        assertEquals(expectedOutput, URICoder.decode(input));
    }

    @Test
    void testEncodeNull() {
        assertEquals("", URICoder.encode("", ESCAPE_CHARS));
    }

    @Test
    void testDecodeNull() {
        assertEquals("", URICoder.decode(""));
    }

}