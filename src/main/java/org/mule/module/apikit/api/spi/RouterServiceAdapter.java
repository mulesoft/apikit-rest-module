/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.api.spi;

import org.mule.module.apikit.Router;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.core.api.event.CoreEvent;

import org.reactivestreams.Publisher;

public final class RouterServiceAdapter {

  private RouterService delegate;
  private org.mule.module.apikit.spi.RouterService fallbackDelegate;

  public RouterServiceAdapter(RouterService delegate) {
    this.delegate = delegate;
  }

  public RouterServiceAdapter(org.mule.module.apikit.spi.RouterService delegate) {
    this.fallbackDelegate = delegate;
  }

  public void initialise(String ramlPath) throws MuleException {
    if (delegate != null) {
      this.delegate = delegate.initialise(ramlPath);
    }
  }

  public Publisher<CoreEvent> process(CoreEvent event, Router router, String ramlPath) throws MuleException {
    if (delegate != null) {
      return delegate.process(event, router);
    } else {
      return fallbackDelegate.process(event, router, ramlPath);
    }
  }
}
