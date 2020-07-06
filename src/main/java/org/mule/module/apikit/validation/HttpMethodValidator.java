/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;

import java.util.Set;
import org.mule.apikit.model.ActionType;
import org.mule.module.apikit.api.exception.MethodNotAllowedException;

public class HttpMethodValidator {

  /**
   * RAML : methods defined in the HTTP version 1.1 specification RFC2616 and its extension, RFC5789
   * OAS : https://tools.ietf.org/html/rfc7231#section-4.3.1
   */
  private static final Set<String> httpValidMethods = asList(ActionType.values())
      .stream()
      .map(value -> value.toString().toLowerCase())
      .collect(toSet());


  /**
   * @param requestMethod HTTP method in lower case
   * @throws MethodNotAllowedException
   */
  public void validateHttpMethod(String requestMethod) throws MethodNotAllowedException {
    if (!httpValidMethods.contains(requestMethod)) {
      throw new MethodNotAllowedException(String.format("HTTP Method : %s is not allowed", requestMethod));
    }
  }

}
