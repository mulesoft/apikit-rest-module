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
import org.mule.module.apikit.api.RamlHandler;
import org.mule.apikit.model.ApiSpecification;
import org.mule.runtime.api.exception.ErrorTypeRepository;

import java.util.Arrays;
import java.util.Collection;
import java.util.OptionalLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

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
        System.out.println("=== Debugging RAML Parser Validation ===");

    
//     // 1. Debug RAML loading and parsing
//     RamlHandler ramlHandler = new RamlHandler(null, "unit/multipart-form-data/multipart-array-shape.raml", false, null, parser);
    
//     // Verify RAML loading
//     System.out.println("1. Parser Mode: " + parser);
//     System.out.println("2. RAML Handler created: " + (ramlHandler != null));

//     ApiSpecification apiSpec = ramlHandler.getApi();
//     System.out.println("5. API Specification loaded: " + (apiSpec != null));
    
//     // Get API specification
   

        // 2. Build and validate request
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

        System.out.println("7. Request built with " + testRestRequestValidator.getRequestBodyLength() + " bytes");

        // 3. Try validation with detailed error handling
        try {
            System.out.println("8. Starting validation...");
            testRestRequestValidator
                .validateRequest()
                .getBody()
                .getPayloadAsTypedValue()
                .getByteLength();
            System.out.println("9. Validation completed without exception (this is unexpected)");
        } catch (InvalidFormParameterException e) {
            System.out.println("9. Caught expected InvalidFormParameterException: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.out.println("9. Caught unexpected exception: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Unexpected exception", e);
        }

        // 4. If we get here, no exception was thrown
        System.out.println("7. No exception was thrown (this is the problem)");
        fail("Expected InvalidFormParameterException was not thrown");

        InvalidFormParameterException invalidFormParameterException = assertThrows(InvalidFormParameterException.class, () -> testRestRequestValidator
        .validateRequest()
        .getBody()
        .getPayloadAsTypedValue().getByteLength());
assertEquals("parameter does not comply with maxItems for Attachments", invalidFormParameterException.getMessage());
    }

}
