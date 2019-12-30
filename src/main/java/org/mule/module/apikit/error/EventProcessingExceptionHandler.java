/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.error;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.mule.runtime.api.component.execution.ComponentExecutionException;
import org.mule.runtime.api.event.Event;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.privileged.exception.MessagingExceptionUtils;

/**
 * {@link RouterExceptionHandler} that propagates the resulting event in a {@link
 * EventProcessingException}.
 * <p>
 * This class will try to find createMessagingException method from MessagingExceptionUtils class
 * reflectively because is present since mule runtime 4.2.2.
 *
 * createMessagingException method was created to propagate a whole custom event when an error occurs.
 *
 * @since 1.3.7
 */
public class EventProcessingExceptionHandler {

  private final Method createMessagingException;

  public EventProcessingExceptionHandler() {
    this.createMessagingException = getCreateMessagingExceptionMethod();
  }

  public Exception handle(Event event, Exception exception) {
    try {
      return (Exception) createMessagingException.invoke(null, event, exception.getCause());
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new IllegalStateException("Error trying to invoke createMessagingException method", e);
    }
  }

  private Method getCreateMessagingExceptionMethod() {
    try {
      return MessagingExceptionUtils.class
          .getMethod("createMessagingException", CoreEvent.class, Throwable.class);
    } catch (NoSuchMethodException e) {
      throw new IllegalStateException("Method createMessagingException not found");
    }
  }
}
