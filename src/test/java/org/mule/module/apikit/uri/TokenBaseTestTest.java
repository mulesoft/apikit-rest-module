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