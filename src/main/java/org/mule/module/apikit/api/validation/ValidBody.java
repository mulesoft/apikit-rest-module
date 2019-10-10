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
    setPayload(payload);
  }

  public TypedValue getPayloadAsTypedValue() {
    if (payload instanceof TypedValue) {
      return (TypedValue) payload;
    }
    return new TypedValue(payload, null);
  }

  public Object getPayload() {
    if (payload instanceof TypedValue) {
      return ((TypedValue) payload).getValue();
    }

    return payload;
  }

  public void setPayload(Object payload) {
    this.payload = payload;
  }

  @Deprecated
  public void setFormParameters(Object formParameters) {
    setPayload(formParameters);
  }

}
