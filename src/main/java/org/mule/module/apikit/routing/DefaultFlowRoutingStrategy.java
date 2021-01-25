/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.routing;

import static reactor.core.publisher.Mono.fromFuture;

import org.mule.module.apikit.error.EventProcessingExceptionHandler;
import org.mule.module.apikit.error.MuleMessagingExceptionHandler;
import org.mule.module.apikit.error.RouterExceptionHandler;
import org.mule.module.apikit.utils.MuleVersionUtils;
import org.mule.runtime.api.component.execution.ComponentExecutionException;
import org.mule.runtime.api.component.execution.ExecutableComponent;
import org.mule.runtime.api.event.Event;
import org.mule.runtime.core.api.construct.Flow;
import org.mule.runtime.core.api.event.CoreEvent;

import java.util.concurrent.CompletableFuture;

import org.reactivestreams.Publisher;

/**
 * Default implementation of {@link FlowRoutingStrategy}, uses the Mule {@link ExecutableComponent} API to perform
 * flow routing.
 */
public class DefaultFlowRoutingStrategy implements FlowRoutingStrategy {

  private final RouterExceptionHandler exceptionHandler;

  public DefaultFlowRoutingStrategy() {
    // In MULE 4.2.0 and 4.2.1 there is not way to propagate an event to the main flow when an error occur, for that
    // we have the MuleMessagingExceptionHandler that reflectively creates an internal Mule Exception to do this task.
    // since Mule 4.2.2 there is a proper API to propagate the error with an exception, see EventProcessingExceptionHandler.
    this.exceptionHandler =
        MuleVersionUtils.isAtLeast("4.2.2") ? new EventProcessingExceptionHandler() : new MuleMessagingExceptionHandler();
  }

  public Publisher<CoreEvent> route(Flow flow, CoreEvent mainEvent, CoreEvent subFlowEvent) {
    CompletableFuture<Event> execution = flow.execute(subFlowEvent);
    return ((Publisher) fromFuture(execution)
        .onErrorMap(ComponentExecutionException.class,
                    e -> exceptionHandler.handle(CoreEvent.builder(mainEvent.getContext(), ((CoreEvent) e.getEvent())).build(),
                                                 e)));
  }
}
