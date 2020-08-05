/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.helpers;

import java.util.Optional;

public interface APISpecModelHandler {

  /**
   * @param hostURL <protocol>://<host>/<api-server-path>
   * @return API spec model as String, with base URL <protocol>://<host>/<api-server-path>
   */
  Optional<String> getModel(String hostURL);

}
