/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.helpers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Util class for mapping config-name with url where router is listening for requests
 */
public enum ConfigURLMapping {

  INSTANCE;

  private static final Map<String, String> configUrl = new ConcurrentHashMap<>();

  public void registerConfigURL(String configName, String url) {
    configUrl.put(configName, url);
  }

  public String getUrl(String configName) {
    return configUrl.get(configName);
  }

}
