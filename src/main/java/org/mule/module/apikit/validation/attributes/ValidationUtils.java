/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.attributes;

import org.mule.apikit.model.parameter.Parameter;

public class ValidationUtils {

  static String escapeAndSurroundWithQuotesIfNeeded(Parameter facet, String value) {
    return facet != null && (facet.isScalar() || (facet.isArray() && (!value.startsWith("{") && !value.startsWith("-"))))
        ? facet.surroundWithQuotesIfNeeded(value.replace("\"", "\\\""))
        : value;
  }

}
