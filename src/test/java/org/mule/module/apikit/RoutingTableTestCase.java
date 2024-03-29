/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.mule.module.apikit.api.RamlHandler;
import org.mule.module.apikit.api.RoutingTable;
import org.mule.module.apikit.api.uri.URIPattern;
import org.mule.module.apikit.api.uri.URIResolver;
import org.mule.module.apikit.uri.URIResolveResult;
import org.mule.runtime.core.api.MuleContext;

public class RoutingTableTestCase {

  private static RamlHandler ramlHandler;
  private static MuleContext muleContext;

  @BeforeClass
  public static void beforeAll() throws IOException {
    muleContext = Mockito.mock(MuleContext.class);
    when(muleContext.getExecutionClassLoader()).thenReturn(Thread.currentThread().getContextClassLoader());
    ramlHandler = new RamlHandler("unit/routing-table-sample.raml", true, muleContext.getErrorTypeRepository());
  }

  public RoutingTableTestCase() {}

  @Test
  public void testResourceFlattenedTree() {
    RoutingTable routingTable = new RoutingTable(ramlHandler.getApi());

    Assert.assertThat(routingTable.keySet(), hasItems(new URIPattern("/single-resource"),
                                                      new URIPattern("/api"),
                                                      new URIPattern("/api/sub-resource"),
                                                      new URIPattern("/api/sub-resource-types")));
  }

  @Test
  public void emptyParametersAreMatchedButNotResolved() {
    URIPattern pattern = new URIPattern("/api/{parameter}/list");
    Assert.assertTrue(pattern.match("/api//list"));
    URIResolver resolver = new URIResolver("/api//list");
    Assert.assertEquals(URIResolveResult.Status.ERROR, resolver.resolve(pattern).getStatus());
  }

  @Test
  public void getResourceByPattern() {
    RoutingTable routingTable = new RoutingTable(ramlHandler.getApi());

    Assert.assertNotNull(routingTable.getResource(new URIPattern("/single-resource")));
    Assert.assertNotNull(routingTable.getResource(new URIPattern("/api/sub-resource")));
  }

  @Test
  public void getResourceByString() {
    RoutingTable routingTable = new RoutingTable(ramlHandler.getApi());

    Assert.assertNotNull(routingTable.getResource("/single-resource"));
    Assert.assertNotNull(routingTable.getResource("/api/sub-resource"));
  }
}
