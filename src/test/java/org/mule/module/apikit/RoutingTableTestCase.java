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
    Assert.assertEquals(URIResolveResult.Status.ERROR, resolver2.resolve(bestPattern2).getStatus());                 

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
    patterns.add(new URIPattern("/api/"));

    URIResolver resolver1 = new URIResolver("/api/hello/");
    Assert.assertNotNull(resolver1.find(patterns, URIResolver.MatchRule.BEST_MATCH));

    URIResolver resolver3 = new URIResolver("/api");
    Assert.assertNull(resolver3.find(patterns, URIResolver.MatchRule.BEST_MATCH));

    patterns.add(new URIPattern("/{param}"));
    URIResolver resolver2 = new URIResolver("/");
    Assert.assertNotNull(resolver2.find(patterns, URIResolver.MatchRule.BEST_MATCH));
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

  @Test
  public void testFindBestMethodWithEmptyParams() {
    // Test Case 1: Empty parameter in the last
    HashSet<URIPattern> patterns1 = new HashSet<>();
    patterns1.add(new URIPattern("/api/users/{id}"));

    URIResolver resolver1 = new URIResolver("/api/users/");
    URIPattern bestPattern1 = resolver1.find(patterns1, URIResolver.MatchRule.BEST_MATCH);
    Assert.assertNotNull(bestPattern1);
    Assert.assertEquals("/api/users/{id}", bestPattern1.toString());


    // Test Case 2: Testing empty parameter in middle
    HashSet<URIPattern> patterns2 = new HashSet<>();
    patterns2.add(new URIPattern("/users/{id}/posts"));
    patterns2.add(new URIPattern("/users/{id}"));
    patterns2.add(new URIPattern("/users"));

    URIResolver resolver2 = new URIResolver("/users//posts");
    URIPattern bestPattern2 = resolver2.find(patterns2, URIResolver.MatchRule.BEST_MATCH);
    Assert.assertEquals("/users/{id}/posts", bestPattern2.toString());

    // Test Case 3: Testing multiple empty parameters
    HashSet<URIPattern> patterns3 = new HashSet<>();
    patterns3.add(new URIPattern("/api/{version}/users/{id}"));
    patterns3.add(new URIPattern("/users/{id}"));
    patterns3.add(new URIPattern("/users"));

    URIResolver resolver3 = new URIResolver("/api//users/");
    URIPattern bestPattern3 = resolver3.find(patterns3, URIResolver.MatchRule.BEST_MATCH);
    Assert.assertEquals("/api/{version}/users/{id}", bestPattern3.toString());

    // Test Case 4: Testing empty parameter with nested resources
    HashSet<URIPattern> patterns4 = new HashSet<>();
    patterns4.add(new URIPattern("/users/{id}/posts/{postId}/comments"));
    patterns4.add(new URIPattern("/users/{id}/posts/{postId}"));
    patterns4.add(new URIPattern("/users/{id}/posts"));

    URIResolver resolver4 = new URIResolver("/users//posts//comments");
    URIPattern bestPattern4 = resolver4.find(patterns4, URIResolver.MatchRule.BEST_MATCH);
    Assert.assertEquals("/users/{id}/posts/{postId}/comments", bestPattern4.toString());

    // Test Case 5: Testing empty parameter with root path
    HashSet<URIPattern> patterns5 = new HashSet<>();
    patterns5.add(new URIPattern("/{version}"));
    patterns5.add(new URIPattern("/"));
    patterns5.add(new URIPattern(""));

    URIResolver resolver12 = new URIResolver("//");
    URIPattern bestPattern12 = resolver12.find(patterns5, URIResolver.MatchRule.BEST_MATCH);
    Assert.assertNull(bestPattern12);
  }
}
