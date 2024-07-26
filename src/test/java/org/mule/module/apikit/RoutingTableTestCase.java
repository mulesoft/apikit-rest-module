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
import java.util.HashSet;

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
  public void URIWithTrailingForwardSlashAreMatchedAndResolvedCorrectly() {
    URIPattern pattern1 = new URIPattern("/api/hello/");
    URIPattern pattern2 = new URIPattern("/");
    URIPattern pattern3 = new URIPattern("/{param}");
    URIPattern pattern4 = new URIPattern("/api/hello/{param}");
    URIPattern pattern5 = new URIPattern("/api/hello/{param}/all");
    URIPattern pattern6 = new URIPattern("/api/hello");
    URIPattern pattern7 = new URIPattern("/{param}/");
    HashSet<URIPattern> patterns = new HashSet<>();
    patterns.add(pattern1);
    patterns.add(pattern2);
    patterns.add(pattern3);
    patterns.add(pattern4);
    patterns.add(pattern5);
    patterns.add(pattern6);
    patterns.add(pattern7);

    URIResolver resolver1 = new URIResolver("/api/hello/");
    URIPattern bestPattern1 = resolver1.find(patterns, URIResolver.MatchRule.BEST_MATCH);
    Assert.assertEquals(URIResolveResult.Status.RESOLVED, resolver1.resolve(bestPattern1).getStatus());
    Assert.assertEquals(pattern1, bestPattern1);

    URIResolver resolver2 = new URIResolver("/");
    URIPattern bestPattern2 = resolver2.find(patterns, URIResolver.MatchRule.BEST_MATCH);
    Assert.assertEquals(URIResolveResult.Status.RESOLVED, resolver2.resolve(bestPattern2).getStatus());
    Assert.assertEquals(pattern2, bestPattern2);

    URIResolver resolver3 = new URIResolver("/api");
    URIPattern bestPattern3 = resolver3.find(patterns, URIResolver.MatchRule.BEST_MATCH);
    Assert.assertEquals(URIResolveResult.Status.RESOLVED, resolver3.resolve(bestPattern3).getStatus());
    Assert.assertEquals(pattern3, bestPattern3);

    URIResolver resolver7 = new URIResolver("/api/");
    URIPattern bestPattern7 = resolver7.find(patterns, URIResolver.MatchRule.BEST_MATCH);
    Assert.assertEquals(URIResolveResult.Status.RESOLVED, resolver7.resolve(bestPattern7).getStatus());
    Assert.assertEquals(pattern7, bestPattern7);
  }

  @Test
  public void URIWithTrailingForwardSlashAreNotMatched() {
    HashSet<URIPattern> patterns = new HashSet<>();
    patterns.add(new URIPattern("/api/hello/{param}"));
    patterns.add(new URIPattern("/api/hello/{param}/all"));
    patterns.add(new URIPattern("/api/hello"));

    URIResolver resolver1 = new URIResolver("/api/hello/");
    Assert.assertNull(resolver1.find(patterns, URIResolver.MatchRule.BEST_MATCH));

    URIResolver resolver3 = new URIResolver("/api");
    Assert.assertNull(resolver3.find(patterns, URIResolver.MatchRule.BEST_MATCH));

    patterns.add(new URIPattern("/{param}"));
    URIResolver resolver2 = new URIResolver("/");
    Assert.assertNull(resolver2.find(patterns, URIResolver.MatchRule.BEST_MATCH));
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
