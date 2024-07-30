/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation;

import org.junit.Test;
import org.mule.module.apikit.api.RoutingTable;
import org.mule.module.apikit.api.exception.MethodNotAllowedException;
import org.mule.module.apikit.api.exception.MuleRestException;
import org.mule.module.apikit.api.uri.URIPattern;

import static org.junit.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MethodNotAllowedTestCase extends AbstractRequestValidatorTestCase {

  @Test
  public void throwMethodNotAllowedExceptionTest() throws MuleRestException {
    expectedException.expect(MethodNotAllowedException.class);
    expectedException.expectMessage(equalTo("HTTP Method post not allowed for : /path/{version}/job/{jobId}"));
    testRestRequestValidatorBuilder
        .withApiLocation("unit/validation/api-with-version.raml")
        .withRelativePath("/path/1.0/job/123")
        .withMethod("POST")
        .build()
        .validateRequest();
  }


  @Test
  public void validRequestTest() throws Exception {
    testRestRequestValidatorBuilder
        .withApiLocation("unit/validation/api-resources.raml")
        .withRelativePath("/test/something")
        .withMethod("GET")
        .build()
        .validateRequest();

    testRestRequestValidatorBuilder
        .withApiLocation("unit/validation/api-resources.raml")
        .withRelativePath("/test/something/else")
        .withMethod("PUT")
        .build()
        .validateRequest();
  }

  @Test
  public void validRoutingTableTest() {
    RoutingTable routingTable = testRestRequestValidatorBuilder
        .withApiLocation("unit/validation/api-resources.raml")
        .withRelativePath("/test/something")
        .withMethod("GET")
        .build()
        .getRoutingTable();

    assertNotNull(routingTable);
    assertEquals(2, routingTable.keySet().size());
    assertTrue(routingTable.keySet().contains(new URIPattern("/test/{resourceZ}")));
  }
}
