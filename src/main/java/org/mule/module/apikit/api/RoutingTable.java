/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.mule.module.apikit.api.uri.URIPattern;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.Resource;

public class RoutingTable {

  protected Map<URIPattern, Resource> routingTable = new HashMap<>();

  public RoutingTable(ApiSpecification api) {

    buildRoutingTable(api.getResources(), api.getVersion());
  }

  private void buildRoutingTable(Map<String, Resource> resources, String version) {

    for (Resource resource : resources.values()) {
      if (!resource.getActions().isEmpty()) {
        String uri = resource.getResolvedUri(version);
        routingTable.put(new URIPattern(uri), resource);
      }
      if (resource.getResources() != null) {
        buildRoutingTable(resource.getResources(), version);
      }

    }
  }

  public Resource getResource(String uri) {
    return routingTable.get(new URIPattern(uri));
  }

  public Resource getResource(URIPattern uriPattern) {
    return routingTable.get(uriPattern);
  }

  public Set<URIPattern> keySet() {
    return routingTable.keySet();
  }
}
