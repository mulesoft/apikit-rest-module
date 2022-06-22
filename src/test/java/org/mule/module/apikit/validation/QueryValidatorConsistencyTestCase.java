/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation;

import org.junit.Test;
import org.mule.module.apikit.api.exception.MuleRestException;
import org.mule.parser.service.ParserMode;
import org.mule.runtime.api.util.MultiMap;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertThrows;

public class QueryValidatorConsistencyTestCase extends AbstractRequestValidatorTestCase {

  private static final String STRING_ITEM_PARAM = "stringItemParam";
  private static final String NUMERIC_ITEM_PARAM = "numericItemParam";
  private static final String INTEGER_ITEM_PARAM = "integerItemParam";
  private static final String BOOLEAN_ITEM_PARAM = "booleanItemParam";
  private static final String DATETIME_ITEM_PARAM = "datetimeItemParam";
  private static final String UNION_ITEM_PARAM = "unionItemParam";
  private static final String OBJECT_ITEM_PARAM = "objectItemParam";
  private static final String STRING_ITEM_PARAMS = "stringItemParams";
  private static final String NUMERIC_ITEM_PARAMS = "numericItemParams";
  private static final String INTEGER_ITEM_PARAMS = "integerItemParams";
  private static final String BOOLEAN_ITEM_PARAMS = "booleanItemParams";
  private static final String DATETIME_ITEM_PARAMS = "datetimeItemParams";
  private static final String OBJECT_ITEM_PARAMS = "objectItemParams";
  private static final String UNION_ARRAYS_PARAMS = "unionOfArraysParams";
  private static final String NON_NULLABLE_UNION_OF_ARRAYS = "nonNullableUnionOfArraysParams";

  @Test
  public void validateString() throws MuleRestException {
    assertConsistencyOnSuccess(STRING_ITEM_PARAM, asList("ABC"));
    assertConsistencyOnSuccess(STRING_ITEM_PARAM, asList("123"));
    assertConsistencyOnSuccess(STRING_ITEM_PARAM, asList("\"123\""));
    assertConsistencyOnSuccess(STRING_ITEM_PARAM, asList("A\"B\"C"));
    assertConsistencyOnSuccess(STRING_ITEM_PARAM, asList("\"ABC\""));
    assertConsistencyOnFail(STRING_ITEM_PARAM, asList("exceedsMaxLength"));
    assertConsistencyOnFail(STRING_ITEM_PARAM, asList("\"1234\""));
  }

  @Test
  public void validateNumber() throws MuleRestException {
    assertConsistencyOnSuccess(NUMERIC_ITEM_PARAM, asList("123.12"));
    assertConsistencyOnSuccess(NUMERIC_ITEM_PARAM, asList("123"));
    assertConsistencyOnSuccess(NUMERIC_ITEM_PARAM, asList("00000123"));
    assertConsistencyOnFail(NUMERIC_ITEM_PARAM, asList("\"123\""));
  }

  @Test
  public void validateInteger() throws MuleRestException {
    assertConsistencyOnSuccess(INTEGER_ITEM_PARAM, asList("123"));
    assertConsistencyOnFail(INTEGER_ITEM_PARAM, asList("12.34"));
  }

  @Test
  public void validateBoolean() throws MuleRestException {
    assertConsistencyOnSuccess(BOOLEAN_ITEM_PARAM, asList("false"));
    assertConsistencyOnSuccess(BOOLEAN_ITEM_PARAM, asList("True"));
    assertConsistencyOnFail(BOOLEAN_ITEM_PARAM, asList("ABC"));
  }

  @Test
  public void validateDatetime() throws MuleRestException {
    assertConsistencyOnSuccess(DATETIME_ITEM_PARAM, asList("2016-02-28T16:41:41.090Z"));
    assertConsistencyOnFail(DATETIME_ITEM_PARAM, asList("12016-02-28T16:41:41.090Z"));
  }

  @Test
  public void validateUnion() throws MuleRestException {
    assertConsistencyOnSuccess(UNION_ITEM_PARAM, asList("ABC"));
    assertConsistencyOnSuccess(UNION_ITEM_PARAM, asList("123"));
    assertConsistencyOnSuccess(UNION_ITEM_PARAM, asList("{\"someField\": \"someValue\"}"));
    assertConsistencyOnSuccess(UNION_ITEM_PARAM, asList("\n- \"firstValue\"\n- \"secondValue\"\n"));
  }

