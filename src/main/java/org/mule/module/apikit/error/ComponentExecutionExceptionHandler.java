/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.error;

import org.mule.runtime.api.component.execution.ComponentExecutionException;
import org.mule.runtime.api.event.Event;

public class ComponentExecutionExceptionHandler implements RouterExceptionHandler {

  public Exception handle(Event event, Exception exception) {
    return new ComponentExecutionException(exception, event);
  }
}
