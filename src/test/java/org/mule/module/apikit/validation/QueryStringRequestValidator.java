/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation;

import org.junit.Test;
import org.mule.module.apikit.api.exception.InvalidQueryStringException;
import org.mule.module.apikit.api.exception.MuleRestException;
import org.mule.runtime.api.util.MultiMap;

import static java.util.Collections.singleton;

public class QueryStringRequestValidator extends AbstractRequestValidatorTestCase {

  @Test
  public void validQueryString() throws MuleRestException {
    MultiMap<String, String> queryParams = new MultiMap<>();
    queryParams.put("start", "2");
    queryParams.put("lat", "12");
    queryParams.put("long", "13");
    testRestRequestValidatorBuilder
        .withApiLocation("unit/query-string/api.raml")
        .withMethod("GET")
        .withRelativePath("/locations")
        .withQueryParams(queryParams)
        .withQueryString("start=2&lat=12&long=13")
        .build()
        .validateRequest();
  }

  @Test
  public void invalidQueryString() {
    MultiMap<String, String> queryParams = new MultiMap<>();
    queryParams.put("start", "5");
    testRestRequestValidatorBuilder
        .withApiLocation("unit/query-string/api.raml")
        .withMethod("GET")
        .withRelativePath("/locations")
        .withQueryParams(queryParams)
        .withQueryString("start=5")
        .build()
        .assertThrows(InvalidQueryStringException.class, "Invalid value for query string");
  }

  @Test
  public void notNullableParameterQueryString() {
    MultiMap<String, String> queryParams = new MultiMap<>();
    queryParams.put("start", singleton(null));
    testRestRequestValidatorBuilder
        .withApiLocation("unit/query-string/api.raml")
        .withMethod("GET")
        .withRelativePath("/locations")
        .withQueryParams(queryParams)
        .withQueryString("start")
        .build()
        .assertThrows(InvalidQueryStringException.class, "Invalid value for query string");
  }

  @Test
  public void nullableParameterQueryString() throws MuleRestException {
    MultiMap<String, String> queryParams = new MultiMap<>();
    queryParams.put("nullableString", singleton(null));
    testRestRequestValidatorBuilder
        .withApiLocation("unit/query-string/api.raml")
        .withMethod("GET")
        .withRelativePath("/unionQueryString")
        .withQueryParams(queryParams)
        .withQueryString("nullableString")
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
        .withRelativePath("/query-string-validation")
        .withQueryParams(queryParams)
        .withQueryString("code=*validC*de*&codes=validCode&codes=*validC*de*")
        .build()
        .validateRequest();
  }

  @Test
  public void validQueryStringWithInvalidUnionYAMLValue() throws MuleRestException {
    MultiMap<String, String> queryParams = new MultiMap<>();
    queryParams.put("code", "*validC*de*");
    queryParams.put("unioncode", "*validC*de*");
    testRestRequestValidatorBuilder
        .withApiLocation("unit/query-string/special-chars-api.raml")
        .withMethod("GET")
        .withRelativePath("/query-string-validation")
        .withQueryParams(queryParams)
        .withQueryString("code=*validC*de*&unioncode=*validC*de*")
        .build()
        .validateRequest();
  }

  @Test
  public void invalidQueryStringExceedingLengthWithInvalidYAMLValue() {
    MultiMap<String, String> queryParams = new MultiMap<>();
    queryParams.put("code", "*invalidC*deLength*");
    testRestRequestValidatorBuilder
        .withApiLocation("unit/query-string/special-chars-api.raml")
        .withMethod("GET")
        .withRelativePath("/query-string-validation")
        .withQueryParams(queryParams)
        .withQueryString("code=*invalidC*deLength*")
        .build()
        .assertThrows(InvalidQueryStringException.class, "Invalid value for query string");
  }


}
