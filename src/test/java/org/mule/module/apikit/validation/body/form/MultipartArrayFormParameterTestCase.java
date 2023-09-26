package org.mule.module.apikit.validation.body.form;

import org.junit.Test;
import org.mule.module.apikit.api.exception.MuleRestException;

public class MultipartArrayFormParameterTestCase extends AbstractMultipartRequestValidatorTestCase{

  @Test
  public void arrayAsJsonTest() throws MuleRestException {
    multipartTestBuilder
      .withApiLocation("munit/body/form/multipart-form-array/sample-system-api.raml")
      .withRelativePath("/students/101")
      .withTextPart("Details", "[{ \"name\": \"class\", \"value\": \"8th\" }, { \"name\": \"section\", \"value\": \"3A\" }, { \"name\": \"DOB\", \"value\": \"08/28/1970\"} ]")
      .build()
      .validateRequest();
  }


  @Test
  public void arrayAsRepeatedFormParamTest() throws MuleRestException {
    multipartTestBuilder
      .withApiLocation("munit/body/form/multipart-form-array/sample-system-api.raml")
      .withRelativePath("/students/101")
      .withTextPart("Details", "{ \"name\": \"class\", \"value\": \"8th\" }")
      .withTextPart("Details", "{ \"name\": \"section\", \"value\": \"3A\" }")
      .build()
      .validateRequest();
  }



}
