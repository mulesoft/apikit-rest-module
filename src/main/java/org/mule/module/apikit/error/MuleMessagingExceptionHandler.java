/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.error;

import org.mule.runtime.api.event.Event;
import org.mule.runtime.core.api.event.CoreEvent;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
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
        return (Exception) constructor.newInstance(event, exception);
      }
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
      LOGGER.error("Error while handling exception for main flow: " + e.getMessage());
    }
    LOGGER.warn("Cannot transform to MuleMessagingException. payload are not going to be propagated to the main flow");
    return exception;
  }
}
