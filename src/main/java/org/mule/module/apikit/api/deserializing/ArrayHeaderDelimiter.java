/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.api.deserializing;

/**
 * Set of values that are allowed as delimiters for HTTP array header values.
 */
public enum ArrayHeaderDelimiter {

  COMMA(','),
  SEMICOLON(';');

  private final char delimiter;

  ArrayHeaderDelimiter(char delimiter) {
    this.delimiter = delimiter;
  }

  public String getDelimiterValue() {
    return Character.toString(delimiter);
  }

  public char getDelimiterChar() {
    return delimiter;
  }

}
