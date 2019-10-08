/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.schema;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import javax.xml.validation.Schema;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import org.mule.module.apikit.Configuration;
import org.mule.module.apikit.api.RamlHandler;
import org.mule.module.apikit.api.exception.BadRequestException;
import org.mule.module.apikit.validation.body.schema.v1.RestXmlSchemaValidator;
import org.mule.apikit.model.Action;
import org.mule.apikit.model.MimeType;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.Resource;
import org.mule.runtime.api.exception.ErrorTypeRepository;
import org.mule.runtime.api.exception.TypedException;
import org.mule.runtime.api.message.ErrorType;

public class RestXMLSchemaValidatorTestCase {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private static final String xmlSchema = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>" +
      "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"" +
      " elementFormDefault=\"qualified\" xmlns=\"http://mulesoft.com/schemas/soccer\"" +
      " targetNamespace=\"http://mulesoft.com/schemas/soccer\">" +
      "<xs:element name=\"league\">" +
      "  <xs:complexType>" +
      "    <xs:sequence>" +
      "      <xs:element name=\"name\" type=\"xs:string\"/>" +
      "      <xs:element name=\"description\" type=\"xs:string\" minOccurs=\"0\"/>" +
      "    </xs:sequence>" +
      "  </xs:complexType>" +
      "</xs:element>" +
      "</xs:schema>";

  private static ApiSpecification api;

  @BeforeClass
  public static void mockApi() {
    api = Mockito.mock(ApiSpecification.class);

    Map<String, Object> compiledSchemaMap = new HashMap<>();
    Schema compiledSchema = org.raml.parser.visitor.SchemaCompiler.getInstance().compile(xmlSchema);
    compiledSchemaMap.put("scheme-xml", compiledSchema);
    when(api.getCompiledSchemas()).thenReturn(compiledSchemaMap);

    Map<String, String> schemaMap = new HashMap<>();
    schemaMap.put("scheme-xml", xmlSchema);
    when(api.getConsolidatedSchemas()).thenReturn(schemaMap);

    Map<String, MimeType> body = new HashMap<>();
    MimeType mimeType = Mockito.mock(MimeType.class);
    when(mimeType.getType()).thenReturn("application/xml");
    when(mimeType.getSchema()).thenReturn("scheme-xml");
    body.put("application/xml", mimeType);
    Action mockedAction = Mockito.mock(Action.class);
    when(mockedAction.getBody()).thenReturn(body);
    Resource mockedResource = Mockito.mock(Resource.class);
    when(mockedResource.getAction("POST")).thenReturn(mockedAction);
    when(api.getResource("/leagues")).thenReturn(mockedResource);
  }

  @Test
  public void validStringPayloadUsingParser() throws TypedException, ExecutionException, BadRequestException {
    String payload = "<league xmlns=\"http://mulesoft.com/schemas/soccer\"><name>MLS</name></league>";
    String schemaPath = "/leagues,POST,application/xml";

    Configuration config = new Configuration();
    RamlHandler ramlHandler = Mockito.mock(RamlHandler.class);
    ErrorTypeRepository errorTypeRepository = Mockito.mock(ErrorTypeRepository.class);
    when(ramlHandler.getApi()).thenReturn(api);
    config.setRamlHandler(ramlHandler);

    RestXmlSchemaValidator xmlValidator = new RestXmlSchemaValidator(config.getXmlSchema(schemaPath), errorTypeRepository, "application/xml");

    xmlValidator.validate(payload, null);
  }

  @Test(expected = TypedException.class)
  public void invalidStringPayloadUsingParser() throws TypedException, BadRequestException, ExecutionException {
    String payload = "<league xmlns=\"http://mulesoft.com/schemas/soccer\"><invalid>hello</invalid></league>";
    String schemaPath = "/leagues,POST,application/xml";

    Configuration config = new Configuration();
    RamlHandler ramlHandler = Mockito.mock(RamlHandler.class);
    when(ramlHandler.getApi()).thenReturn(api);
    config.setRamlHandler(ramlHandler);

    RestXmlSchemaValidator xmlValidator = new RestXmlSchemaValidator(config.getXmlSchema(schemaPath), null, "application/xml");
    xmlValidator.validate(payload, null);
  }

  @Test
  public void xxeAttackIsDisabled() throws TypedException, ExecutionException, BadRequestException {

    expectedException.expectMessage("An internal operation failed.");

    String payload = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
      + "<!DOCTYPE leagues [\n"
      + "<!ENTITY mls \"MLS\">"
      + "]>\n"
      + "<league xmlns=\"http://mulesoft.com/schemas/soccer\"><name>&mls;</name></league>";

    String schemaPath = "/leagues,POST,application/xml";

    Configuration config = new Configuration();
    RamlHandler ramlHandler = Mockito.mock(RamlHandler.class);
    ErrorTypeRepository errorTypeRepository = Mockito.mock(ErrorTypeRepository.class);
    ErrorType type = mock(ErrorType.class);
    when(errorTypeRepository.getErrorType(any())).thenReturn(Optional.of(type));
    when(ramlHandler.getApi()).thenReturn(api);
    config.setRamlHandler(ramlHandler);

    RestXmlSchemaValidator xmlValidator = new RestXmlSchemaValidator(config.getXmlSchema(schemaPath), errorTypeRepository, "application/xml");

    xmlValidator.validate(payload, null);
  }
}
