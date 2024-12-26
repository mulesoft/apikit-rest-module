package org.mule.module.apikit.error;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mule.runtime.api.event.Event;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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