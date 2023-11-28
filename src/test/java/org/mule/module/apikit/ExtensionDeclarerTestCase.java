/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static java.util.Collections.emptySet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.mule.runtime.api.dsl.DslResolvingContext;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.api.meta.model.declaration.fluent.ExtensionDeclarer;
import org.mule.runtime.extension.api.loader.ExtensionLoadingContext;
import org.mule.runtime.extension.internal.loader.DefaultExtensionLoadingContext;
import org.mule.runtime.extension.internal.loader.ExtensionModelFactory;

import org.junit.Test;

public class ExtensionDeclarerTestCase {

  @Test
  public void getApikitExtensionDeclarer() {
    ApikitExtensionLoadingDelegate apikitExtensionLoadingDelegate = new ApikitExtensionLoadingDelegate();
    ExtensionDeclarer extensionDeclarer = new ExtensionDeclarer();
    apikitExtensionLoadingDelegate.accept(extensionDeclarer, null);

    ExtensionLoadingContext ctx =
        new DefaultExtensionLoadingContext(extensionDeclarer, Thread.currentThread().getContextClassLoader(),
                                           DslResolvingContext.getDefault(emptySet()));
    ExtensionModel extensionModel = new ExtensionModelFactory().create(ctx);
    assertEquals(1, extensionModel.getConfigurationModels().size());
    assertEquals(2, extensionModel.getConfigurationModels().get(0).getOperationModels().size());
    assertTrue(extensionModel.getConfigurationModels().get(0).getOperationModel("console").isPresent());
    assertEquals(1, extensionModel.getConfigurationModels().get(0).getOperationModels().get(0).getErrorModels().size());
    assertTrue(extensionModel.getConfigurationModels().get(0).getOperationModel("router").isPresent());
    assertEquals(5, extensionModel.getConfigurationModels().get(0).getOperationModels().get(1).getErrorModels().size());
    assertEquals(7, extensionModel.getErrorModels().size());
  }
}
