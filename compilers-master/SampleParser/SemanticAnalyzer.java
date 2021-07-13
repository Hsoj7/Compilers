import absyn.*;

import java.util.*;

/*
to do:
create google doc
handle collisions of a variable being declared in multiple scopes
print global scope variables/functions at the end of all input
*/

public class SemanticAnalyzer implements AbsynVisitor{
  final static int SPACES = 4;

  //declare hashmap structure
  public HashMap<String, ArrayList<NodeType>> symbolTable;
  public String currentFunction;
  public NameTy curFuncReturn;
  public boolean isFunctionArgument = false;
  public ArrayList<String> allFunctions = new ArrayList<>();
  public ArrayList<Integer> funcParams = new ArrayList();
  public boolean isArg = false;
  public int argCounter = 10;

  public SemanticAnalyzer(){
    symbolTable = new HashMap<String, ArrayList<NodeType>>();
    currentFunction = "";
    curFuncReturn = null;
    //printScope(0);
    //System.out.println("Leaving the global scope");
  }

  public void printScope(int level) {
    ArrayList<String> keysToPrint = new ArrayList<>();
    for(String key : symbolTable.keySet()) {
      ArrayList<NodeType> tempList = symbolTable.get(key);
      if(tempList.get(0).level == level) {
        keysToPrint.add(key);
      }
    }

    for(String key : keysToPrint) {
      int type = symbolTable.get(key).get(0).def.name;
      if(type == 1) {
        indent( level );
        indent( level );
        System.out.println(symbolTable.get(key).get(0).name + ": int");
      }
      else {
        System.out.println(symbolTable.get(key).get(0).name + ": void");
      }
    }
  }


  //store information from name declarations
  public int insert(String id, NameTy type, int level){
    NodeType nt = new NodeType(id, type, level, 0, 0);

    //tempList = symbolTable.get(id);

    //if templist is null at this point, symbolTable @ id is empty
    //thus, we create a new list first, then add it

    if(symbolTable.containsKey(id)) {
      ArrayList<NodeType> tempArr = symbolTable.get(id);
      int curLevel  = tempArr.get(0).level;

      if(curLevel == level) {
        return -1;
      }

      tempArr.add(0, nt);
      symbolTable.put(id, tempArr);

    }
    else {
      ArrayList<NodeType> tempList = new ArrayList<NodeType>();
      tempList.add(0, nt);
      symbolTable.put(id, tempList);
    }

    // System.out.println("\nPrinting table:");
    // printTable(symbolTable);
    // System.out.println("");
    return 0;
  }

  public NameTy lookup(String id){
    ArrayList<NodeType> al = symbolTable.get(id);
    if(al == null){
      return null;
    }
    NodeType nt = al.get(0);
    NameTy type = nt.def;

    return type;
  }

  //remove information from the view when the corresponding declaration is out of scope
  public void delete(int level){
    // System.out.println("Level = " + level);

    ArrayList<String> keysToRemove = new ArrayList<>();

    for(String key : symbolTable.keySet()) {
      ArrayList<NodeType> tempList = symbolTable.get(key);
      if(tempList.get(0).level == level) {
        keysToRemove.add(key);
      }
    }

    for(int i = 0; i < keysToRemove.size(); i++) {
      String key = keysToRemove.get(i);
      //if there's only one thing that in the table
      if(symbolTable.get(key).size() > 0){
        symbolTable.get(key).remove(0);
      }
      if(symbolTable.get(key).size() == 0) {
        symbolTable.remove(key);
      }
    }
  }

  //method to print contents of the symbol table, mainly used for debugging
  public void printTable(HashMap<String, ArrayList<NodeType>> symbolTable){
    for(String i: symbolTable.keySet()){
      System.out.println(i);
    }
  }

  //method to check for integer value for type checking
  public boolean isInteger(){

    return false;
  }

  //might not need this.. maybe delete later
  private void indent( int level ) {
    for( int i = 0; i < level * SPACES; i++ ) System.out.print( " " );
  }

  //for expression nodes, do type-checking!
  public void visit( ExpList expList, int level, boolean isAddr) {
    // System.out.println("ExpList, level: " + level);

    while( expList != null ) {
      expList.head.accept( this, level, isAddr);
      expList = expList.tail;
    }
  }

