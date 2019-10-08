/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.api.validation;

import org.mule.runtime.api.metadata.TypedValue;

public class ValidBody {

  private Object payload;

  public ValidBody(Object payload) {
    this.payload = payload;
  }

  public Object getPayload() {
    if (payload instanceof TypedValue) {
      return ((TypedValue) payload).getValue();
    } else {
      return payload;
    }
  }
}
