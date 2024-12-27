/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.uri;

import org.junit.Test;
import static org.junit.Assert.*;
import org.mule.module.apikit.uri.VariableType;

public class VariableTypeTest {

    @Test
    public void testVariableTypeConstructor() {
        String name = "testName";
        VariableType variableType = new VariableType(name);
        assertEquals(name, variableType.getName());
    }

    @Test
    public void testGetName() {
        String name = "testName";
        VariableType variableType = new VariableType(name);
        assertEquals(name, variableType.getName());
    }

    @Test
    public void testEquals() {
        VariableType variableType1 = new VariableType("testName");
        VariableType variableType2 = new VariableType("testName");
        assertEquals(variableType1, variableType2);
    }

}