  public void visit( AssignExp exp, int level, boolean isAddr) {
    // System.out.println("AssignExp, level: " + level);
    // indent( level );
    exp.lhs.accept( this, level, isAddr);
    exp.rhs.accept( this, level, isAddr);

    // OpExp op = (OpExp)exp.rhs;
    // System.out.println("Type is " + op.op);

    // NameTy rhsType = lookup(exp.rhs.exp.name);

    NameTy lhsType = null;

    //check value of lhs
    if(exp.lhs instanceof SimpleVar) {
      SimpleVar sVar = (SimpleVar) exp.lhs;
      lhsType = lookup(exp.lhs.name);
      if(lhsType == null){
        int row = exp.lhs.row + 1;
        System.err.println("Error found on line "+row+", row: "+exp.lhs.col+" variable \""+exp.lhs.name+"\" not declared");
      }

      //check if sVar.name exists in table
    }
    else if(exp.lhs instanceof IndexVar){
      //check if indexvar? I think so.
      IndexVar iVar = (IndexVar) exp.lhs;
      lhsType = lookup(exp.lhs.name);
      if(lhsType == null){
        int row = exp.lhs.row + 1;
        System.err.println("Error found on line "+row+", row: "+exp.lhs.col+" variable \""+exp.lhs.name+"\" not declared");
      }
    }
    else{
      System.err.println("Illegal assignment expression on line: "+exp.row+", row: "+exp.col);
    }

    //get values of rhs and make sure the are both the same type that lhs is
    //might have to figure out some sort of loop in the case of
    //fac = ((fac * x)*x)
    // System.out.println("HERE " + exp.rhs);
    if(exp.rhs instanceof OpExp) {
      NameTy rhslhsType = null;
      NameTy rhsrhsType = null;
      OpExp opExp = (OpExp) exp.rhs;
      Exp left = (Exp) opExp.left;
      Exp right = (Exp) opExp.right;

      //checking the lhs of the rhs (the expression)
      if(left instanceof VarExp){
        VarExp varExp = (VarExp) left;

        if(varExp.variable instanceof IndexVar) {
          IndexVar iVar = (IndexVar) varExp.variable;
          rhslhsType = lookup(iVar.name);
          // System.out.println("rhslhsType = " + rhslhsType.name);
        }
        else if(varExp.variable instanceof SimpleVar) {
          SimpleVar sVar = (SimpleVar) varExp.variable;
          rhslhsType = lookup(sVar.name);
          // System.out.println("rhslhsType = " + rhslhsType.name);
        }
      }
      else if(left instanceof IntExp){
        IntExp iExp = (IntExp) left;
        rhslhsType = new NameTy(exp.row, exp.col, 1);
      }
      //checking the rhs of the rhs (the expression)
      if(right instanceof VarExp){
        VarExp varExp = (VarExp) right;

        if(varExp.variable instanceof IndexVar) {
          IndexVar iVar = (IndexVar) varExp.variable;
          rhsrhsType = lookup(iVar.name);
          // System.out.println("rhsrhsType = " + rhsrhsType.name);
        }
        else if(varExp.variable instanceof SimpleVar) {
          SimpleVar sVar = (SimpleVar) varExp.variable;
          rhsrhsType = lookup(sVar.name);
          // System.out.println("rhsrhsType = " + rhsrhsType.name);
        }
      }
      else if(right instanceof IntExp){
        IntExp iExp = (IntExp) right;
        rhsrhsType = new NameTy(exp.row, exp.col, 1);
      }

      //now check that all the lhs and rhs of assign expression match
      //null check to avoid NullPointerException
      //error is handled in varExp
      if(lhsType == null){
        ;
      }
      else if(rhslhsType == null){
        ;
      }
      else if(rhsrhsType == null){
        ;
      }
      else if(lhsType.name == rhslhsType.name && lhsType.name == rhsrhsType.name){
        ;
      }
      else{
        System.err.println("Error: Type mismatch found in assignment expression on line: "+exp.row+", row: "+exp.col);
      }
    }
    else if(exp.rhs instanceof CallExp) {
      CallExp callExp = (CallExp) exp.rhs;
      NameTy rhsType = lookup(callExp.func);

      if(rhsType == null){
        System.err.println("Error found on line "+callExp.row+", row: "+callExp.col+" function \""+callExp.func+"\" not found");
      }
      else if(rhsType.name == lhsType.name){
        ;
      }
      else{
        System.err.println("Error: Type mismatch found in assignment expression on line: "+exp.row+", row: "+exp.col);
      }
    }

  }

