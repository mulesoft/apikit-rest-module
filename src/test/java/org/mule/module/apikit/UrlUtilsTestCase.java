/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import org.junit.Test;
import org.mule.module.apikit.api.UrlUtils;

public class UrlUtilsTestCase {

  @Test
  public void getRelativePath() {
    assertThat(UrlUtils.getRelativePath("/query-params/*", "/query-params/constrains"), equalTo("/constrains"));
    assertThat(UrlUtils.getRelativePath("/validation/scalar/types/*", "/validation/scalar/types/resource"), equalTo("/resource"));
    assertThat(UrlUtils.getRelativePath("/console/*", "/console/"), equalTo("/"));
    assertThat(UrlUtils.getRelativePath("/console/*", "/console/munit/console/example.json"),
               equalTo("/munit/console/example.json"));
    assertThat(UrlUtils.getRelativePath("/console/*", "/console"), equalTo("/"));
    assertThat(UrlUtils.getRelativePath("api", "/api"), equalTo("/"));
  }

}
