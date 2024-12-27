/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.uri;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class URITemplateTest {


    @Test
    public void testConstructor() {
        // Test with a valid template
        try {
            new URITemplate("/users/{userId}");
        } catch (Exception e) {
            fail("Failed to create a valid URI template");
        }

        // Test with a null template
        try {
            new URITemplate(null);
            fail("Expected a NullPointerException for a null template");
        } catch (NullPointerException e) {
            // Expected exception
        }
    }

    @Test
    public void testDigest() {
        // Test with a valid template
        try {
            List<Token> tokens = URITemplate.digest("/users/{userId}");
            assertNotNull("Token list should not be null", tokens);
            assertEquals("Token list should have 2 elements", 2, tokens.size());
            assertEquals("First token should be a literal token", "/users/", tokens.get(0).toString());
            assertEquals("Second token should be a variable token", "{userId}", tokens.get(1).toString());
        } catch (URITemplateSyntaxException e) {
            fail("Failed to digest a valid URI template");
        }
    }

    @Test
    public void testEquals() {
        URITemplate template1 = new URITemplate("/users/{userId}");
        URITemplate template2 = new URITemplate("/users/{userId}");
        URITemplate template3 = new URITemplate("/users/{username}");

        assertEquals("Template 1 should equal itself", template1, template1);
        assertEquals("Template 1 should equal template 2", template1, template2);
        assertNotEquals("Template 1 should not equal template 3", template1, template3);
        assertNotEquals("Template 1 should not equal null", null, template1);
    }

    @Test
    public void testHashCode() {
        URITemplate template = new URITemplate("/users/{userId}");
        assertEquals("Hash code should match", 127 * "/users/{userId}".hashCode() + "/users/{userId}".hashCode(), template.hashCode());
    }

    @Test
    public void testToString() {
        URITemplate template = new URITemplate("/users/{userId}");
        assertEquals("String representation should match", "/users/{userId}", template.toString());
    }

}