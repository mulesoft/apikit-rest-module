/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.ApiVendor;
import org.mule.module.apikit.api.RamlHandler;
import org.mule.parser.service.ParserMode;
import org.mule.parser.service.result.DefaultParsingIssue;
import org.mule.parser.service.result.ParsingIssue;
import org.mule.parser.service.result.UnsupportedParsingIssue;
import org.mule.runtime.core.api.MuleContext;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mule.apikit.ApiType.AMF;
import static org.mule.apikit.ApiType.RAML;
import static org.mule.parser.service.ParserMode.AUTO;

public class RamlHandlerTestCase {

  private static final String UNSUPPORTED_FEATURE_CAUSE = "Unsupported Feature Cause";
  private static final String TEST_CAUSE = "Test Cause";
  private static MuleContext muleContext;

  @BeforeClass
  public static void beforeAll() {
    muleContext = mock(MuleContext.class);
    when(muleContext.getExecutionClassLoader()).thenReturn(Thread.currentThread().getContextClassLoader());
  }

  @Test
  public void apiVendorForRaml08() {
    String ramlLocation = "unit/raml-handler/simple08.raml";
    String apiServer = "unused";
    RamlHandler handler = createRamlHandler(ramlLocation, true);
    handler.setApiServer(apiServer);
    assertTrue(handler.getApiVendor().equals(ApiVendor.RAML_08));
  }

  @Test
  public void isParserV2TrueUsingRaml10() {
    String ramlLocation = "unit/raml-handler/simple10.raml";
    String apiServer = "unused";
    RamlHandler handler = createRamlHandler(ramlLocation);
    handler.setApiServer(apiServer);
    assertTrue(handler.isParserV2());
    assertTrue(handler.getApiVendor().equals(ApiVendor.RAML_10));
  }

  @Test
  public void addLocalHostAsServerWhenIsNotDefined() {
    String ramlLocation = "unit/raml-handler/simple10.raml";
    boolean keepRamlBaseUri = false;
    RamlHandler handler = createRamlHandler(ramlLocation, keepRamlBaseUri);
    handler.setApiServer("localhost:8081/");
    String rootRaml = handler.getAMFModel();
    assertThat(rootRaml, containsString("localhost:8081/"));
  }

