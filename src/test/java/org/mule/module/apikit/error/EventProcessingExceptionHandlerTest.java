/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.error;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mule.runtime.api.event.Event;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class EventProcessingExceptionHandlerTest {

    @Test
    public void testHandle() throws Exception {
        MuleMessagingExceptionHandler handler = new MuleMessagingExceptionHandler();
        Event event = mock(Event.class);
        Exception exception = mock(Exception.class);
        Assertions.assertThrows(IllegalArgumentException.class, () -> handler.handle(event, exception));
    }

    @Test
    public void testGetMessagingExceptionConstructor() throws Exception {
        MuleMessagingExceptionHandler handler = new MuleMessagingExceptionHandler();
        assertNotNull(handler);
    }
}