  @Test
  public void validateObject() throws MuleRestException {
    assertConsistencyOnSuccess(OBJECT_ITEM_PARAM,
                               asList("{\"stringProp\":\"test\",\"numberProp\":0.10,\"integerProp\":3,\"booleanProp\":false,\"datetimeProp\":\"2016-02-28T16:41:41.090Z\",\"stringProps\":[\"A\",\"B\"],\"numberProps\":[0,1.26],\"integerProps\": [0, 1, 2],\"booleanProps\":[false,true,true],\"datetimeProps\":[\"2016-02-28T16:41:41.090Z\",\"2016-02-28T16:41:41.090Z\"]}"));
    assertConsistencyOnFail(OBJECT_ITEM_PARAM, asList("{\"integerProp\":3.4}"));
  }

  @Test
  public void validateStingArray() throws MuleRestException {
    assertConsistencyOnSuccess(STRING_ITEM_PARAMS, asList("ABC", "DEF"));
    assertConsistencyOnSuccess(STRING_ITEM_PARAMS, asList("123", "456"));
    assertConsistencyOnSuccess(STRING_ITEM_PARAMS, asList("A\"B\"C", "D\"E\"F"));
    assertConsistencyOnSuccess(STRING_ITEM_PARAMS, asList("\"ABC\"", "\"DEF\""));
    assertConsistencyOnFail(STRING_ITEM_PARAMS, asList("ABC", "exceedsMaxLength"));

  }

  @Test
  public void validateNumberArray() throws MuleRestException {
    assertConsistencyOnSuccess(NUMERIC_ITEM_PARAMS, asList("123.34", "456.67"));
    assertConsistencyOnSuccess(NUMERIC_ITEM_PARAMS, asList("123", "456"));
    assertConsistencyOnFail(NUMERIC_ITEM_PARAMS, asList("123", "ABC"));
  }

  @Test
  public void validateIntegerArray() throws MuleRestException {
    assertConsistencyOnSuccess(INTEGER_ITEM_PARAMS, asList("123", "456"));
    assertConsistencyOnFail(INTEGER_ITEM_PARAMS, asList("123", "45.67"));
  }

  @Test
  public void validateBooleanArray() throws MuleRestException {
    assertConsistencyOnSuccess(BOOLEAN_ITEM_PARAMS, asList("false", "True"));
    assertConsistencyOnFail(BOOLEAN_ITEM_PARAMS, asList("false", "ABC"));
  }

  @Test
  public void validateDatetimeArray() throws MuleRestException {
    assertConsistencyOnSuccess(DATETIME_ITEM_PARAMS, asList("2016-02-28T16:41:41.090Z", "2018-08-25T16:41:41.090Z"));
    assertConsistencyOnFail(DATETIME_ITEM_PARAMS, asList("12016-02-28T16:41:41.090Z", "2018-08-25T16:41:41.090Z"));
  }

  @Test
  public void validateObjectArray() throws MuleRestException {
    assertConsistencyOnSuccess(OBJECT_ITEM_PARAMS,
                               asList("{\"stringProp\":\"test\",\"numberProp\":0.10,\"integerProp\":3,\"booleanProp\":false,\"datetimeProp\":\"2016-02-28T16:41:41.090Z\",\"stringProps\":[\"A\",\"B\"],\"numberProps\":[0,1.26],\"integerProps\": [0, 1, 2],\"booleanProps\":[false,true,true],\"datetimeProps\":[\"2016-02-28T16:41:41.090Z\",\"2016-02-28T16:41:41.090Z\"]}",
                                      "{\"stringProp\":\"test2\",\"numberProp\":1.23,\"integerProp\":4,\"booleanProp\":True,\"datetimeProp\":\"2016-02-28T16:41:41.090Z\",\"stringProps\":[\"C\",\"D\"],\"numberProps\":[56,242.66],\"integerProps\":[3,4,5],\"booleanProps\":[true,false,true],\"datetimeProps\":[\"2016-02-28T16:41:41.090Z\",\"2016-02-28T16:41:41.090Z\"]}"));
    assertConsistencyOnFail(OBJECT_ITEM_PARAMS, asList("{\"integerProp\":3.4}", "{\"stringProp\":\"test2\"}"));
  }

  @Test
  public void validateUnionArraysParams() throws MuleRestException {
    if (parser.equals(ParserMode.AMF)) {// RAML Parser query string union of arrays validation is not supported
      assertConsistencyOnSuccess(UNION_ARRAYS_PARAMS, asList("123", "456"));
      assertConsistencyOnSuccess(UNION_ARRAYS_PARAMS, asList("true", "false"));
      assertConsistencyOnFail(UNION_ARRAYS_PARAMS, asList("true", "123"));
      assertConsistencyOnSuccess(UNION_ARRAYS_PARAMS, null);
      assertConsistencyOnSuccess(UNION_ARRAYS_PARAMS, singletonList(null));
      assertConsistencyOnSuccess(UNION_ARRAYS_PARAMS, singletonList("null"));
    }
  }

