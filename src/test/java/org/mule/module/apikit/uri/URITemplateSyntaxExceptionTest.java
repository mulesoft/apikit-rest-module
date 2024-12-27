/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.uri;

import org.junit.Test;
import static org.junit.Assert.*;

public class URITemplateSyntaxExceptionTest {

    @Test
    public void testConstructor() {
        String input = "testInput";
        String reason = "testReason";
        URITemplateSyntaxException exception = new URITemplateSyntaxException(input, reason);
        assertEquals("testReason : testInput", exception.getMessage());
        assertEquals(input, exception.getInput());
        assertEquals(reason, exception.getReason());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullInput() {
        String input = null;
        String reason = "testReason";
        URITemplateSyntaxException exception = new URITemplateSyntaxException(input, reason);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullReason() {
        String input = "testInput";
        String reason = null;
        URITemplateSyntaxException exception = new URITemplateSyntaxException(input, reason);
    }

}