/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static java.util.Collections.emptySet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.mule.runtime.api.dsl.DslResolvingContext;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.api.meta.model.config.ConfigurationModel;
import org.mule.runtime.api.meta.model.declaration.fluent.ExtensionDeclarer;
import org.mule.runtime.extension.api.loader.ExtensionLoadingContext;
import org.mule.runtime.extension.api.persistence.ExtensionModelJsonSerializer;
import org.mule.runtime.extension.api.runtime.config.ConfigurationFactory;
import org.mule.runtime.extension.internal.loader.DefaultExtensionLoadingContext;
import org.mule.runtime.extension.internal.loader.ExtensionModelFactory;
import org.mule.runtime.module.extension.internal.loader.java.property.ConfigurationFactoryModelProperty;

import org.junit.Before;
import org.junit.Test;

import io.qameta.allure.Issue;

public class ExtensionDeclarerTestCase {

  private ExtensionModel extensionModel;

  @Before
  public void setUp() {
    ApikitExtensionLoadingDelegate apikitExtensionLoadingDelegate = new ApikitExtensionLoadingDelegate();
    ExtensionDeclarer extensionDeclarer = new ExtensionDeclarer();
    apikitExtensionLoadingDelegate.accept(extensionDeclarer, null);
    ExtensionLoadingContext ctx =
        new DefaultExtensionLoadingContext(extensionDeclarer, Thread.currentThread().getContextClassLoader(),
                                           DslResolvingContext.getDefault(emptySet()));
    extensionModel = new ExtensionModelFactory().create(ctx);
  }

  @Test
  public void getApikitExtensionDeclarer() {
    ExtensionModelJsonSerializer serializer = new ExtensionModelJsonSerializer(true);
    String jsonContent = serializer.serialize(extensionModel);
    assertNotNull(jsonContent);
    assertEquals(2, countOccurences(jsonContent, "BAD_REQUEST"));
    assertEquals(3, countOccurences(jsonContent, "NOT_FOUND"));
    assertEquals(2, countOccurences(jsonContent, "METHOD_NOT_ALLOWED"));
    assertEquals(2, countOccurences(jsonContent, "UNSUPPORTED_MEDIA_TYPE"));
    assertEquals(2, countOccurences(jsonContent, "NOT_ACCEPTABLE"));
  }

  @Test
  @Issue("W-11858268")
  public void configHasConfigFactory() {
    ConfigurationModel configurationModel = extensionModel.getConfigurationModel("config").get();
    ConfigurationFactoryModelProperty configurationFactoryModelProperty =
        configurationModel.getModelProperty(ConfigurationFactoryModelProperty.class).get();
    ConfigurationFactory configurationFactory = configurationFactoryModelProperty.getConfigurationFactory();
    assertThat(configurationFactory.newInstance(), instanceOf(Configuration.class));
  }

  private static int countOccurences(String str, String substring) {
    int lastIndex = 0;
    int count = 0;
    while (lastIndex >= 0) {
      lastIndex = str.indexOf(substring, lastIndex);
      if (lastIndex >= 0) {
        count++;
        lastIndex += substring.length();
      }
    }
    return count;
  }
}
