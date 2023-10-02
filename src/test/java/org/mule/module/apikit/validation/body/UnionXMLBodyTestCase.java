/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mule.module.apikit.api.exception.BadRequestException;
import org.mule.module.apikit.api.exception.MuleRestException;
import org.mule.module.apikit.validation.AbstractRequestValidatorTestCase;
import org.mule.module.apikit.validation.TestRestRequestValidatorBuilder;
import org.mule.runtime.api.util.MultiMap;

import java.io.ByteArrayInputStream;
import java.util.Collections;

/**
 * This test class is mainly intend to show the difference between the validations of union RAML types from the AMF and JAVA RAML
 * parsers. The recommendation is not to use the RAML JAVA parser since it contains some bugs and it is deprecated.
 */
public class UnionXMLBodyTestCase extends AbstractRequestValidatorTestCase {

  public static final String AMF_VALIDATE_UNION_PROPERTY = "amf.plugins.xml.validateUnion";
  private TestRestRequestValidatorBuilder testBuilder;

  @Before
  public void before() {
    System.setProperty(AMF_VALIDATE_UNION_PROPERTY, "true");
    testBuilder = testRestRequestValidatorBuilder
        .withApiLocation("unit/validation/xml-union-types-api.raml")
        .withMethod("POST")
        .withHeaders(new MultiMap<>(Collections.singletonMap("Content-Type", "application/xml")));
  }

  /**
   * When the type is defined at the types facet, the root element takes the name for the respective type.
   * <p>
   * NOTE: The RAML parser will only allow the first element of the union
   */
  @Test
  public void xmlUnionTest() throws MuleRestException {
    testBuilder
        .withRelativePath("/xmlUnion");

    if (parser.name().equals("RAML")) {
      String validForRamlParser = "<unionElement><message1>test</message1></unionElement>";
      testBuilder
          .withBody(new ByteArrayInputStream(validForRamlParser.getBytes()))
          .build()
          .validateRequest();
    } else {
      String validForAmfParser = "<unionElement><message1>test</message1></unionElement>";
      testBuilder
          .withBody(new ByteArrayInputStream(validForAmfParser.getBytes()))
          .build()
          .validateRequest();
    }
  }

  /**
   * When the union is defined inline RAML Java parser will set root as the root tag name and AMF will use model.
   * <p>
   * NOTE: The RAML parser will only allow the first element of the union
   */
  @Test
  public void xmlUnionInlineTest() throws MuleRestException {
    testBuilder
        .withRelativePath("/xmlUnionInline");

    if (parser.name().equals("RAML")) {
      String validForRamlParser = "<root><message1>test</message1></root>";
      testBuilder
          .withBody(new ByteArrayInputStream(validForRamlParser.getBytes()))
          .build()
          .validateRequest();
    } else {
      String validForAmfParser = "<model><message2>test</message2></model>";
      testBuilder
          .withBody(new ByteArrayInputStream(validForAmfParser.getBytes()))
          .build()
          .validateRequest();
    }
  }

  /**
   * When using serialization properties RAML Java parser will ignore them while AMF set them properly.
   * <p>
   * NOTE: The RAML parser will only allow the first element of the union
   */
  @Test
  public void xmlUnionInlineWithSerializationPropertiesTest() throws MuleRestException {
    testBuilder
        .withRelativePath("/xmlUnionInlineWithSerializationProperties");

    if (parser.name().equals("RAML")) {
      String validForRamlParser = "<root><message1>test</message1></root>";
      testBuilder
          .withBody(new ByteArrayInputStream(validForRamlParser.getBytes()))
          .build()
          .validateRequest();
    } else {
      String validForAmfParser = "<someName xmlns=\"http://test.namespace.com\"><message1>test</message1></someName>";
      testBuilder
          .withBody(new ByteArrayInputStream(validForAmfParser.getBytes()))
          .build()
          .validateRequest();
    }
  }

  /**
   * When sending any other element but the first element RAML Java parser will fail, AMF parser validates properly
   */
  @Test
  public void invalidXmlUnionTest() {
    testBuilder
        .withRelativePath("/xmlUnion");

    if (parser.name().equals("RAML")) {
      String validForRamlParser = "<unionElement><message2>test</message2></unionElement>";
      testBuilder
          .withBody(new ByteArrayInputStream(validForRamlParser.getBytes()))
          .build()
          .assertThrows(BadRequestException.class, "One of '{\"http://validationnamespace.raml.org\":message1}' is expected.");
    } else {
      String validForAmfParser = "<unionElement><message3>test</message3></unionElement>";
      testBuilder
          .withBody(new ByteArrayInputStream(validForAmfParser.getBytes()))
          .build()
          .assertThrows(BadRequestException.class,
                        "One of '{\"http://amf-xml-extension/\":message1, \"http://amf-xml-extension/\":message2}' is expected");
    }
  }


  @After
  public void after() {
    System.clearProperty(AMF_VALIDATE_UNION_PROPERTY);
  }
}
