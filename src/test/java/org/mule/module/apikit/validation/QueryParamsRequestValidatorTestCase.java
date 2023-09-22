/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation;

import org.junit.Test;
import org.mule.module.apikit.api.exception.MuleRestException;
import org.mule.runtime.api.util.MultiMap;

public class QueryParamsRequestValidatorTestCase extends AbstractRequestValidatorTestCase {

  @Test
  public void queryParamAnyType() throws MuleRestException {
    MultiMap<String, String> queryParams = new MultiMap<>();
    queryParams.put("anytype", "Any value");
    testRestRequestValidatorBuilder
        .withApiLocation("unit/query-parameters/query-parameters.raml")
        .withMethod("GET")
        .withRelativePath("/any-value")
        .withQueryParams(queryParams)
        .build()
        .validateRequest();
  }
}
