/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation;

import org.mule.apikit.model.Action;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.module.apikit.api.config.ValidationConfig;
import org.mule.module.apikit.api.exception.MuleRestException;
import org.mule.module.apikit.api.uri.ResolvedVariables;
import org.mule.module.apikit.helpers.AttributesHelper;
import org.mule.module.apikit.validation.attributes.HeadersValidator;
import org.mule.module.apikit.validation.attributes.QueryParameterValidator;
import org.mule.module.apikit.validation.attributes.QueryStringValidator;
import org.mule.module.apikit.validation.attributes.UriParametersValidator;
import org.mule.module.apikit.validation.attributes.ValidatedQueryParams;
import org.mule.runtime.api.util.MultiMap;

import java.util.HashMap;
import java.util.Map;

public class AttributesValidator {

  public static HttpRequestAttributes validateAndAddDefaults(HttpRequestAttributes attributes, Action action,
                                                             ResolvedVariables resolvedVariables, ValidationConfig config)
      throws MuleRestException {

    MultiMap<String, String> headers;
    MultiMap<String, String> queryParams;
    String queryString;

    // uriparams
    UriParametersValidator.validate(action.getResolvedUriParameters(), resolvedVariables);

    // queryStrings
    QueryStringValidator.validate(action.queryString(), attributes.getQueryParams());

    // queryparams
    ValidatedQueryParams validatedQueryParams =
        QueryParameterValidator.validate(action.getQueryParameters(), attributes.getQueryParams(),
                                         attributes.getQueryString(),
                                         config.isQueryParamsStrictValidation());
    queryParams = validatedQueryParams.getQueryParams();
    queryString = validatedQueryParams.getQueryString();

    // headers
    headers = HeadersValidator.validateAndAddDefaults(action.getHeaders(), action.getResponses(),
            attributes.getHeaders(),
            config
                    .isHeadersStrictValidation());

    Map<String, String> uriParamsMap = new HashMap<>();
    resolvedVariables.names().forEach(name -> uriParamsMap.put(name, String.valueOf(resolvedVariables.get(name))));
    // regenerate attributes
    return AttributesHelper.replaceParams(attributes,
                                          headers,
                                          queryParams,
                                          queryString,
                                          uriParamsMap);
  }

}
