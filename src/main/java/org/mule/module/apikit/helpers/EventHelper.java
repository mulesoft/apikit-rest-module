/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.helpers;


import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.core.api.event.CoreEvent;

import java.nio.charset.Charset;
import java.util.Optional;

public class EventHelper {

  private EventHelper() {

  }

  public static <T> Charset getEncoding(TypedValue<T> payload) {
    Optional<Charset> payloadEncoding = payload.getDataType().getMediaType().getCharset();
    return payloadEncoding.orElse(Charset.defaultCharset());// TODO Should we get default charset from mule?
  }

  public static HttpRequestAttributes getHttpRequestAttributes(CoreEvent event) {
    return ((HttpRequestAttributes) event.getMessage().getAttributes().getValue());
  }

}
