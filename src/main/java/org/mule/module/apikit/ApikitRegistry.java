/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ApikitRegistry {

  private Map<String, Configuration> configMap = new ConcurrentHashMap<>();
  private Map<String, String> apiSourceMap = new ConcurrentHashMap<>();

  public void registerConfiguration(Configuration config) {
    synchronized (this) {
      this.configMap.put(config.getName(), config);
      config.getRamlHandler().setApiServer(apiSourceMap.get(config.getName()));
      for (String apiSourceMapItem : apiSourceMap.keySet()) {
        if (configMap.get(apiSourceMapItem) != null) {
          configMap.get(apiSourceMapItem).getRamlHandler().setApiServer(apiSourceMap.get(apiSourceMapItem));
        }
      }
    }
  }

  public Configuration getConfiguration(String configName) {
    return configMap.get(configName);
  }

  public void setApiSource(String configName, String apiSource) {
    synchronized (this) {
      apiSourceMap.put(configName, apiSource);
      for (String apiSourceMapItem : apiSourceMap.keySet()) {
        if (configMap.get(apiSourceMapItem) != null) {
          configMap.get(apiSourceMapItem).getRamlHandler().setApiServer(apiSourceMap.get(apiSourceMapItem));
        }
      }
    }
  }

}