  //NEED TO CHECK TYPE OF EXPRESSION
  /* public Exp ifpart;
     public Exp thenpart;
     public Exp elsepart; */
  public void visit( IfExp exp, int level, boolean isAddr) {
    // indent( level );
    // System.out.println( "IfExp:, level: " + level );
    level++;
    indent( level );
    exp.ifpart.accept( this, level, isAddr);

    /*Need to check that if what follows if part is OpExp or VarExp or IntExp */
    System.out.println("Entering a new block if:");
    exp.thenpart.accept( this, level, isAddr);
    if (exp.elsepart != null ) {
      exp.elsepart.accept( this, level, isAddr);

    }
    printScope(level);
    delete(level);
    indent( level );
    level--;
    System.out.println("Leaving the block");
  }

  public void visit( IntExp exp, int level, boolean isAddr) {
    // System.out.println("IntExp, level: " + level);

  }

  public void visit( OpExp exp, int level, boolean isAddr) {
    // System.out.println("OpExp, level: " + level);
    exp.left.accept( this, level, isAddr);
    exp.right.accept( this, level, isAddr);

    //NEED TO MAKE SURE exp.rhs & exp.lhs are both valid type
    //else return error


  }

  public void visit( VarExp exp, int level, boolean isAddr) {
    // System.out.println("VarExp, level: " + level);

    //WHEN YOU SEE A VARIABLE, YOU NEED TO MAKE SURE
    //IT HAS BEEN DECLARED AND ADDED TO THE TABLE FIRST
    if(exp.variable instanceof SimpleVar) {
      // System.out.println("HERERERERERERE");
      SimpleVar sVar = (SimpleVar) exp.variable;
      // System.out.println("name = " + sVar.name);
      if(!symbolTable.containsKey(sVar.name)) {
        System.err.println("Error: row: "+exp.row+", col: "+exp.col+", undeclared variable");
      }
    }
    else {
      IndexVar iVar = (IndexVar) exp.variable;
      if(!symbolTable.containsKey(iVar.name)) {
        System.err.println("Error: row: "+exp.row+", col: "+exp.col+", undeclared variable");
      }
    }
  }

  public void visit( ArrayDec exp, int level, boolean isAddr){
    // System.out.println("ArrayDec, level: " + level);
    int insertReturn = 0;

    if(exp.typ.name == 2){
      System.err.println("Error on line: "+exp.row+", column: "+exp.col+", variable "+exp.name+" cannot be type void. Corrected to type int");
      exp.typ.name = 1;
    }

    NameTy type = exp.typ;
    insertReturn = insert(exp.name, type, level);
    if(insertReturn == -1){
      System.err.println("Error on line: "+exp.row+", column: "+exp.col+", variable "+exp.name+" is already defined");
    }
  }

  public void visit( CallExp exp, int level, boolean isAddr){
    // System.out.println("CallExp, level: " + level);
    if(exp.args != null){
      exp.args.accept(this, level, isAddr);
    }

    //NEED TO CHECK THAT THE FUNCTION BEING CALLED IS DEFINED IN TABLE
  }

  public void visit( CompoundExp exp, int level, boolean isAddr){
    // System.out.println("CompoundExp, level: " + level);

    // indent( level );
    if(exp.decs != null){
      exp.decs.accept(this,level, isAddr);
    }
    if(exp.exps != null){
      exp.exps.accept(this,level, isAddr);
    }

  }

  public void visit( DecList decList, int level, boolean isAddr){
    // System.out.println("DecList, level: " + level);
    //I don't think this needs to be changed?
    while( decList != null ) {
      decList.head.accept( this, level, isAddr);
      decList = decList.tail;
    }

  }

