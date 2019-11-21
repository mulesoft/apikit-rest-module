/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.uri;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A token based on the operators defined in the latest draft.
 * <p/>
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
 * @version 5 November 2009
 * @see <a href="http://code.google.com/p/uri-templates/source/browse/trunk/spec/draft-gregorio-uritemplate.xml">URI
 *      Template Library draft specifications at Google Code</a>
 */
public class TokenOperatorDX extends TokenBase implements Matchable {

  /**
   * The list of operators currently supported.
   */
  public enum Operator {

    /**
     * The '?' operator for query parameters.
     * <p/>
     * Example:
     * <p/>
     * <pre>
     *  undef = null;
     *  empty = &quot;&quot;;
     *  x     = &quot;1024&quot;;
     *  y     = &quot;768&quot;;
     *
     * {?x,y}                    ?x=1024&amp;y=768
     * {?x,y,empty}              ?x=1024&amp;y=768&amp;empty=
     * {?x,y,undef}              ?x=1024&amp;y=768
     * </pre>
     */
    QUERY_PARAMETER('?') {

      @Override
      boolean isResolvable(List<Variable> arg0) {
        return true;
      }

      @Override
      boolean resolve(List<Variable> vars, String value, Map<Variable, Object> values) {
        for (Variable var : vars) {
          Pattern p = Pattern.compile("(?<=[&?]" + var.namePatternString() + "=)([^&#]*)");
          Matcher m = p.matcher(value);
          while (m.find()) {
            values.put(var, m.group());
          }
        }
        return true;
      }

      @Override
      Pattern pattern(List<Variable> vars) {
        StringBuffer pattern = new StringBuffer();
        pattern.append("\\?(");
        for (Variable var : vars) {
          pattern.append('(');
          pattern.append(var.namePatternString());
          pattern.append("=[^&#]*)|");
        }
        pattern.append("&)*");
        return Pattern.compile(pattern.toString());
      }
    },

    /**
     * The ';' operator for path parameters.
     * <p/>
     * Example:
     * <p/>
     * <pre>
     *  undef = null;
     *  empty = &quot;&quot;;
     *  x     = &quot;1024&quot;;
     *  y     = &quot;768&quot;;
     *
     * {;x,y}                    ;x=1024;y=768
     * {;x,y,empty}              ;x=1024;y=768;empty
     * {;x,y,undef}              ;x=1024;y=768
     * </pre>
     */
    PATH_PARAMETER(';') {

      @Override
      boolean isResolvable(List<Variable> vars) {
        return true;
      }

      @Override
      boolean resolve(List<Variable> vars, String value, Map<Variable, Object> values) {
        for (Variable var : vars) {
          Pattern p = Pattern.compile("(?<=;" + var.namePatternString() + "=)([^;/?#]*)");
          Matcher m = p.matcher(value);
          while (m.find()) {
            values.put(var, m.group());
          }
        }
        return true;
      }

      @Override
      Pattern pattern(List<Variable> vars) {
        StringBuffer pattern = new StringBuffer();
        pattern.append("(?:");
        for (Variable var : vars) {
          pattern.append("(?:;");
          pattern.append(var.namePatternString());
          pattern.append("=[^;/?#]*)|");
        }
        pattern.append(";)*");
        return Pattern.compile(pattern.toString());
      }
    },

    /**
     * The '/' operator for path segments.
     * <p/>
     * Example:
     * <p/>
     * <pre>
     *  list  = [ &quot;val1&quot;, &quot;val2&quot;, &quot;val3&quot; ];
     *  x     = &quot;1024&quot;;
     *
     *  {/list,x}                 /val1/val2/val3/1024
     * </pre>
     */
    PATH_SEGMENT('/') {

      @Override
      boolean isResolvable(List<Variable> arg0) {
        return true;
      }

      @Override
      boolean resolve(List<Variable> vars, String value, Map<Variable, Object> values) {
        if (vars.size() != 1) {
          throw new UnsupportedOperationException("Operator + cannot be resolved with multiple variables.");
        }
        values.put(vars.get(0), URICoder.decode(value));
        return true;
      }

      @Override
      Pattern pattern(List<Variable> vars) {
        return Pattern.compile("(?:/[^/?#]*)*");
      }
    },

    /**
     * The '+' operator for URI inserts.
     * <p/>
     * Example:
     * <p/>
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
    },

    /**
     * The substitution operator is only used to aggregate variables.
     */
    SUBSTITUTION(' ') {

      @Override
      boolean resolve(List<Variable> vars, String value, Map<Variable, Object> values) {
        // TODO: should we return false instead??
        // TODO: could we somewhat support a comma separated list of values?
        if (vars.size() != 1) {
          throw new UnsupportedOperationException("Operator cannot be resolved with multiple variables.");
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
        return Pattern.compile("[^;/?#,&]*");
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
    Operator(char c) {
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
     * @param vars   The variables for the operator.
     */
    abstract Pattern pattern(List<Variable> vars);

    /**
     * Returns the map of the string to values given  the specified data.
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
   * Creates a new operator token.
   *
   * @param op   The operator to use.
   * @param vars The variables for this operator.
   * @throws NullPointerException If any of the argument is <code>null</code>.
   */
  public TokenOperatorDX(Operator op, List<Variable> vars) throws NullPointerException {
    super(toExpression(op, vars));
    if (op == null || vars == null) {
      throw new NullPointerException("The operator must have a value");
    }
    this._operator = op;
    this._vars = vars;
    this._pattern = op.pattern(vars);
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
   * Returns the operator if it is defined in this class.
   *
   * @param c The character representation of the operator.
   * @return The corresponding operator instance.
   */
  public static Operator toOperator(char c) {
    for (Operator o : Operator.values()) {
      if (o.character() == c) {
        return o;
      }
    }
    // default on simple substitution
    return Operator.SUBSTITUTION;
  }

  /**
   * Parses the specified string and returns the corresponding token.
   * <p/>
   * This method accepts both the raw expression or the expression wrapped in curly brackets.
   *
   * @param exp The expression to get.
   * @return The corresponding token.
   * @throws URITemplateSyntaxException If the string cannot be parsed as a valid
   */
  public static TokenOperatorDX parse(String exp) throws URITemplateSyntaxException {
    String sexp = strip(exp);
    if (sexp.length() < 2) {
      throw new URITemplateSyntaxException(exp, "Cannot produce a valid token operator.");
    }
    char c = sexp.charAt(0);
    Operator operator = TokenOperatorDX.toOperator(c);
    List<Variable> variables = toVariables(operator == Operator.SUBSTITUTION ? sexp : sexp.substring(1));
    return new TokenOperatorDX(operator, variables);
  }

  /**
   * Generate the expression corresponding to the specified operator, argument and variables.
   *
   * @param op   The operator.
   * @param arg  the argument.
   * @param vars The variables.
   */
  private static String toExpression(Operator op, List<Variable> vars) {
    StringBuffer exp = new StringBuffer();
    exp.append('{');
    exp.append(op.character());
    boolean first = true;
    for (Variable v : vars) {
      if (!first) {
        exp.append(',');
      }
      exp.append(v.toString());
      first = false;
    }
    exp.append('}');
    return exp.toString();
  }

}
