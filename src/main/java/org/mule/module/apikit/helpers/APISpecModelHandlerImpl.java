/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.helpers;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.mule.apikit.ApiType.AMF;

import java.util.Optional;
import org.mule.apikit.ApiType;
import org.mule.module.apikit.api.RamlHandler;

public class APISpecModelHandlerImpl implements APISpecModelHandler {

  private final RamlHandler ramlHandler;
  private final String listenerPath;
  private final String requestPath;
  private final String queryString;
  private final String method;
  private final String acceptHeader;
  private final ApiType apiType;
  private final String resourceRelativePath;

  public APISpecModelHandlerImpl(RamlHandler ramlHandler, String listenerPath,
                                 String requestPath, String queryString, String method, String acceptHeader,
                                 ApiType apiType, String resourceRelativePath) {
    this.ramlHandler = ramlHandler;
    this.listenerPath = listenerPath;
    this.requestPath = requestPath;
    this.queryString = queryString;
    this.method = method;
    this.acceptHeader = acceptHeader;
    this.apiType = apiType;
    this.resourceRelativePath = resourceRelativePath;
  }

  @Override
  public synchronized Optional<String> getModel(String hostURL) {
    if (queryString.equals("api")) {
      return ofNullable(ramlHandler.dumpRaml());
    }
    if (ramlHandler.isRequestingRamlV1ForConsole(listenerPath, requestPath, queryString, method, acceptHeader)) {
      setApiServer(hostURL);
      return ofNullable(ramlHandler.getRamlV1());
    }

    if (ramlHandler.isRequestingRamlV2(listenerPath, requestPath, queryString, method)) {
      setApiServer(hostURL);
      return ofNullable(ramlHandler.getRamlV2(resourceRelativePath));
    }

    if (AMF.equals(apiType) && "amf".equals(queryString)) {
      setApiServer(hostURL);
      return ofNullable(ramlHandler.getAMFModel());
    }

    return empty();
  }

  private void setApiServer(String hostURL) {
    if (hostURL != null) {
      ramlHandler.setApiServer(hostURL);
    }
  }

}
