/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.uri;

import java.util.Map;

class TokenBaseDummy extends TokenBase{

    /**
     * Creates a new expansion token.
     *
     * @param exp The expression corresponding to this URI token.
     * @throws NullPointerException If the specified expression is <code>null</code>.
     */
    public TokenBaseDummy(String exp) throws NullPointerException {
        super(exp);
    }

    @Override
    public boolean resolve(String expanded, Map<Variable, Object> values) {
        return false;
    }

    public static String forTest(String exp) {
        return TokenBase.strip(exp);
    }
}