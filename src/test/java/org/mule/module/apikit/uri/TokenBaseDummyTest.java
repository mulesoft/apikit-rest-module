/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.uri;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenBaseDummyTest {

    @Test
    void testCreateTokenBaseFailure() {
        Assertions.assertThrows(NullPointerException.class, () -> new TokenBaseDummy(null));
        TokenBaseDummy tokenBaseDummyA = new TokenBaseDummy("testA");
        TokenBaseDummy tokenBaseDummyB = new TokenBaseDummy("testB");
        assertFalse(tokenBaseDummyA.isResolvable());
        assertNotEquals(tokenBaseDummyA, tokenBaseDummyB);
        TokenBaseDummy.forTest("{test}");
    }
}