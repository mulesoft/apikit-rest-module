/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.error;

import org.mule.runtime.api.component.execution.ComponentExecutionException;
import org.mule.runtime.api.event.Event;

/**
 * {@link RouterExceptionHandler} that propagates the resulting event in a {@link ComponentExecutionException}.
 * <p>
 * This is only going to work from mule version 4.2.2 and forward.
 */
public class ComponentExecutionExceptionHandler implements RouterExceptionHandler {

  // TODO(APIKIT-1943): CHANGE IMPLEMENTATION ONCE NEW MULE API FOR ERRORS IS AVAILABLE, UNIGNORE TESTS.
  public Exception handle(Event event, Exception exception) {
    return new ComponentExecutionException(exception, event);
  }
}
