/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.helpers;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class FlowName {

  private FlowName() {}

  public static final String FLOW_NAME_SEPARATOR = ":";
  public static final String URL_RESOURCE_SEPARATOR = "/";

  private static final ImmutableMap<String, String> specialCharacters = ImmutableMap.<String, String>builder()
      .put(URL_RESOURCE_SEPARATOR, "\\")
      .put("{", "(")
      .put("}", ")")
      .build();


  public static String encode(String value) {
    for (Map.Entry<String, String> entry : specialCharacters.entrySet()) {
      value = value.replace(entry.getKey(), entry.getValue());
    }

    return value;
  }

  public static String decode(String value) {
    for (Map.Entry<String, String> entry : specialCharacters.entrySet()) {
      value = value.replace(entry.getValue(), entry.getKey());
    }

    return value;
  }

}