  public void visit( FunctionDec exp, int level, boolean isAddr){
    // System.out.println("FunctionDec, level: " + level);
    int insertReturn = 0;
    currentFunction = exp.func;
    curFuncReturn = null;


    level++;
    indent( level );
    System.out.println("Entering the scope for function " +exp.func+":");
    if(exp.params != null){
      isArg = true;
      if(exp.params.head != null){
        isFunctionArgument = true;
        exp.params.accept(this,level, isAddr);
      }
    }
    funcParams.add(argCounter);
    argCounter++;
    isArg = false;
    if(exp.body != null){
      exp.body.accept(this,level, isAddr);
    }

    allFunctions.add(exp.func);
    NameTy type = exp.result;

    insertReturn = insert(exp.func, type, level-1);
    if(insertReturn == -1){
      System.err.println("Error on line: "+exp.row+", column: "+exp.col+", variable "+exp.func+" is already defined");
    }

    if(curFuncReturn == null){
      //case where there is no return statement but the function is not void
      if(type.name == 1){
        System.err.println("Error: Missing return expression from function \""+exp.func+"\"");
      }
    }
    else{
      // System.out.println("else ");
      if(type.name == curFuncReturn.name){
        // System.out.println("the types are same ");
        ; //good, the function declaration matches its return
      }
      else{
        int row = curFuncReturn.row + 1;
        System.err.println("Error: Illegal return expression on line: "+row+", row: "+curFuncReturn.col);
      }
    }

    //delete function scope variables
    printScope(level);
    delete(level);
    indent( level );
    level = level - 1;

    System.out.println("Leaving the function scope");
  }

  public void visit( IndexVar exp, int level, boolean isAddr){
    // System.out.println("IndexVar, level: " + level);
    exp.index.accept(this, level, isAddr);

    //need to check return type
  }

  public void visit( NameTy exp, int level, boolean isAddr){
    // System.out.println("NameTy, level: " + level);

  }

  public void visit( NilExp exp, int level, boolean isAddr){
    // System.out.println("NilExp, level: " + level);

  }

  public void visit( ReturnExp exp, int level, boolean isAddr){
    // System.out.println("ReturnExp, level: " + level);
    level++;
    if(exp.exp != null){
      exp.exp.accept(this,level, isAddr);
    }

    //gets the return type of the return statement
    //it is used when the FunctionDec visit function is finished visiting children
    if(exp.exp == null){
      curFuncReturn = new NameTy(exp.row, exp.col, 2);
    }
    else{
      curFuncReturn = new NameTy(exp.row, exp.col, 1);
    }

  }

  public void visit( SimpleDec exp, int level, boolean isAddr){
    int insertReturn = 0;
    boolean mainVoid = false;
    boolean missingintID = false;

    if(isArg == true){
      funcParams.add(exp.typ.name);
    }


    if(exp.typ.name == 2){
      if(exp.name == null && isFunctionArgument == true){
        mainVoid = true;
        isFunctionArgument = false;
      }
      else{
        System.err.println("Error on line: "+exp.row+", column: "+exp.col+", variable "+exp.name+" cannot be type void. Corrected to type int");
        exp.typ.name = 1;
      }
    }
    else if(exp.typ.name == 1 && exp.name == null){
      System.err.println("Error on line: "+exp.row+", column: "+exp.col+", expected variable name");
      missingintID = true;
    }

    if(mainVoid || missingintID){
      mainVoid = false;
      missingintID = false;
      ;//dont insert into table
    }
    else{
      NameTy type = exp.typ;
      insertReturn = insert(exp.name, type, level);
      if(insertReturn == -1){
        System.err.println("Error on line: "+exp.row+", column: "+exp.col+", variable "+exp.name+" is already defined");
      }
    }
  }

  public void visit( SimpleVar exp, int level, boolean isAddr){
    // System.out.println("SimpleVar, level: " + level);

  }

  public void visit( VarDecList varDecList, int level, boolean isAddr){
    // System.out.println("VarDecList, level: " + level);
    //I don't think this needs to be changed?
    while( varDecList != null ) {
      varDecList.head.accept( this, level, isAddr);
      varDecList = varDecList.tail;
    }

  }

  public void visit( WhileExp exp, int level, boolean isAddr){
    // System.out.println("WhileExp, level: " + level);
    level++;
    indent(level);
    System.out.println("Entering a new block while:");
    exp.exp.accept(this,level, isAddr);
    exp.compound.accept(this,level, isAddr);

    printScope(level);
    delete(level);
    indent(level);
    level = level - 1;
    System.out.println("Leaving the block");
  }

}

//i think we'll need methods for insert, lookup and delete

//need new visitor methods from this class

//I assume it is in the visitor methods that we add to the symbol table?

//Interating declarations
  //The same name can't be re-declared in the same scopre
    //solution: perform a lookup before each insert

//make sure to include all special cases -> slides at the end of lec 8
