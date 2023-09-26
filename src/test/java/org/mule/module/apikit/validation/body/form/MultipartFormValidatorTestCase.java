package org.mule.module.apikit.validation.body.form;

import org.junit.Test;
import org.mule.module.apikit.api.exception.MuleRestException;

public class MultipartFormValidatorTestCase extends AbstractMultipartRequestValidatorTestCase{

  @Test
  public void test() throws MuleRestException {
    multipartTestBuilder
      .withApiLocation("munit/body/form/multipart-form-array/sample-system-api.raml")
      .withRelativePath("/students/101")
      .withTextPart("Details", "[{ \"name\": \"class\", \"value\": \"8th\" }, { \"name\": \"section\", \"value\": \"3A\" }, { \"name\": \"DOB\", \"value\": \"08/28/1970\"} ]")
      .build()
      .validateRequest();
  }

}
