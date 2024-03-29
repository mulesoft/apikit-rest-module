/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form.transformation;


import org.mule.module.apikit.api.exception.InvalidFormParameterException;

import java.util.OptionalLong;

/**
 * Content to be pushed to the flow
 */
public interface Multipart {

  /**
   * Content to be pushed to implementation flow
   */
  Object content() throws InvalidFormParameterException;

  /**
   * @return content-type
   */
  String contentType();

  /**
   * @return content length
   * @since 1.7.0
   */
  OptionalLong getLength();

}
