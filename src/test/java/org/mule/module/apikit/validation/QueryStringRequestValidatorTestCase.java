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
import org.mule.parser.service.ParserMode;
import org.mule.runtime.api.util.MultiMap;

import java.util.Collections;

public class QueryStringRequestValidatorTestCase extends AbstractRequestValidatorTestCase {

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
  public void notValidAdditionalParamQueryString() {
    MultiMap<String, String> queryParams = new MultiMap<>();
    queryParams.put("start", "2");
    queryParams.put("lat", "12");
    queryParams.put("long", "13");
    queryParams.put("extraParam", "13");
    testRestRequestValidatorBuilder
        .withApiLocation("unit/query-string/api.raml")
        .withMethod("GET")
        .withRequestPath("/api/locations")
        .withRelativePath("/locations")
        .withQueryParams(queryParams)
        .withQueryString("start=2&lat=12&long=13&extraParam=13")
        .build()
        .assertThrows(InvalidQueryStringException.class, "Invalid value for query string");;
  }

  @Test
  public void validUnionQueryString() throws MuleRestException {
    if (parser == ParserMode.RAML) {
      return; //auto-quoting is not supported for RAML parser
    }
    MultiMap<String, String> queryParams = new MultiMap<>();
    queryParams.put("stringOrNumber", "abc");
    queryParams.put("nullableString", "123");
    queryParams.put("nonNullableString", "13");
    testRestRequestValidatorBuilder
        .withApiLocation("unit/query-string/union.raml")
        .withMethod("GET")
        .withRequestPath("/api/unionQueryString")
        .withRelativePath("/unionQueryString")
        .withQueryParams(queryParams)
        .build()
        .validateRequest();
  }

  @Test
  public void notValidUnionQueryString() {
    MultiMap<String, String> queryParams = new MultiMap<>();
    queryParams.put("stringOrNumber", Collections.singleton(null));
    queryParams.put("nullableString", Collections.singleton(null));
    queryParams.put("nonNullableString", "123");
    testRestRequestValidatorBuilder
        .withApiLocation("unit/query-string/union.raml")
        .withMethod("GET")
        .withRequestPath("/api/unionQueryString")
        .withRelativePath("/unionQueryString")
        .withQueryParams(queryParams)
        .withQueryString("stringOrNumber&nullableString&nonNullableString=123")
        .build()
        .assertThrows(InvalidQueryStringException.class, "Invalid value for query string");
  }

  @Test
  public void invalidQueryString() {
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
        .assertThrows(InvalidQueryStringException.class, "Invalid value for query string");;
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
  public void invalidQueryStringExceedingLengthWithInvalidYAMLValue() {
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
        .assertThrows(InvalidQueryStringException.class, "Invalid value for query string");;
  }
}
