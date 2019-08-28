/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.error;

import org.mule.runtime.api.component.execution.ExecutableComponent;
import org.mule.runtime.api.event.Event;
import org.mule.runtime.core.api.event.CoreEvent;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RouterExceptionHandler} implementation that wraps the resulting event and exception into a MessagingException.
 *
 * The MuleMessagingException is an internal Mule exception but it the only one that provides de capability to provide a custom
 * event to the processing chain when an error occurs, there is no API for doing this until mule 4.2.2 in which cases the
 * {@link ComponentExecutionExceptionHandler} should be active instead of this implementation.
 *
 * This class builds a MessagingException reflectively by looking up in the classloader hierarchy until the class is visible.
 *
 * Also note that previous versions of APIKit did not have this code, this is because the Router used to use a privileged API that
 * does not work anymore. Now, instead, is using the {@link ExecutableComponent} API to execute flows dynamically, see the
 * org.mule.module.apikit.Router#doRoute() for more info on this.
 */
public class MuleMessagingExceptionHandler implements RouterExceptionHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(MuleMessagingExceptionHandler.class);

  private final Constructor constructor;

  public MuleMessagingExceptionHandler() {
    this.constructor = getMessagingExceptionConstructor(Thread.currentThread().getContextClassLoader());
  }

  private Constructor getMessagingExceptionConstructor(ClassLoader cl) {
    if (cl == null) {
      return null;
    }
    try {
      Class<?> clazz = cl.loadClass("org.mule.runtime.core.internal.exception.MessagingException");
      return clazz.getConstructor(CoreEvent.class, Throwable.class);
    } catch (Exception e) {
      return getMessagingExceptionConstructor(cl.getParent());
    }
  }

  @Override
  public Exception handle(Event event, Exception exception) {
    try {
      if (constructor != null) {
        // we use exception.getCause() to get the actual exception thrown by the module inside the flow.
        // this way we propagate to the runtime an exception that may carry additional data. e.g. an ErrorMessageAwareException
        return (Exception) constructor.newInstance(event, exception.getCause());
      }
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
      LOGGER.error("Error while handling exception for main flow: " + e.getMessage());
    }
    LOGGER.warn("Cannot transform to MuleMessagingException. payload are not going to be propagated to the main flow");
    return exception;
  }
}