  @Test
  public void streamAMFModelReplacingUrl() throws IOException {
    RamlHandler handler = createRamlHandler("unit/raml-handler/simple10.raml", false);
    PipedOutputStream pipedOutputStream = new PipedOutputStream();
    PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);
    Thread thread = new Thread(() -> handler.writeAMFModel("http://google.com", pipedOutputStream));
    thread.start();
    String model = IOUtils.toString(pipedInputStream);
    assertThat(model, containsString("http://google.com"));
  }

  @Test
  public void getRamlV2KeepRamlBaseUriTrue() {
    String ramlLocation = "unit/raml-handler/simple10-with-example.raml";
    boolean keepRamlBaseUri = true;
    String apiServer = "http://www.newBaseUri.com";
    RamlHandler handler = createRamlHandler(ramlLocation, keepRamlBaseUri);
    handler.setApiServer(apiServer);
    String rootRaml = handler.getRamlV2("unit/raml-handler/?raml");
    assertTrue(rootRaml.contains("RAML 1.0"));
    assertTrue(!rootRaml.contains(apiServer));
    assertTrue(rootRaml.contains("baseUri: http://localhost/myapi"));
  }



  @Test
  public void getRamlV2KeepRamlBaseUriFalse() {
    String ramlLocation = "unit/raml-handler/simple10-with-example.raml";// this.getClass().getResource("../../../../org/mule/module/apikit/simple-raml/simple10-with-example.raml").toString();
    String apiServer = "http://pepe.com";
    RamlHandler handler = createRamlHandler(ramlLocation, false);
    handler.setApiServer(apiServer);

    String ramlV1 = handler.getRamlV1();
    assertTrue(ramlV1.contains("baseUri: " + apiServer));

    String ramlV2 = handler.getRamlV2("unit/raml-handler/?raml");
    assertTrue(ramlV2.contains("baseUri: " + apiServer));

    String ramlAmf = handler.getAMFModel();
    assertTrue(ramlAmf.contains("baseUri: " + apiServer));
  }

  @Test
  public void getRamlV2Example() {
    String ramlLocation = "unit/raml-handler/simple10-with-example.raml";
    String apiServer = "unused";
    RamlHandler handler = createRamlHandler(ramlLocation);
    handler.setApiServer(apiServer);
    assertTrue(handler.getRamlV2("unit/raml-handler/example.json/?raml").contains("{\"name\":\"jane\"}"));
  }

  @Test
  public void testInitializationUsingAUTO() {
    RamlHandler handler;

    final boolean keepRamlBaseUri = true;

    handler = createRamlHandler("unit/raml-handler/amf-only.raml", keepRamlBaseUri, ParserMode.AUTO);
    assertEquals(AMF, handler.getApi().getType());

    assertException("Invalid reference 'SomeTypo'",
                    () -> createRamlHandler("unit/raml-handler/failing-api.raml", keepRamlBaseUri, ParserMode.AUTO));
  }

  @Test
  public void testInitializationUsingAMF() {
    RamlHandler handler;

    final boolean keepRamlBaseUri = true;

    handler = createRamlHandler("unit/raml-handler/amf-only.raml", keepRamlBaseUri, ParserMode.AMF);
    assertEquals(AMF, handler.getApi().getType());

    assertException("Unresolved reference 'SomeTypo'",
                    () -> createRamlHandler("unit/raml-handler/failing-api.raml", keepRamlBaseUri, ParserMode.AMF));
  }

  @Test
  public void testInitializationUsingRAML() {
    RamlHandler handler;

    final boolean keepRamlBaseUri = true;

    handler = createRamlHandler("unit/raml-handler/raml-parser-only.raml", keepRamlBaseUri, ParserMode.RAML);
    assertEquals(RAML, handler.getApi().getType());

    assertException("Invalid reference 'SomeTypo'",
                    () -> createRamlHandler("unit/raml-handler/failing-api.raml", keepRamlBaseUri, ParserMode.RAML));
  }

  @Test
  public void ramlWithSpacesInPath() {
    RamlHandler handler = createRamlHandler("unit/space in path api/api.raml", true, ParserMode.RAML);
    ApiSpecification api = handler.getApi();
    assertEquals(RAML, api.getType());
    List<String> refs = api.getAllReferences();
    assertTrue(refs.stream().anyMatch(ref -> ref.endsWith("unit/space%20in%20path%20api/example.json")));
    assertTrue(refs.stream().anyMatch(ref -> ref.endsWith("unit/space%20in%20path%20api/more%20spaces/schema.json")));
  }

  @Test
  public void oas30WithUnsupportedFeatures() {
    assertNotNull(createRamlHandler("unit/raml-handler/oas30-api.yaml", true, ParserMode.AMF));
  }

  @Test
  public void testFilteringOutUnsupportedParsingIssue() throws Exception {
    RamlHandler ramlHandler = mock(RamlHandler.class);
    Class[] cArg = new Class[2];
    cArg[0] = List.class;
    cArg[1] = boolean.class;
    Method getFilteredParsingIssueStream = RamlHandler.class.getDeclaredMethod("getFilteredParsingIssueStream", cArg);
    getFilteredParsingIssueStream.setAccessible(true);
    List<ParsingIssue> parsingIssues =
        Arrays.asList(new DefaultParsingIssue(TEST_CAUSE), new UnsupportedParsingIssue(UNSUPPORTED_FEATURE_CAUSE));
    Stream<ParsingIssue> result = (Stream<ParsingIssue>) getFilteredParsingIssueStream.invoke(ramlHandler, parsingIssues, true);
    List<String> collectedResults = result.map(e -> e.cause()).collect(toList());
    assertEquals(1, collectedResults.size());
    assertEquals(TEST_CAUSE, collectedResults.get(0));
    result = (Stream<ParsingIssue>) getFilteredParsingIssueStream.invoke(ramlHandler, parsingIssues, false);
    collectedResults = result.map(e -> e.cause()).collect(toList());
    assertEquals(2, collectedResults.size());
    assertEquals(TEST_CAUSE, collectedResults.get(0));
    assertEquals(UNSUPPORTED_FEATURE_CAUSE, collectedResults.get(1));
  }

  private <A extends Exception, B> void assertException(String message, Supplier<B> supplier) {
    try {
      supplier.get();
      fail("an exception was expected");
    } catch (Exception e) {
      assertThat(e.getMessage(), containsString(message));
    }
  }

  private RamlHandler createRamlHandler(String ramlPath) {
    return createRamlHandler(ramlPath, true, AUTO);
  }

  private RamlHandler createRamlHandler(String ramlPath, boolean keepRamlBaseUri) {
    return createRamlHandler(ramlPath, keepRamlBaseUri, AUTO);
  }

  private RamlHandler createRamlHandler(String ramlPath, boolean keepRamlBaseUri, ParserMode parser) {
    return createRamlHandler(ramlPath, keepRamlBaseUri, parser, false);
  }

  private RamlHandler createRamlHandler(String ramlPath, boolean keepRamlBaseUri, ParserMode parser,
                                        boolean filterUnsupportedLogging) {
    try {
      return new RamlHandler(null, ramlPath, keepRamlBaseUri, muleContext.getErrorTypeRepository(), parser,
                             filterUnsupportedLogging);
    } catch (IOException e) {
      throw new RuntimeException("Error creating RamlHandler", e);
    }
  }
}
