/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.mule.apikit.model.Action;
import org.mule.module.apikit.validation.attributes.HeadersValidator;
import org.mule.module.apikit.validation.attributes.QueryParameterValidator;
import org.mule.module.apikit.validation.attributes.QueryStringValidator;

public enum ValidatorsCache {

  INSTANCE;

  private Map<Action, QueryParameterValidator> queryParamValidators = new ConcurrentHashMap<>();
  private Map<Action, QueryStringValidator> queryStringValidators = new ConcurrentHashMap<>();
  private Map<Action, HeadersValidator> headersValidators = new ConcurrentHashMap<>();

  public QueryParameterValidator getQueryParameterValidator(Action action) {
    return queryParamValidators.computeIfAbsent(action, key -> new QueryParameterValidator(key));
  }

  public QueryStringValidator getQueryStringValidator(Action action) {
    return queryStringValidators.computeIfAbsent(action, key -> new QueryStringValidator(key));
  }

  public HeadersValidator getHeadersValidator(Action action) {
    return headersValidators.computeIfAbsent(action, key -> new HeadersValidator(key));
  }
}
