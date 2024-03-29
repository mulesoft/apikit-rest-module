/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.mule.apikit.model.Action;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.Resource;
import org.mule.module.apikit.api.RamlHandler;
import org.mule.module.apikit.api.RoutingTable;
import org.mule.module.apikit.api.uri.URIPattern;
import org.mule.module.apikit.api.uri.URIResolver;
import org.mule.module.apikit.exception.NotImplementedException;
import org.mule.module.apikit.exception.UnsupportedMediaTypeException;
import org.mule.module.apikit.helpers.FlowName;
import org.mule.module.apikit.uri.URICoder;
import org.mule.runtime.api.component.location.ConfigurationComponentLocator;
import org.mule.runtime.api.exception.ErrorTypeRepository;
import org.mule.runtime.core.api.construct.Flow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.mule.module.apikit.ApikitErrorTypes.throwErrorType;
import static org.mule.module.apikit.api.FlowUtils.getFlowsList;
import static org.mule.module.apikit.helpers.AttributesHelper.getMediaType;
import static org.mule.module.apikit.helpers.FlowName.FLOW_NAME_SEPARATOR;
import static org.mule.module.apikit.helpers.FlowName.URL_RESOURCE_SEPARATOR;


public class FlowFinder {

  protected static final Logger logger = LoggerFactory.getLogger(FlowFinder.class);

  private Map<String, Resource> flatResourceTree = new HashMap<>();
  private Map<String, Flow> restFlowMap;

  protected RoutingTable routingTable;

  private String apiVersion;
  private String configName;
  private List<FlowMapping> flowMappings;
  private ConfigurationComponentLocator locator;
  private ErrorTypeRepository errorTypeRepository;

  public FlowFinder(RamlHandler ramlHandler, String configName, ConfigurationComponentLocator locator,
                    List<FlowMapping> flowMappings, ErrorTypeRepository errorTypeRepository) {
    this.configName = configName;
    this.flowMappings = flowMappings;
    this.locator = locator;
    this.errorTypeRepository = errorTypeRepository;
    this.apiVersion = ramlHandler.getApi().getVersion();
    initializeRestFlowMap(ramlHandler);
    loadRoutingTable(ramlHandler);
  }

  protected void initializeRestFlowMap(RamlHandler ramlHandler) {
    final ApiSpecification api = ramlHandler.getApi();
    flattenResourceTree(api.getResources(), api.getVersion());

    if (restFlowMap == null) {
      restFlowMap = new HashMap<>();

      List<Flow> flows = getFlows();

      // init flows by convention
      for (Flow flow : flows) {
        String key = getRestFlowKey(flow.getName());
        if (key != null) {
          restFlowMap.put(key, flow);
        }
      }

      //// init flow mappings
      for (FlowMapping mapping : flowMappings) {
        for (Flow flow : flows) {
          if (flow.getName().equals(mapping.getFlowRef())) {
            mapping.setFlow(flow);
            restFlowMap.put(mapping.getKey(), mapping.getFlow());
          }
        }
      }

      logMissingMappings(api.getVersion());
    }
  }

  private List<Flow> getFlows() {
    return getFlowsList(locator);
  }

  private void flattenResourceTree(Map<String, Resource> resources, String version) {
    for (Resource resource : resources.values()) {
      if (!resource.getActions().isEmpty()) {
        flatResourceTree.put(resource.getResolvedUri(version), resource);
      }
      if (resource.getResources() != null) {
        flattenResourceTree(resource.getResources(), version);
      }
    }
  }

  public Map<String, Flow> getRawRestFlowMap() {
    return restFlowMap;
  }

