/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.console;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mule.module.apikit.api.RamlHandler;
import org.mule.module.apikit.api.UrlUtils;
import org.mule.runtime.api.exception.ErrorTypeRepository;
import org.mule.runtime.core.api.MuleContext;

import static java.lang.System.clearProperty;
import static java.lang.System.setProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mule.module.apikit.api.UrlUtils.getBaseUriReplacement;
import static org.mule.module.apikit.api.UrlUtils.replaceHostInURL;
import static org.mule.parser.service.ParserMode.AUTO;

public class BaseUriReplacementTestCase {

  private static final String FULL_DOMAIN = UrlUtils.FULL_DOMAIN;

  private static MuleContext muleContext;

  @BeforeClass
  public static void beforeAll() {
    muleContext = mock(MuleContext.class);
    when(muleContext.getExecutionClassLoader()).thenReturn(Thread.currentThread().getContextClassLoader());
  }

  @Test
  public void baseUriReplacementTest() throws Exception {
    ErrorTypeRepository errorRepo = muleContext.getErrorTypeRepository();
    RamlHandler ramlHandler = new RamlHandler(null, "unit/console/simple-with-baseuri10.raml", false, errorRepo, AUTO);
    assertEquals("http://localhost:8081/api", ramlHandler.getBaseUriReplacement("http://localhost:8081/api"));
    assertEquals("http://localhost:8081/api", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/api"));

    setProperty(FULL_DOMAIN, "pepe.cloudhub.io");
    assertEquals("http://localhost:8081/api", ramlHandler.getBaseUriReplacement("http://localhost:8081/api"));
    assertEquals("http://pepe.cloudhub.io/api", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/api"));

    setProperty(FULL_DOMAIN, "http://pepe.cloudhub.io");
    assertEquals("http://localhost:8081/api", ramlHandler.getBaseUriReplacement("http://localhost:8081/api"));
    assertEquals("http://pepe.cloudhub.io/api", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/api"));

    setProperty(FULL_DOMAIN, "http://pepe.cloudhub.io/");
    assertEquals("http://localhost:8081/api", ramlHandler.getBaseUriReplacement("http://localhost:8081/api"));
    assertEquals("http://pepe.cloudhub.io/api", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/api"));

    setProperty(FULL_DOMAIN, "pepe.cloudhub.io/");
    assertEquals("http://localhost:8081/api", ramlHandler.getBaseUriReplacement("http://localhost:8081/api"));
    assertEquals("http://pepe.cloudhub.io/api", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/api"));

    setProperty(FULL_DOMAIN, "pepe.cloudhub.io");
    assertEquals("http://localhost:8081/api/", ramlHandler.getBaseUriReplacement("http://localhost:8081/api/"));
    assertEquals("http://pepe.cloudhub.io/api/", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/api/"));

    setProperty(FULL_DOMAIN, "http://pepe.cloudhub.io");
    assertEquals("http://pepe.cloudhub.io/api/", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/api/"));

    setProperty(FULL_DOMAIN, "http://pepe.cloudhub.io/");
    assertEquals("http://pepe.cloudhub.io/api/", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/api/"));

    setProperty(FULL_DOMAIN, "pepe.cloudhub.io/");
    assertEquals("http://pepe.cloudhub.io/api/", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/api/"));

    setProperty(FULL_DOMAIN, "pepe.cloudhub.io");
    assertEquals("http://localhost:8081/", ramlHandler.getBaseUriReplacement("http://localhost:8081/"));
    assertEquals("http://pepe.cloudhub.io/", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/"));

    setProperty(FULL_DOMAIN, "http://pepe.cloudhub.io");
    assertEquals("http://pepe.cloudhub.io/", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/"));

    setProperty(FULL_DOMAIN, "http://pepe.cloudhub.io/");
    assertEquals("http://pepe.cloudhub.io/", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/"));

    setProperty(FULL_DOMAIN, "pepe.cloudhub.io/");
    assertEquals("http://pepe.cloudhub.io/", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/"));

    setProperty(FULL_DOMAIN, "pepe.cloudhub.io");
    assertEquals("http://localhost:8081", ramlHandler.getBaseUriReplacement("http://localhost:8081"));
    assertEquals("http://pepe.cloudhub.io", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081"));

    setProperty(FULL_DOMAIN, "http://pepe.cloudhub.io");
    assertEquals("http://pepe.cloudhub.io", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081"));

    setProperty(FULL_DOMAIN, "http://pepe.cloudhub.io/");
    assertEquals("http://pepe.cloudhub.io/", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081"));

    setProperty(FULL_DOMAIN, "pepe.cloudhub.io/");
    assertEquals("http://pepe.cloudhub.io/", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081"));

    setProperty(FULL_DOMAIN, "pepe.cloudhub.io/api");
    assertEquals("http://pepe.cloudhub.io/api", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081"));

    setProperty(FULL_DOMAIN, "http://pepe.cloudhub.io/api");
    assertEquals("http://pepe.cloudhub.io/api", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081"));

    setProperty(FULL_DOMAIN, "http://pepe.cloudhub.io/api");
    assertEquals("http://pepe.cloudhub.io/api", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081"));

    setProperty(FULL_DOMAIN, "pepe.cloudhub.io/api");
    assertEquals("http://pepe.cloudhub.io/api", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081"));
  }

  @Test
  public void consoleUriReplacementTest() {
    assertEquals("http://localhost:8081/console", getBaseUriReplacement("http://localhost:8081/console"));
    assertEquals("http://localhost:8081/console/", getBaseUriReplacement("http://localhost:8081/console/"));

    setProperty(FULL_DOMAIN, "http://aamura.cloudhub.io/api");
    assertEquals("http://aamura.cloudhub.io/api/console", getBaseUriReplacement("http://0.0.0.0:8081/console"));
    assertEquals("http://aamura.cloudhub.io/api/console/", getBaseUriReplacement("http://0.0.0.0:8081/console/"));

    setProperty(FULL_DOMAIN, "http://aamura.cloudhub.io/api/");
    assertEquals("http://aamura.cloudhub.io/api/console", getBaseUriReplacement("http://0.0.0.0:8081/console"));
    assertEquals("http://aamura.cloudhub.io/api/console/", getBaseUriReplacement("http://0.0.0.0:8081/console/"));

    setProperty(FULL_DOMAIN, "https://aamura.cloudhub.io/api");
    assertEquals("https://aamura.cloudhub.io/api/console", getBaseUriReplacement("http://0.0.0.0:8081/console"));
    assertEquals("https://aamura.cloudhub.io/api/console/", getBaseUriReplacement("http://0.0.0.0:8081/console/"));

    setProperty(FULL_DOMAIN, "https://aamura.cloudhub.io/api/");
    assertEquals("https://aamura.cloudhub.io/api/console", getBaseUriReplacement("http://0.0.0.0:8081/console"));
    assertEquals("https://aamura.cloudhub.io/api/console/", getBaseUriReplacement("http://0.0.0.0:8081/console/"));

    setProperty(FULL_DOMAIN, "https://aamura.cloudhub.io/api/v1");
    assertEquals("https://aamura.cloudhub.io/api/v1/console", getBaseUriReplacement("http://0.0.0.0:8081/console"));
    assertEquals("https://aamura.cloudhub.io/api/v1/console/", getBaseUriReplacement("http://0.0.0.0:8081/console/"));

    setProperty(FULL_DOMAIN, "aamura.cloudhub.io/api/v1");
    assertEquals("https://aamura.cloudhub.io/api/v1/console", getBaseUriReplacement("https://0.0.0.0:8081/console"));
    assertEquals("https://aamura.cloudhub.io/api/v1/console/", getBaseUriReplacement("https://0.0.0.0:8081/console/"));

    assertEquals("http://aamura.cloudhub.io/api/v1/console", getBaseUriReplacement("http://0.0.0.0:8081/console"));
    assertEquals("http://aamura.cloudhub.io/api/v1/console/", getBaseUriReplacement("http://0.0.0.0:8081/console/"));
  }

  @Test
  public void withoutSystemProperty() {
    assertEquals("http://localhost:48518/api/", getBaseUriReplacement("http://0.0.0.0:48518/api/"));
    assertEquals("http://localhost:48518/api/", getBaseUriReplacement("http://localhost:48518/api/"));
    assertNull(getBaseUriReplacement(null));

  }

  @Test
  public void replaceHostWithIncomingRequestHost() {
    assertEquals("http://localhost:48518/api", replaceHostInURL("http://0.0.0.0:48518/api", "http://localhost:48518"));
    assertEquals("http://localhost:48518/api/", replaceHostInURL("http://0.0.0.0:48518/api/", "http://localhost:48518"));
    assertEquals("http://localhost:48518/api/", replaceHostInURL("http://0.0.0.0:48518/api/", "localhost:48518"));
    assertEquals("https://127.0.0.1:48518/api/", replaceHostInURL("https://0.0.0.0:48518/api/", "https://127.0.0.1:48518"));
    assertEquals("http://192.168.0.196:48518/api/", replaceHostInURL("http://0.0.0.0:48518/api/", "http://192.168.0.196:48518"));
    assertEquals("https://192.168.0.196:48518/api/",
                 replaceHostInURL("https://0.0.0.0:48518/api/", "https://192.168.0.196:48518"));
    assertEquals("https://192.168.0.196:48518/api/", replaceHostInURL("https://0.0.0.0:48518/api/", "192.168.0.196:48518"));
  }

  @Test
  public void replaceHostWithSystemProperty() {
    // using host from system property, instead from incoming request
    setProperty(FULL_DOMAIN, "aamura.cloudhub.io/v1");
    assertEquals("https://aamura.cloudhub.io/v1/api/", replaceHostInURL("https://0.0.0.0:48518/api/", "192.168.0.196:48518"));
    setProperty(FULL_DOMAIN, "https://aamura.cloudhub.io/v1");
    assertEquals("https://aamura.cloudhub.io/v1/api/", replaceHostInURL("https://0.0.0.0:48518/api/", "192.168.0.196:48518"));
  }

  @After
  public void after() {
    clearProperty(FULL_DOMAIN);
  }

}
