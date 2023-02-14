/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package mtf;

import static java.lang.System.setProperty;

import org.mule.runtime.api.exception.ErrorMessageAwareException;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.TypedValue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Utils {

  public static void setSystemProperty(String key, String value) {
    setProperty(key, value);
  }

  public static void generateFile(String fileName, long fileSize) throws IOException {
    try {
      String absolutePath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
      File file = new File(absolutePath + fileName);
      file.createNewFile();
      FileWriter writer = new FileWriter(file);

      for (int length = 0; length <= fileSize; length += 32) {
        writer.write("abcdefghijabcdefghijabcdefghijk");
        writer.write("\n");
      }

      writer.flush();
      writer.close();
    } catch (Exception e) {
      throw e;
    }
  }

  public static void deleteFile(String fileName) throws IOException {
    String absolutePath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
    if (new File(absolutePath + fileName).delete()) {
      return;
    }
    throw new IOException("Failed to delete file");

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
