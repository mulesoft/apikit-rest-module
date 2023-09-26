package org.mule.module.apikit.validation.body.form;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mule.parser.service.ParserMode;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public abstract class AbstractMultipartRequestValidatorTestCase {

  protected MultipartTestBuilder multipartTestBuilder;

  @Parameterized.Parameter(0)
  public ParserMode parser;

  @Parameterized.Parameters(name = "{0}")
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
      {ParserMode.AMF},
      {ParserMode.RAML}
    });
  }

  @Before
  public void setup() {
    this.multipartTestBuilder = new MultipartTestBuilder();
    multipartTestBuilder.withParser(parser);
  }
}
