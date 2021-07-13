import absyn.*;

public class ShowTreeVisitor implements AbsynVisitor {

  final static int SPACES = 4;

  private void indent( int level ) {
    for( int i = 0; i < level * SPACES; i++ ) System.out.print( " " );
  }

  public void visit( ExpList expList, int level, boolean isAddr) {
    while( expList != null ) {
      expList.head.accept( this, level, isAddr);
      expList = expList.tail;
    }
  }

  public void visit( AssignExp exp, int level, boolean isAddr) {
    indent( level );
    System.out.println( "AssignExp:" );
    level++;
    exp.lhs.accept( this, level, isAddr);
    exp.rhs.accept( this, level, isAddr);
  }

  public void visit( IfExp exp, int level, boolean isAddr) {
    indent( level );
    System.out.println( "IfExp:" );
    level++;
    exp.ifpart.accept( this, level, isAddr);
    exp.thenpart.accept( this, level, isAddr);
    if (exp.elsepart != null ) {
      level--;
      indent( level );
      level++;
      System.out.println( "ElseExp:" );
      exp.elsepart.accept( this, level, isAddr);
    }
    //level++;
  }

  public void visit( IntExp exp, int level, boolean isAddr) {
    indent( level );
    System.out.println( "IntExp: " + exp.value );
  }

  public void visit( OpExp exp, int level, boolean isAddr) {
    indent( level );
    System.out.print( "OpExp:" );
    switch( exp.op ) {
      case OpExp.PLUS:
        System.out.println( " + " );
        break;
      case OpExp.MINUS:
        System.out.println( " - " );
        break;
      case OpExp.TIMES:
        System.out.println( " * " );
        break;
      case OpExp.OVER:
        System.out.println( " / " );
        break;
      case OpExp.EQ:
        System.out.println( " = " );
        break;
      case OpExp.EQUIV:
        System.out.println( " == " );
        break;
      case OpExp.NOTEQ:
        System.out.println( " != " );
        break;
      case OpExp.LT:
        System.out.println( " < " );
        break;
      case OpExp.LESSEQ:
        System.out.println( " <= " );
        break;
      case OpExp.GT:
        System.out.println( " > " );
        break;
      case OpExp.GREATEQ:
        System.out.println( " >= " );
        break;
      default:
        System.out.println( "Unrecognized operator at line " + exp.row + " and column " + exp.col);
    }
    level++;
    exp.left.accept( this, level, isAddr);
    exp.right.accept( this, level, isAddr);
  }

  public void visit( VarExp exp, int level, boolean isAddr) {
    indent( level );
    System.out.println( "VarExp: " + exp.name );
  }

  public void visit( ArrayDec exp, int level, boolean isAddr){
    indent( level );
    System.out.println( "ArrayDec: " + exp.getName(exp.typ) + ' ' + exp.name );

    // if(exp.name == null){
    //   System.out.println( "SimpleDec: " + exp.getName(exp.typ));
    // }
    // else{
    //   System.out.println( "SimpleDec: " + exp.getName(exp.typ) + ' ' + exp.name);
    // }
  }

  public void visit( CallExp exp, int level, boolean isAddr){
    indent( level );
    level++;
    System.out.println( "CallExp: " + exp.func );
    if(exp.args != null){
      exp.args.accept(this, level, isAddr);
    }
  }

  public void visit( CompoundExp exp, int level, boolean isAddr){
    indent( level );
    level++;
    System.out.println( "CompoundExp: " );
    if(exp.decs != null){
      exp.decs.accept(this,level, isAddr);
    }
    if(exp.exps != null){
      exp.exps.accept(this,level, isAddr);
    }
  }

  public void visit( DecList decList, int level, boolean isAddr){
    while( decList != null ) {
      decList.head.accept( this, level, isAddr);
      decList = decList.tail;
    }
  }

  public void visit( FunctionDec exp, int level, boolean isAddr){
    indent( level );
    level++;
    System.out.println( "FunctionDec: " + exp.getName(exp.result) + ' ' + exp.func);
    if(exp.params != null){
      exp.params.accept(this,level, isAddr);
    }
    if(exp.body != null){
      exp.body.accept(this,level, isAddr);
    }
  }

  public void visit( IndexVar exp, int level, boolean isAddr){
    indent( level );
    System.out.println( "IndexVar: " + exp.name);
  }

  public void visit( NameTy exp, int level, boolean isAddr){
    indent( level );
    System.out.println( "NameTy: " + exp.name);
  }

  public void visit( NilExp exp, int level, boolean isAddr){
    indent( level );
    System.out.println( "NilExp: ");
  }

  public void visit( ReturnExp exp, int level, boolean isAddr){
    indent( level );
    System.out.println( "ReturnExp: ");
    level++;
    if(exp.exp != null){
      exp.exp.accept(this,level, isAddr);
    }
  }

  public void visit( SimpleDec exp, int level, boolean isAddr){
    indent( level );
    if(exp.name == null){
      System.out.println( "SimpleDec: " + exp.getName(exp.typ));
    }
    else{
      System.out.println( "SimpleDec: " + exp.getName(exp.typ) + ' ' + exp.name);
    }
  }

  public void visit( SimpleVar exp, int level, boolean isAddr){
    indent( level );
    System.out.println( "SimpleVar: " + exp.name );
  }

  public void visit( VarDecList varDecList, int level, boolean isAddr){
    while( varDecList != null ) {
      varDecList.head.accept( this, level, isAddr);
      varDecList = varDecList.tail;
    }
  }

  public void visit( WhileExp exp, int level, boolean isAddr){
    indent( level );
    level++;
    System.out.println( "WhileExp: " );
    exp.exp.accept(this,level, isAddr);
    exp.compound.accept(this,level, isAddr);
  }

}
