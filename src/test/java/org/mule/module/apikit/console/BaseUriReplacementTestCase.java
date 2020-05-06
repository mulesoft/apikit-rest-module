/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.console;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mule.module.apikit.api.RamlHandler;
import org.mule.module.apikit.api.UrlUtils;
import org.mule.parser.service.ParserMode;
import org.mule.runtime.api.exception.ErrorTypeRepository;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.core.api.MuleContext;
import org.mule.service.scheduler.internal.DefaultSchedulerService;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mule.parser.service.ParserMode.AUTO;

public class BaseUriReplacementTestCase {

  private static final String FULL_DOMAIN = UrlUtils.FULL_DOMAIN;

  private static MuleContext muleContext;
  private static DefaultSchedulerService service;

  @BeforeClass
  public static void beforeAll() throws MuleException {
    muleContext = mock(MuleContext.class);
    when(muleContext.getExecutionClassLoader()).thenReturn(Thread.currentThread().getContextClassLoader());
    service = new DefaultSchedulerService();
    service.start();
  }

  @AfterClass
  public static void afterAll() throws MuleException {
    service.stop();
  }

  @Test
  public void baseUriReplacementTest() throws Exception {
    ErrorTypeRepository errorRepo = muleContext.getErrorTypeRepository();
    RamlHandler ramlHandler = new RamlHandler(service, "unit/console/simple-with-baseuri10.raml", false, errorRepo, AUTO);
    assertEquals("http://localhost:8081/api", ramlHandler.getBaseUriReplacement("http://localhost:8081/api"));
    assertEquals("http://localhost:8081/api", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/api"));

    System.setProperty(FULL_DOMAIN, "pepe.cloudhub.io");
    assertEquals("http://localhost:8081/api", ramlHandler.getBaseUriReplacement("http://localhost:8081/api"));
    assertEquals("http://pepe.cloudhub.io/api", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/api"));

    System.setProperty(FULL_DOMAIN, "http://pepe.cloudhub.io");
    assertEquals("http://localhost:8081/api", ramlHandler.getBaseUriReplacement("http://localhost:8081/api"));
    assertEquals("http://pepe.cloudhub.io/api", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/api"));

    System.setProperty(FULL_DOMAIN, "http://pepe.cloudhub.io/");
    assertEquals("http://localhost:8081/api", ramlHandler.getBaseUriReplacement("http://localhost:8081/api"));
    assertEquals("http://pepe.cloudhub.io/api", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/api"));

    System.setProperty(FULL_DOMAIN, "pepe.cloudhub.io/");
    assertEquals("http://localhost:8081/api", ramlHandler.getBaseUriReplacement("http://localhost:8081/api"));
    assertEquals("http://pepe.cloudhub.io/api", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/api"));

    System.setProperty(FULL_DOMAIN, "pepe.cloudhub.io");
    assertEquals("http://localhost:8081/api/", ramlHandler.getBaseUriReplacement("http://localhost:8081/api/"));
    assertEquals("http://pepe.cloudhub.io/api/", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/api/"));

    System.setProperty(FULL_DOMAIN, "http://pepe.cloudhub.io");
    assertEquals("http://pepe.cloudhub.io/api/", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/api/"));

    System.setProperty(FULL_DOMAIN, "http://pepe.cloudhub.io/");
    assertEquals("http://pepe.cloudhub.io/api/", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/api/"));

    System.setProperty(FULL_DOMAIN, "pepe.cloudhub.io/");
    assertEquals("http://pepe.cloudhub.io/api/", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/api/"));

    System.setProperty(FULL_DOMAIN, "pepe.cloudhub.io");
    assertEquals("http://localhost:8081/", ramlHandler.getBaseUriReplacement("http://localhost:8081/"));
    assertEquals("http://pepe.cloudhub.io/", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/"));

    System.setProperty(FULL_DOMAIN, "http://pepe.cloudhub.io");
    assertEquals("http://pepe.cloudhub.io/", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/"));

    System.setProperty(FULL_DOMAIN, "http://pepe.cloudhub.io/");
    assertEquals("http://pepe.cloudhub.io/", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/"));

    System.setProperty(FULL_DOMAIN, "pepe.cloudhub.io/");
    assertEquals("http://pepe.cloudhub.io/", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/"));

    System.setProperty(FULL_DOMAIN, "pepe.cloudhub.io");
    assertEquals("http://localhost:8081", ramlHandler.getBaseUriReplacement("http://localhost:8081"));
    assertEquals("http://pepe.cloudhub.io", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081"));

    System.setProperty(FULL_DOMAIN, "http://pepe.cloudhub.io");
    assertEquals("http://pepe.cloudhub.io", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081"));

    System.setProperty(FULL_DOMAIN, "http://pepe.cloudhub.io/");
    assertEquals("http://pepe.cloudhub.io/", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081"));

    System.setProperty(FULL_DOMAIN, "pepe.cloudhub.io/");
    assertEquals("http://pepe.cloudhub.io/", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081"));

    System.setProperty(FULL_DOMAIN, "pepe.cloudhub.io/api");
    assertEquals("http://pepe.cloudhub.io/api", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081"));

    System.setProperty(FULL_DOMAIN, "http://pepe.cloudhub.io/api");
    assertEquals("http://pepe.cloudhub.io/api", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081"));

    System.setProperty(FULL_DOMAIN, "http://pepe.cloudhub.io/api");
    assertEquals("http://pepe.cloudhub.io/api", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081"));

    System.setProperty(FULL_DOMAIN, "pepe.cloudhub.io/api");
    assertEquals("http://pepe.cloudhub.io/api", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081"));
  }

  @Test
  public void consoleUriReplacementTest() throws Exception {
    // Console Replacements
    System.clearProperty(FULL_DOMAIN);
    assertEquals("http://localhost:8081/console", UrlUtils.getBaseUriReplacement("http://localhost:8081/console"));
    assertEquals("http://localhost:8081/console/", UrlUtils.getBaseUriReplacement("http://localhost:8081/console/"));

    System.setProperty(FULL_DOMAIN, "http://aamura.cloudhub.io/api");
    assertEquals("http://aamura.cloudhub.io/api/console", UrlUtils.getBaseUriReplacement("http://0.0.0.0:8081/console"));
    assertEquals("http://aamura.cloudhub.io/api/console/", UrlUtils.getBaseUriReplacement("http://0.0.0.0:8081/console/"));

    System.setProperty(FULL_DOMAIN, "http://aamura.cloudhub.io/api/");
    assertEquals("http://aamura.cloudhub.io/api/console", UrlUtils.getBaseUriReplacement("http://0.0.0.0:8081/console"));
    assertEquals("http://aamura.cloudhub.io/api/console/", UrlUtils.getBaseUriReplacement("http://0.0.0.0:8081/console/"));

    System.setProperty(FULL_DOMAIN, "https://aamura.cloudhub.io/api");
    assertEquals("https://aamura.cloudhub.io/api/console", UrlUtils.getBaseUriReplacement("http://0.0.0.0:8081/console"));
    assertEquals("https://aamura.cloudhub.io/api/console/", UrlUtils.getBaseUriReplacement("http://0.0.0.0:8081/console/"));

    System.setProperty(FULL_DOMAIN, "https://aamura.cloudhub.io/api/");
    assertEquals("https://aamura.cloudhub.io/api/console", UrlUtils.getBaseUriReplacement("http://0.0.0.0:8081/console"));
    assertEquals("https://aamura.cloudhub.io/api/console/", UrlUtils.getBaseUriReplacement("http://0.0.0.0:8081/console/"));

    System.setProperty(FULL_DOMAIN, "https://aamura.cloudhub.io/api/v1");
    assertEquals("https://aamura.cloudhub.io/api/v1/console", UrlUtils.getBaseUriReplacement("http://0.0.0.0:8081/console"));
    assertEquals("https://aamura.cloudhub.io/api/v1/console/", UrlUtils.getBaseUriReplacement("http://0.0.0.0:8081/console/"));

    System.setProperty(FULL_DOMAIN, "aamura.cloudhub.io/api/v1");
    assertEquals("https://aamura.cloudhub.io/api/v1/console", UrlUtils.getBaseUriReplacement("https://0.0.0.0:8081/console"));
    assertEquals("https://aamura.cloudhub.io/api/v1/console/", UrlUtils.getBaseUriReplacement("https://0.0.0.0:8081/console/"));

    assertEquals("http://aamura.cloudhub.io/api/v1/console", UrlUtils.getBaseUriReplacement("http://0.0.0.0:8081/console"));
    assertEquals("http://aamura.cloudhub.io/api/v1/console/", UrlUtils.getBaseUriReplacement("http://0.0.0.0:8081/console/"));
  }

  @AfterClass
  public static void after() {
    System.clearProperty(FULL_DOMAIN);
  }
}
