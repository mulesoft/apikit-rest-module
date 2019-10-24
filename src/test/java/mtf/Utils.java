/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package mtf;

import org.mule.runtime.api.exception.ErrorMessageAwareException;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.TypedValue;

public class Utils {

  public static void setSystemProperty(String key, String value) {
    System.setProperty(key, value);
  }

  public static ExceptionWithMuleMessage throwErrorMessageAwareException() {
    return new ExceptionWithMuleMessage();
  }

  public static class ExceptionWithMuleMessage extends RuntimeException implements ErrorMessageAwareException {

    ExceptionWithMuleMessage() {
      super("We are testing, everything is fine");
    }

    @Override
    public Message getErrorMessage() {
      return Message.builder()
          .payload(new TypedValue<>("Payload value", DataType.STRING))
          .attributes(new TypedValue<>("Attributes value", DataType.STRING)).build();
    }
  }


}
