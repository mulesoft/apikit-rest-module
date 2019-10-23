/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.attributes;

import org.mule.runtime.api.util.MultiMap;

public class ValidatedQueryParams {

  private final MultiMap<String, String> queryParams;
  private final String queryString;

  public ValidatedQueryParams(MultiMap<String, String> queryParams, String queryString) {
    this.queryParams = queryParams;
    this.queryString = queryString;
  }

  public String getQueryString() {
    return queryString;
  }

  public MultiMap<String, String> getQueryParams() {
    return queryParams;
  }

}
