/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.routing;

import static java.util.Optional.ofNullable;
import static org.mule.runtime.core.privileged.processor.MessageProcessors.processWithChildContext;

import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.core.api.construct.Flow;
import org.mule.runtime.core.api.event.CoreEvent;

import org.reactivestreams.Publisher;

/**
 * Implementation of {@link FlowRoutingStrategy} that uses the Mule privileged API to perform the flow routing job,
 * this should not be used since MULE 4.2.0 and forward versions, use {@link DefaultFlowRoutingStrategy} instead.
 */
public class PrivilegedFlowRoutingStrategy implements FlowRoutingStrategy {

  private final ComponentLocation location;

  public PrivilegedFlowRoutingStrategy(ComponentLocation location) {
    this.location = location;
  }

  public Publisher<CoreEvent> route(Flow flow, CoreEvent mainEvent, CoreEvent subFlowEvent) {
    return processWithChildContext(subFlowEvent, flow, ofNullable(location), flow.getExceptionListener());
  }
}
