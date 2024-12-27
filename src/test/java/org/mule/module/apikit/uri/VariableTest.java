/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.uri;

import org.junit.Test;
import static org.junit.Assert.*;

public class VariableTest {


        @Test
        public void testVariable() {
            Variable variable = new Variable("foo", "bar");
            assertEquals("foo", variable.name());
            assertEquals("bar", variable.defaultValue());
            assertNull(variable.type());

            variable = new Variable(Variable.Reserved.WILDCARD);
            assertEquals("*", variable.name());
            assertEquals("", variable.defaultValue());
            assertNull(variable.type());
        }

        @Test
        public void testForm() {
            assertEquals(Variable.Form.STRING, Variable.Form.getType(""));
            assertEquals(Variable.Form.LIST, Variable.Form.getType("@"));
            assertEquals(Variable.Form.MAP, Variable.Form.getType("%"));
        }

        @Test
        public void testIsValidName() {
            assertTrue(Variable.isValidName("foo"));
            assertTrue(Variable.isValidName("foo.bar"));
            assertTrue(Variable.isValidName("foo_1"));
            assertTrue(Variable.isValidName("foo-bar"));
            assertFalse(Variable.isValidName("foo*"));
            assertFalse(Variable.isValidName(""));
            assertFalse(Variable.isValidName(null));
        }

        @Test
        public void testIsValidValue() {
            assertTrue(Variable.isValidValue("foo"));
            assertTrue(Variable.isValidValue("foo.bar"));
            assertTrue(Variable.isValidValue("foo_1"));
            assertTrue(Variable.isValidValue("foo-bar"));
            assertTrue(Variable.isValidValue("~%"));
            assertFalse(Variable.isValidValue("foo*"));
            //assertFalse(Variable.isValidValue(""));
            assertFalse(Variable.isValidValue(null));
        }


}

