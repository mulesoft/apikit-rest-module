/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.uri;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * A token based on the operators used in PageSeeder.
 * <p/>
 * <p>
 * This syntax borrows heavily from a suggestion made by Roy T. Fielding on the W3C URI list and regarding the URI Template draft
 * specification.
 * <p/>
 * 
 * <pre>
 *  instruction   = &quot;{&quot; [ operator ] variable-list &quot;}&quot;
 *  operator      = &quot;/&quot; / &quot;+&quot; / &quot;;&quot; / &quot;?&quot; / op-reserve
 *  variable-list =  varspec *( &quot;,&quot; varspec )
 *  varspec       =  [ var-type ] varname [ &quot;:&quot; prefix-len ] [ &quot;=&quot; default ]
 *  var-type      = &quot;@&quot; / &quot;%&quot; / type-reserve
 *  varname       = ALPHA *( ALPHA | DIGIT | &quot;_&quot; )
 *  prefix-len    = 1*DIGIT
 *  default       = *( unreserved / reserved )
 *  op-reserve    = &lt;anything else that isn't ALPHA or operator&gt;
 *  type-reserve  = &lt;anything else that isn't ALPHA, &quot;,&quot;, or operator&gt;
 * </pre>
 *
 * @author Christophe Lauret
 * @version 6 November 2009
 * @see <a href="http://lists.w3.org/Archives/Public/uri/2008Sep/0007.html">Re: URI Templates? from Roy T. Fielding on 2008-09-16
 *      (uri@w3.org)</a>
 */
public class TokenOperatorPS extends TokenBase implements Matchable {

  /**
   * The list of operators currently supported.
   */
  public enum Operator {

    /**
     * The '+' operator for URI inserts.
     * <p/>
     * Example:
     * <p/>
     * 
     * <pre>
     * empty = &quot;&quot;
     * path  = &quot;/foo/bar&quot;
     * x     = &quot;1024&quot;
     *
     *  {+path}/here              /foo/bar/here
     *  {+path,x}/here            /foo/bar,1024/here
     *  {+path}{x}/here           /foo/bar1024/here
     *  {+empty}/here             /here
     * </pre>
     */
    URI_INSERT('+') {

      @Override
      boolean resolve(List<Variable> vars, String value, Map<Variable, Object> values) {
        // TODO: should we return false instead??
        if (vars.size() != 1) {
          throw new UnsupportedOperationException("Operator + cannot be resolved with multiple variables.");
        }
        values.put(vars.get(0), URICoder.decode(value));
        return true;
      }

      @Override
      boolean isResolvable(List<Variable> vars) {
        return vars.size() == 1;
      }

      @Override
      Pattern pattern(List<Variable> vars) {
        return Pattern.compile("[^?#]*");
      }
    };

    /**
     * The character used to represent this operator.
     */
    private final char _c;

    /**
     * Creates a new operator.
     *
     * @param c The character used to represent this operator.
     */
    private Operator(char c) {
      this._c = c;
    }

    /**
     * Returns the character.
     *
     * @return The character used to represent this operator.
     */
    public char character() {
      return this._c;
    }

    /**
     * Indicates whether the operator can be resolved.
     *
     * @param vars The variables for the operator.
     */
    abstract boolean isResolvable(List<Variable> vars);

    /**
     * Returns the pattern for this operator given the specified list of variables.
     *
     * @param vars The variables for the operator.
     */
    abstract Pattern pattern(List<Variable> vars);

    /**
     * Returns the map of the string to values given the specified data.
     */
    abstract boolean resolve(List<Variable> vars, String value, Map<Variable, Object> values);

  }

  /**
   * The operator.
   */
  private Operator _operator;

  /**
   * The variables for this token.
   */
  private List<Variable> _vars;

  /**
   * The pattern for this token.
   */
  private Pattern _pattern;

  /**
   * Creates a new operator token for one variable only.
   *
   * @param op The operator to use.
   * @param var The variable for this operator.
   * @throws NullPointerException If any of the argument is <code>null</code>.
   */
  public TokenOperatorPS(Operator op, Variable var) throws NullPointerException {
    super(toExpression(op, var));
    if (op == null || var == null) {
      throw new NullPointerException("The operator must have a value");
    }
    this._operator = op;
    this._vars = new ArrayList<Variable>(1);
    this._vars.add(var);
    this._pattern = op.pattern(this._vars);
  }

  /**
   * Returns the operator part of this token.
   *
   * @return the operator.
   */
  public Operator operator() {
    return this._operator;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isResolvable() {
    return this._operator.isResolvable(this._vars);
  }

  /**
   * {@inheritDoc}
   */
  public boolean resolve(String expanded, Map<Variable, Object> values) {
    if (this.isResolvable()) {
      this._operator.resolve(this._vars, expanded, values);
      return true;
    } else {
      return false;
    }
  }

  /**
   * {@inheritDoc}
   */
  public boolean match(String part) {
    return this._pattern.matcher(part).matches();
  }

  /**
   * {@inheritDoc}
   */
  public Pattern pattern() {
    return this._pattern;
  }

  /**
   * Generate the expression corresponding to the specified operator and variable.
   *
   * @param op The operator.
   * @param var The variable.
   */
  private static String toExpression(Operator op, Variable var) {
    return "{" + op.character() + var.name() + '}';
  }
}
