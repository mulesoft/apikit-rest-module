/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.routing;

import org.mule.module.apikit.error.EventProcessingExceptionHandler;
import org.mule.runtime.api.component.execution.ComponentExecutionException;
import org.mule.runtime.api.component.execution.ExecutableComponent;
import org.mule.runtime.api.event.Event;
import org.mule.runtime.core.api.construct.Flow;
import org.mule.runtime.core.api.event.CoreEvent;
import org.reactivestreams.Publisher;

import java.util.concurrent.CompletableFuture;

import static reactor.core.publisher.Mono.fromFuture;

/**
 * Default implementation of {@link FlowRoutingStrategy}, uses the Mule {@link ExecutableComponent} API to perform
 * flow routing.
 */
public class DefaultFlowRoutingStrategy implements FlowRoutingStrategy {

  private final EventProcessingExceptionHandler exceptionHandler;

  public DefaultFlowRoutingStrategy() {
    this.exceptionHandler = new EventProcessingExceptionHandler();
  }

  public Publisher<CoreEvent> route(Flow flow, CoreEvent mainEvent, CoreEvent subFlowEvent) {
    CompletableFuture<Event> execution = flow.execute(subFlowEvent);
    return ((Publisher) fromFuture(execution)
        .onErrorMap(ComponentExecutionException.class,
                    e -> exceptionHandler.handle(CoreEvent.builder(mainEvent.getContext(), ((CoreEvent) e.getEvent())).build(),
                                                 e)));
  }
}