  @Test
  public void validateNonNullableUnionArraysParams() throws MuleRestException {
    if (parser.equals(ParserMode.AMF)) {// RAML Parser query string union of arrays validation is not supported
      assertConsistencyOnSuccess(NON_NULLABLE_UNION_OF_ARRAYS, asList("123", "456"));
      assertConsistencyOnSuccess(NON_NULLABLE_UNION_OF_ARRAYS, asList("true", "false"));
      assertConsistencyOnFail(NON_NULLABLE_UNION_OF_ARRAYS, asList("true", "123"));
      assertConsistencyOnFail(NON_NULLABLE_UNION_OF_ARRAYS, null);
      assertConsistencyOnFail(NON_NULLABLE_UNION_OF_ARRAYS, singletonList(null));
      assertConsistencyOnFail(NON_NULLABLE_UNION_OF_ARRAYS, asList("null"));
    }
  }

  @Test
  public void validateEmpty() throws MuleRestException {
    MultiMap<String, String> queryParams = MultiMap.emptyMultiMap();
    String queryString = "";
    validateRequest(queryParams, queryString, "/testQueryString");
    validateRequest(queryParams, queryString, "/testQueryParams");
  }

  @Test
  public void invalidDefaultValueInSpecIsNotBeingValidatedForQueryString() throws MuleRestException {
    MultiMap<String, String> queryParams = MultiMap.emptyMultiMap();
    String queryString = "";
    testRestRequestValidatorBuilder
        .withApiLocation("unit/query-consistency/invalid-default.raml")
        .withMethod("GET")
        .withRequestPath("/api/testQueryString")
        .withRelativePath("/testQueryString")
        .withQueryParams(queryParams)
        .withQueryString(queryString)
        .build()
        .validateRequest();
  }

  @Test
  public void invalidDefaultValueInSpecIsNotBeingValidatedForQueryParams() throws MuleRestException {
    MultiMap<String, String> queryParams = MultiMap.emptyMultiMap();
    String queryString = "";
    testRestRequestValidatorBuilder
        .withApiLocation("unit/query-consistency/invalid-default.raml")
        .withMethod("GET")
        .withRequestPath("/api/testQueryParams")
        .withRelativePath("/testQueryParams")
        .withQueryParams(queryParams)
        .withQueryString(queryString)
        .build()
        .validateRequest();
  }


  private void assertConsistencyOnSuccess(String queryName, List<String> queryValues) throws MuleRestException {
    MultiMap<String, String> queryParams = getQueryParams(queryName, queryValues);
    String queryString = getQueryString(queryName, queryValues);
    validateRequest(queryParams, queryString, "/testQueryString");
    validateRequest(queryParams, "", "/testQueryParams");
  }

  private void assertConsistencyOnFail(String queryName, List<String> queryValues) {
    MultiMap<String, String> queryParams = getQueryParams(queryName, queryValues);
    String queryString = getQueryString(queryName, queryValues);
    assertThrows(MuleRestException.class, () -> validateRequest(queryParams, queryString, "/testQueryString"));
    assertThrows(MuleRestException.class, () -> validateRequest(queryParams, "", "/testQueryParams"));
  }

  private MultiMap<String, String> getQueryParams(String queryName, List<String> queryValues) {
    MultiMap<String, String> queryParams = new MultiMap<>();
    if (queryValues == null || isNull(queryValues)) {
      queryParams.put(queryName, (String) null);
      return queryParams;
    }
    queryValues.forEach(v -> queryParams.put(queryName, v));
    return queryParams;
  }

  private String getQueryString(String queryName, List<String> queryValues) {
    if (queryValues == null || isNull(queryValues)) {
      return queryName;
    }
    String queryString = queryName + "=" + queryValues.stream().map(v -> {
      try {
        return URLEncoder.encode(v, StandardCharsets.UTF_8.name());
      } catch (UnsupportedEncodingException e) {
        // do nothing
      }
      return v;
    }).collect(joining("&" + queryName + "="));
    return queryString;
  }

  private void validateRequest(MultiMap<String, String> queryParams, String queryString, String relativePath)
      throws MuleRestException {
    testRestRequestValidatorBuilder
        .withApiLocation("unit/query-consistency/api.raml")
        .withMethod("GET")
        .withRequestPath("/api" + relativePath)
        .withRelativePath(relativePath)
        .withQueryParams(queryParams)
        .withQueryString(queryString)
        .build()
        .validateRequest();
  }

  private static boolean isNull(Collection<?> paramValues) {
    return paramValues == null ||
        paramValues.size() == 1 && paramValues.stream().allMatch(value -> value == null || "null".equals(value));
  }
}