  /**
   * validates if name is a valid router flow name according to the following pattern:
   * method:\resource[:content-type][:config-name]
   *
   * @param name to be validated
   * @return the name with the config-name stripped or null if it is not a router flow
   */
  private String getRestFlowKey(String name) {
    final String[] validMethods = {"get", "put", "post", "delete", "head", "patch", "options"};

    final String[] coords = FlowName.decode(name).split(FLOW_NAME_SEPARATOR);

    if (coords.length < 2)
      return null;

    final String method = coords[0];
    final String resource = coords[1];

    if (coords.length > 4 ||
        !Arrays.asList(validMethods).contains(method) ||
        !resource.startsWith(URL_RESOURCE_SEPARATOR)) {
      return null;
    }

    if (coords.length == 4) {
      if (coords[3].equals(configName)) {
        final String contentType = coords[2];
        return validateRestFlowKeyAgainstApi(method, resource, contentType);
      }
      return null;
    }

    if (coords.length == 3) {
      if (!coords[2].equals(configName)) {
        final String contentType = coords[2];
        return validateRestFlowKeyAgainstApi(method, resource, contentType);
      }
    }

    return validateRestFlowKeyAgainstApi(method, resource);
  }

  private String validateRestFlowKeyAgainstApi(String... coords) {
    String method = coords[0];
    String resource = URICoder.decode(coords[1]);
    String type = coords.length == 3 ? coords[2] : null;
    String key = format("%s:%s", method, resource);

    if (type != null) {
      key = key + ":" + type;
    }

    Resource apiResource = flatResourceTree.get(resource);
    if (apiResource != null) {
      Action action = apiResource.getAction(method);
      if (action != null) {
        if (type == null)
          return key;
        if (!action.hasBody() || action.getBody().entrySet().stream().anyMatch(v -> v.getKey().contains(type))) {
          return key;
        }
      }
    }

    return null;
  }

  private void logMissingMappings(String version) {
    for (Resource resource : flatResourceTree.values()) {
      String fullResource = resource.getResolvedUri(version);
      for (Action action : resource.getActions().values()) {
        String method = action.getType().name().toLowerCase();
        String key = method + ":" + fullResource;
        if (restFlowMap.get(key) != null) {
          continue;
        }
        if (action.hasBody()) {
          for (String contentType : action.getBody().keySet()) {
            String mediaType = retrieveMediaType(contentType, method, fullResource);
            if (mediaType != null && restFlowMap.get(key + ":" + mediaType) == null) {
              logger.warn(format("Action-Resource-ContentType triplet has no implementation -> %s:%s:%s ",
                                 method, fullResource, mediaType));
            }
          }
        } else {
          logger.warn(format("Action-Resource pair has no implementation -> %s:%s ",
                             method, fullResource));
        }
      }
    }
  }

  private String retrieveMediaType(String contentType, String method, String fullResource) {
    try {
      return getMediaType(contentType);
    } catch (UnsupportedMediaTypeException e) {
      logger.warn(format("Action-Resource-ContentType triplet has no implementation -> %s:%s:%s ",
                         method, fullResource, contentType));
    }
    return null;
  }

  private void loadRoutingTable(RamlHandler ramlHandler) {
    if (routingTable == null) {
      routingTable = new RoutingTable(ramlHandler.getApi());
    }
  }

  public Flow getFlow(Resource resource, String method, String contentType) throws UnsupportedMediaTypeException {
    String baseKey = method + ":" + resource.getResolvedUri(apiVersion);
    Map<String, Flow> rawRestFlowMap = getRawRestFlowMap();
    Flow flow = rawRestFlowMap.get(baseKey + ":" + contentType);
    if (flow == null) {
      flow = rawRestFlowMap.get(baseKey);
      if (flow == null) {
        if (isFlowDeclaredWithDifferentMediaType(rawRestFlowMap, baseKey)) {
          throw throwErrorType(new UnsupportedMediaTypeException(), errorTypeRepository);
        } else {
          throw throwErrorType(new NotImplementedException(), errorTypeRepository);
        }
      }
    }
    return flow;
  }

  public Resource getResource(URIPattern uriPattern) {
    return routingTable.getResource(uriPattern);
  }

  private boolean isFlowDeclaredWithDifferentMediaType(Map<String, Flow> map, String baseKey) {
    for (String flowName : map.keySet()) {
      String[] split = flowName.split(":");
      String methodAndResource = split[0] + ":" + split[1];
      if (methodAndResource.equals(baseKey))
        return true;
    }
    return false;
  }

  public URIPattern findBestMatch(URIResolver resolver) {
    return resolver.find(routingTable.keySet(), URIResolver.MatchRule.BEST_MATCH);
  }
}
