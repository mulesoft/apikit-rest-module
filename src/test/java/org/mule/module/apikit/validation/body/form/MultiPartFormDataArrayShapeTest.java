/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mule.module.apikit.api.exception.InvalidFormParameterException;
import org.mule.module.apikit.api.exception.MuleRestException;
import org.mule.module.apikit.validation.TestRestRequestValidator;
import org.mule.parser.service.ParserMode;

import java.util.Arrays;
import java.util.Collection;
import java.util.OptionalLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

@RunWith(Parameterized.class)
public class MultiPartFormDataArrayShapeTest extends AbstractMultipartRequestValidatorTestCase {

    @Parameterized.Parameter
    public ParserMode parser;

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {ParserMode.AMF},
            {ParserMode.RAML}
        });
    }

    @Test
    public void minItemsValidationTest() throws MuleRestException {
        TestRestRequestValidator testRestRequestValidator = multipartTestBuilder
                .withApiLocation("unit/multipart-form-data/multipart-array-shape.raml")
                .withRelativePath("/test")
                .withParser(parser)
                .withTextPart("zipFiles", "\nsome.zip\n")
                .build();

        OptionalLong afterValidationBodyLength = testRestRequestValidator
                .validateRequest()
                .getBody()
                .getPayloadAsTypedValue().getByteLength();

        assertEquals(testRestRequestValidator.getRequestBodyLength(), afterValidationBodyLength);
    }

    @Test
    public void maxItemsValidationTest() throws MuleRestException {
        TestRestRequestValidator testRestRequestValidator = multipartTestBuilder
                .withApiLocation("unit/multipart-form-data/multipart-array-shape.raml")
                .withRelativePath("/multipart-upload")
                .withParser(parser)
                .withTextPart("zipFiles", "\nsome.zip\n")
                .withTextPart("zipFiles", "\nother.zip\n")
                .withTextPart("Attachments", "\none.txt\n")
                .withTextPart("Attachments", "\ntwo.txt\n")
                .build();

        OptionalLong afterValidationBodyLength = testRestRequestValidator
                .validateRequest()
                .getBody()
                .getPayloadAsTypedValue().getByteLength();

        assertEquals(testRestRequestValidator.getRequestBodyLength(), afterValidationBodyLength);
    }

    @Test
    public void minItemsValidationTestFailure() throws MuleRestException {
        TestRestRequestValidator testRestRequestValidator = multipartTestBuilder
                .withApiLocation("unit/multipart-form-data/multipart-array-shape.raml")
                .withRelativePath("/multipart-upload")
                .withParser(parser)
                .withTextPart("zipFiles", "\nsome.zip\n")
                .withTextPart("zipFiles", "\nother.zip\n")
                .withTextPart("Attachments", "\none.txt\n")
                .build();
        InvalidFormParameterException invalidFormParameterException = assertThrows(InvalidFormParameterException.class, () -> testRestRequestValidator
                .validateRequest()
                .getBody()
                .getPayloadAsTypedValue().getByteLength());
        assertEquals("parameter does not comply with minItems for Attachments", invalidFormParameterException.getMessage());
    }

    @Test
    public void minItemsValidationTestRequiredFieldAbsentFailure() throws MuleRestException {
        TestRestRequestValidator testRestRequestValidator = multipartTestBuilder
                .withApiLocation("unit/multipart-form-data/multipart-array-shape.raml")
                .withRelativePath("/multipart-upload")
                .withParser(parser)
                .withTextPart("zipFiles", "\nsome.zip\n")
                .withTextPart("Attachments", "\none.txt\n")
                .withTextPart("Attachments", "\ntwo.txt\n")
                .build();
        InvalidFormParameterException invalidFormParameterException = assertThrows(InvalidFormParameterException.class, () -> testRestRequestValidator
                .validateRequest()
                .getBody()
                .getPayloadAsTypedValue().getByteLength());
        assertEquals("parameter does not comply with minItems for zipFiles", invalidFormParameterException.getMessage());
    }

    @Test
    public void maxItemsValidationTestFailure2() throws MuleRestException {
        TestRestRequestValidator testRestRequestValidator = multipartTestBuilder
                .withApiLocation("unit/multipart-form-data/multipart-array-shape.raml")
                .withRelativePath("/multipart-upload")
                .withParser(parser)
                .withTextPart("zipFiles", "\nsome.zip\n")
                .withTextPart("Attachments", "\none.txt\n")
                .withTextPart("Attachments", "\ntwo.txt\n")
                .withTextPart("Attachments", "\nthree.txt\n")
                .withTextPart("Attachments", "\nfour.txt\n")
                .build();
        InvalidFormParameterException invalidFormParameterException = assertThrows(InvalidFormParameterException.class, () -> testRestRequestValidator
                .validateRequest()
                .getBody()
                .getPayloadAsTypedValue().getByteLength());
        assertEquals("parameter does not comply with maxItems for Attachments", invalidFormParameterException.getMessage());
    }

}
