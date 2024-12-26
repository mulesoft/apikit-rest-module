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