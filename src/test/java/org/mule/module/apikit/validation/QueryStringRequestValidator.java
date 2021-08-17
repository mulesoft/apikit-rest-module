/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mule.module.apikit.api.exception.InvalidQueryStringException;
import org.mule.module.apikit.api.exception.MuleRestException;
import org.mule.runtime.api.util.MultiMap;

public class QueryStringRequestValidator extends AbstractRequestValidatorTestCase {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void validQueryString() throws MuleRestException {
    MultiMap<String, String> queryParams = new MultiMap<>();
    queryParams.put("start", "2");
    queryParams.put("lat", "12");
    queryParams.put("long", "13");
    testRestRequestValidatorBuilder
        .withApiLocation("unit/query-string/api.raml")
        .withMethod("GET")
        .withRequestPath("/api/locations")
        .withRelativePath("/locations")
        .withQueryParams(queryParams)
        .withQueryString("start=2&lat=12&long=13")
        .build()
        .validateRequest();
  }

  @Test
  public void invalidQueryString() throws MuleRestException {
    expectedException.expect(InvalidQueryStringException.class);
    expectedException.expectMessage("Invalid value for query string");
    MultiMap<String, String> queryParams = new MultiMap<>();
    queryParams.put("start", "5");
    testRestRequestValidatorBuilder
        .withApiLocation("unit/query-string/api.raml")
        .withMethod("GET")
        .withRequestPath("/api/locations")
        .withRelativePath("/locations")
        .withQueryParams(queryParams)
        .withQueryString("start=5")
        .build()
        .validateRequest();
  }

  @Test
  public void validQueryStringWithInvalidYAMLValue() throws MuleRestException {
    MultiMap<String, String> queryParams = new MultiMap<>();
    queryParams.put("code", "*validC*de*");
    queryParams.put("codes", "validCode");
    queryParams.put("codes", "*validC*de*");
    testRestRequestValidatorBuilder
        .withApiLocation("unit/query-string/special-chars-api.raml")
        .withMethod("GET")
        .withRequestPath("/api/query-string-validation")
        .withRelativePath("/query-string-validation")
        .withQueryParams(queryParams)
        .withQueryString("code=*validC*de*&codes=validCode&codes=*validC*de*")
        .build()
        .validateRequest();
  }

  @Test
  public void invalidQueryStringExceedingLengthWithInvalidYAMLValue() throws MuleRestException {
    expectedException.expect(InvalidQueryStringException.class);
    expectedException.expectMessage("Invalid value for query string");
    MultiMap<String, String> queryParams = new MultiMap<>();
    queryParams.put("code", "*invalidC*deLength*");
    testRestRequestValidatorBuilder
        .withApiLocation("unit/query-string/special-chars-api.raml")
        .withMethod("GET")
        .withRequestPath("/api/query-string-validation")
        .withRelativePath("/query-string-validation")
        .withQueryParams(queryParams)
        .withQueryString("code=*invalidC*deLength*")
        .build()
        .validateRequest();
  }
}
