/* Generated By:JJTree: Do not edit this line. ExpNamedParameter.java Version 7.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=Exp,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package io.agrest.exp.parser;

public
class ExpNamedParameter extends SimpleNode {
  public ExpNamedParameter(int id) {
    super(id);
  }

  public ExpNamedParameter(AgExpressionParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public <T> T jjtAccept(AgExpressionParserVisitor<T> visitor, T data) {

    return
    visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=0f846793afdcf903f407ba56845fbc51 (do not edit this line) */
