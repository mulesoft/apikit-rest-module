/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.utils;

import org.mule.runtime.api.meta.MuleVersion;
import org.mule.runtime.core.api.config.MuleManifest;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class MuleVersionUtils {

  private MuleVersionUtils() {}

  public static boolean isAtLeast(String version) {
    if (isBlank(version)) {
      return false;
    }
    try {
      return new MuleVersion(MuleManifest.getProductVersion().split("-")[0]).atLeast(version);
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

}
