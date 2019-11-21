/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.uri;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.mule.module.apikit.uri.TokenOperatorDX.Operator;

public class TokenOperatorDXTestCase {

  @Test(expected = URITemplateSyntaxException.class)
  public void parseInvalidExpression() {
    TokenOperatorDX.parse("a");
  }

  @Test
  public void queryParameter() {
    TokenOperatorDX tokenOperator = TokenOperatorDX.parse("{?x,y}");
    assertThat(tokenOperator.operator(), equalTo(Operator.QUERY_PARAMETER));
    assertThat(tokenOperator.isResolvable(), equalTo(true));
  }

  @Test
  public void pathParameter() {
    TokenOperatorDX tokenOperator = TokenOperatorDX.parse("{;x,y,empty}");
    assertThat(tokenOperator.operator(), equalTo(Operator.PATH_PARAMETER));
    assertThat(tokenOperator.isResolvable(), equalTo(true));
  }

  @Test
  public void pathSegment() {
    TokenOperatorDX tokenOperator = TokenOperatorDX.parse("{/list,x}");
    assertThat(tokenOperator.operator(), equalTo(Operator.PATH_SEGMENT));
    assertThat(tokenOperator.isResolvable(), equalTo(true));
  }

  @Test
  public void uriInsert() {
    TokenOperatorDX tokenOperator = TokenOperatorDX.parse("{+test}");
    assertThat(tokenOperator.operator(), equalTo(Operator.URI_INSERT));
    assertThat(tokenOperator.isResolvable(), equalTo(true));
  }

  @Test
  public void substitution() {
    TokenOperatorDX tokenOperator = TokenOperatorDX.parse("test");
    assertThat(tokenOperator.operator(), equalTo(Operator.SUBSTITUTION));
    assertThat(tokenOperator.isResolvable(), equalTo(true));
  }
}
