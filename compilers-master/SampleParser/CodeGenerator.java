import absyn.*;

import java.util.*;
import java.io.*;
import java.io.IOException;

/*
to do:
call expression function

account for divide by 0 error

get all assign cases working
*/

public class CodeGenerator implements AbsynVisitor{
  final static int SPACES = 4;
  final static int IADDR_SIZE = 1024;
  final static int DADDR_SIZE = 1024;
  final static int NO_REGS = 8;
  //program counter
  final static int PC_REG = 7;
  //global pointer
  final static int GP_REG = 6;
  //frame pointer
  final static int FP_REG = 5;
  final static int AC_REG = 0;
  final static int AC1_REG = 1;

  //declare hashmap structure
  public HashMap<String, ArrayList<NodeType>> symbolTable;
  public String currentFunction;
  public NameTy curFuncReturn;
  public boolean isFunctionArgument = false;
  public ArrayList<String> allFunctions = new ArrayList<>();
  public int levelCounter;
  //this will hold the filename .tm to write assembly instructions to
  public String fileName;
  //file write will append strings to a given file
  public FileWriter tmWriter;
  //indicates the current instruction we are working with
  static int emitLoc = 0;
  //points to the next available space
  static int highEmitLoc = 0;
  //keeps track of the label for main's entry point
  public int mainEntry = 0;

  //incremented for every variable in the global space
  //just int y would increment by 1
  //an array of 10, int array[10], would offset it by 10
  public static int globaloffset = 0;
  //keeps track of how much stuff is in a stackframe
  public static int frameoffset = 0;
  //useful when jumping between functions/scopes
  public static int frameOffsetHolder = 0;
  //holds frameoffset for calcuations such as x + 3
  public int assignCalc;
  // public int skippedOffset;
  //finds number of variables declared in a function scope
  public int numberDecs = 0;
  //checks if the current calls are within callExp
  public boolean isCallExp = false;
  //check if the current call is within return
  public boolean isreturnExp = false;



  public static int ofpFO = 0;
  public static int retFO = -1;
  public static int initFO = -2;

  public CodeGenerator(String fileName){
    symbolTable = new HashMap<String, ArrayList<NodeType>>();
    currentFunction = "";
    curFuncReturn = null;
    levelCounter = 0;
    assignCalc = 0;
    // skippedOffset = 0;

    int i = 0;
    String filePrefix = "";
    while(fileName.charAt(i) != '.'){
      filePrefix = filePrefix + fileName.charAt(i);
      i++;
    }
    filePrefix = filePrefix + ".tm";
    this.fileName = filePrefix;
  }

  public void addPrelude(){
    writeStringToFile("* C-Minus Compilation to TM Code\n");
    writeStringToFile("* File: "+fileName+"\n");
    writeStringToFile("* Standard prelude\n");
    writeStringToFile("  "+emitLoc+":    LD  "+GP_REG+",0"+"("+AC_REG+")  load gp with maxaddress\n");
    emitLoc++;
    writeStringToFile("  "+emitLoc+":   LDA  "+FP_REG+",0"+"("+GP_REG+")  copy gp to fp\n");
    emitLoc++;
    writeStringToFile("  "+emitLoc+":    ST  "+AC_REG+",0"+"("+AC_REG+")  clear location 0\n");
    emitLoc++;
    //savedLocation now holds 3
    int savedLocation = emitSkip(1);
    // emitLoc++;
    // emitLoc = 4;
    writeStringToFile("* Jump around i/o routines here\n");
    writeStringToFile("* Code for input routine\n");
    writeStringToFile("  "+emitLoc+":    ST  "+AC_REG+",-1"+"("+FP_REG+")  store return\n");
    emitLoc++;
    writeStringToFile("  "+emitLoc+":    IN  "+AC_REG+",0,"+AC_REG+"  input\n");
    emitLoc++;
    writeStringToFile("  "+emitLoc+":    LD  "+PC_REG+",-1"+"("+FP_REG+")  return to caller\n");
    emitLoc++;
    writeStringToFile("* Code for output routine\n");
    writeStringToFile("  "+emitLoc+":    ST  "+AC_REG+",-1"+"("+FP_REG+")  store return\n");
    emitLoc++;
    writeStringToFile("  "+emitLoc+":    LD  "+AC_REG+",-2"+"("+FP_REG+")  load output value\n");
    emitLoc++;
    writeStringToFile("  "+emitLoc+":   OUT  "+AC_REG+",0,"+AC_REG+"  output\n");
    emitLoc++;
    writeStringToFile(" "+emitLoc+":    LD  "+PC_REG+",-1"+"("+FP_REG+")  return to caller\n");
    emitLoc++;
    int savedLocation2 = emitSkip(0);
    emitBackup(savedLocation);
    // emitLoc = savedLocation;
    writeStringToFile("  "+emitLoc+":   LDA  "+PC_REG+",7"+"("+PC_REG+")  jump around i/o code\n");
    emitRestore();
    // emitLoc = 11;
    writeStringToFile("* End of standard prelude\n");
  }

  public void addFinale(){
    writeStringToFile("* End of execution\n");
    emitRM("ST", FP_REG, globaloffset+ofpFO, FP_REG, "push ofp");
    emitRM("LDA", FP_REG, globaloffset, FP_REG, "push frame");
    emitRM("LDA", AC_REG, 1, PC_REG, "load ac with return pointer");
    emitRM_Abs("LDA", PC_REG, mainEntry, "jump to main's location");
    emitRM("LD", FP_REG, ofpFO, FP_REG, "pop frame");
    writeStringToFile(" "+emitLoc+":  HALT  0,0,0   \n");

    emitLoc++;

  }

  public void genCode(Exp exp, boolean isAddr){
    String codeStr = "";

    if(exp != null){
      if(exp instanceof OpExp){
        OpExp opex = (OpExp) exp;
        System.out.println("Still op");
        genCode(opex.left, isAddr);
        genCode(opex.right, isAddr);
      }
      else if(exp instanceof AssignExp){
        AssignExp assExp = (AssignExp) exp;
        System.out.println("Found assignExp");
        // genCode(assExp.lhs, true);
        // genCode(assExp.rhs, isAddr);
      }
      //this one was giving an error
    //   else if(exp instanceof IndexVar){

    //   }
      else if(exp instanceof IntExp){
        IntExp ie = (IntExp) exp;
        System.out.println("Found IntExp, value = " + ie.value);
      }
      else if(exp instanceof VarExp){
        VarExp ve = (VarExp) exp;
        System.out.println("Found VarExp, name = " + ve.name);
      }
      else{
        //writeStringToFile("Error");
      }
    }

  }

  //opens, writes to then closes .tm file
  //The string should be already formatted in assembly when calling this method
  public void writeStringToFile(String codeStr){
    try{
      tmWriter = new FileWriter(fileName, true);
    } catch (Exception e) {
      System.out.println("Error opening/creating file "+fileName+": " + e);
    }

    try{
      tmWriter.write(codeStr);
    } catch (Exception e) {
      System.out.println("Error writing to file "+fileName+": " + e);
    }

    try{
      tmWriter.close();
    } catch (Exception e) {
      System.out.println("Error closing file "+fileName+": " + e);
    }

  }

  //this will take in a regular string and convert it to assembly.
  //call writeStringToFile() once the assembly string is formatted correctly
  public void emitCode(String codeStr){

  }

  //used from the genCode function to return a variable when breaking down
  //a larger calculation, such as x = (x * fac) * 3
  //this enforces three-address
  public String newtemp() {
      //need to implement
      return "newvariablename";
  }

  //used to store a number when skipping if not all infomation is known with
  //the given call
  public int emitSkip(int distance){
    int i = emitLoc;
    emitLoc += distance;

    if(highEmitLoc < emitLoc) {
        highEmitLoc = emitLoc;
    }
    return i;
  }

  //used for back patching when we know the back patch value
  public void emitBackup(int loc){
    if(loc > highEmitLoc) {
        emitComment("BUG in emitBackup");
    }
    emitLoc = loc;
  }

  //used after emitBackup to continue where you left off before back patching
  public void emitRestore(){
    emitLoc = highEmitLoc;
  }

  public void emitRM_Abs(String op, int r, int a, String c) {
    if(op.length() == 3){
      writeStringToFile(" "+emitLoc + ":   " + op + "  " + r + "," + (a - (emitLoc + 1)) + "(" + PC_REG + ") \t" + c+"\n");
    }
    else{
      writeStringToFile(" "+emitLoc + ":    " + op + "  " + r + "," + (a - (emitLoc + 1)) + "(" + PC_REG + ") \t" + c+"\n");
    }

    emitLoc++;
    if(highEmitLoc < emitLoc) {
        highEmitLoc = emitLoc;
    }
  }

  //write register only (RO) instructions to .tm file
  public void emitRO(String op, int r, int s, int t, String c) {
    writeStringToFile(" "+emitLoc + ":   " + op + "  " + r + "," + s + "," + t + " \t" + c+"\n");
      emitLoc++;
      if(highEmitLoc < emitLoc) {
        highEmitLoc = emitLoc;
    }
  }

  //write register memory (RM) instructions to .tm file
  public void emitRM(String op, int r, int d, int s, String c) {
    if(op.length() == 3){
      writeStringToFile(" "+emitLoc + ":   " + op +"  "+ r + "," + d + "(" + s + ") \t" + c+"\n");
    }
    else{
      writeStringToFile(" "+emitLoc + ":    " + op +"  "+ r + "," + d + "(" + s + ") \t" + c+"\n");
    }

    emitLoc++;
      if(highEmitLoc < emitLoc) {
        highEmitLoc = emitLoc;
    }
  }

  //write comments to .tm file
  public void emitComment(String comment) {
    String c = "* " + comment+"\n";
    writeStringToFile(c);
  }

  public int insert(String id, int variableOffset, NameTy type, int levelCounter, int funcAddr){
    NodeType nt = new NodeType(id, type, levelCounter, variableOffset, funcAddr);

    if(symbolTable.containsKey(id)) {
      ArrayList<NodeType> tempArr = symbolTable.get(id);
      int curLevel  = tempArr.get(0).level;

      if(curLevel == levelCounter) {
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
    return 0;
  }

  public int lookupOffset(String id){
    ArrayList<NodeType> al = symbolTable.get(id);
    if(al == null){
      return 404;
    }
    NodeType nt = al.get(0);
    int returnOffset = nt.offset;

    return returnOffset;
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
  public void delete(int levelCounter){
    // System.out.println("levelCounter = " + levelCounter);

    ArrayList<String> keysToRemove = new ArrayList<>();

    for(String key : symbolTable.keySet()) {
      ArrayList<NodeType> tempList = symbolTable.get(key);
      if(tempList.get(0).level == levelCounter) {
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

  //might not need this.. maybe delete later
  private void indent( int levelCounter ) {
    // for( int i = 0; i < levelCounter * SPACES; i++ ) System.out.print( " " );
  }

  //for expression nodes, do type-checking!
  public void visit( ExpList expList, int offset, boolean isAddr) {
    while( expList != null ) {
      expList.head.accept( this, offset, isAddr);
      expList = expList.tail;
    }
  }

  public void visit( AssignExp exp, int offset, boolean isAddr) {

    // genCode(exp, isAddr);
    frameoffset--;
    exp.lhs.accept( this, offset, true);

    // skippedOffset = frameoffset;
    // frameoffset--;

    //check value of lhs
    if(exp.lhs instanceof SimpleVar) {
      SimpleVar sVar = (SimpleVar) exp.lhs;

      int varOffset = lookupOffset(sVar.name);
      if(varOffset == 404){
        System.out.println("Error getting offset value in assign exp");
      }
      else{
        // System.out.println("variable " + sVar.name +" offset = " + varOffset);
        emitComment("-> id");
        emitComment("looking up id: " + sVar.name);
        emitRM("LDA", AC_REG, varOffset, FP_REG, "load id " + sVar.name + " address");
        emitComment("<- id");
        emitRM("ST", AC_REG, frameoffset, FP_REG, "op: push left");
        assignCalc = frameoffset;
        frameoffset--;
      }
    }
    //come back to here to implement assembly
    else if(exp.lhs instanceof IndexVar){
      //check if indexvar? I think so.
      IndexVar iVar = (IndexVar) exp.lhs;

    }

    exp.rhs.accept( this, offset, isAddr);

    if(exp.rhs instanceof IntExp) {
      IntExp iExp = (IntExp) exp.rhs;

      //these put the value in variable in next available memory location
      emitRM("LDC", AC_REG, iExp.value, AC_REG, "load constant " + iExp.value);
      emitRM("ST", AC_REG, frameoffset, FP_REG, "store constant " + iExp.value);

      emitRM("LD", AC_REG, assignCalc, FP_REG, "load variable address");
      emitRM("LD", AC1_REG, frameoffset, FP_REG, "load register with stored value");
      emitRM("ST", AC1_REG, 0, AC_REG, "store value");
      frameoffset--;
    }
    else if(exp.rhs instanceof VarExp) {
      VarExp vExp = (VarExp) exp.rhs;

      if(vExp.variable instanceof SimpleVar){
        SimpleVar sVar = (SimpleVar) vExp.variable;
        int varOffset = lookupOffset(sVar.name);
        if(varOffset == 404){
          System.out.println("Error getting offset value in assign exp");
        }
        else{
          // System.out.println("variable " + sVar.name +" offset = " + varOffset);
          // System.out.println("FrameOffset = " + frameoffset);
          emitComment("-> id");
          emitComment("looking up id: " + sVar.name);
          emitRM("LD", AC_REG, varOffset, FP_REG, "load id " + sVar.name);
          emitComment("<- id");
          emitRM("LD", AC1_REG, assignCalc, FP_REG, "op: load left ");
          emitRM("ST", AC_REG, 0, AC1_REG, "assign: store value ");
          // emitRM("ST", AC_REG, frameoffset, FP_REG, "op: push left");

          // assignCalc = frameoffset;
          frameoffset--;
        }
      }
      else if(vExp.variable instanceof IndexVar){
        //implement
      }
    }
    else if(exp.rhs instanceof CallExp) {
      //implement
      // CallExp callExp = (CallExp) exp.rhs;
      // visit(callExp, offset, isAddr);
      //will need these lines
      // 20:     LD  1,-4(5) 	op: load left
      // 21:     ST  0,0(1) 	assign: store value

      emitRM("LD", AC1_REG, assignCalc, FP_REG, "load left");
      emitRM("ST", AC_REG, 0, AC1_REG, "store value");
    }


  }

  public void visit( IfExp exp, int offset, boolean isAddr) {

    levelCounter++;

    frameOffsetHolder = frameoffset;
    //then put frameoffset back to 0
    frameoffset = 0;
    frameoffset = frameoffset - 1;
    frameoffset -= numberDecs;


    emitComment("-> If");
    emitComment("if: jump after body comes back here");
    int saveWhileStart = emitSkip(0);

    //this will go to op expression
    frameoffset--;
    exp.ifpart.accept( this, offset, isAddr);


    emitComment("if: jump to end belongs here");
    int savedLocation = emitSkip(1);

    emitComment("-> compound statement");
    exp.thenpart.accept( this, offset, isAddr);
    emitComment("<- compound statement");

    //we need to increment the frame pointer correctly
    //check to make sure the while loop condition is being called


    //to jump back to condition of while loop
    emitRM_Abs("LDA", PC_REG, saveWhileStart, "jump to if condition");

    emitBackup(savedLocation);
    int tempOffset = (highEmitLoc - 1) - savedLocation;
    emitRM("JEQ", AC_REG, tempOffset, PC_REG, "if: jump to end");

    emitRestore();
    emitComment("<- if");

    emitComment("-> else");
    if ( exp.elsepart != null ) {
      exp.elsepart.accept( this, offset, isAddr);
    }
    emitComment("<- else");

    frameoffset = frameOffsetHolder;

    delete(levelCounter);
    levelCounter--;
  }

  public void visit( IntExp exp, int offset, boolean isAddr) {

    if(isreturnExp){
      emitComment("-> constant");
      emitRM("LDC", AC_REG, exp.value, AC_REG, "load constant");
      emitComment("<- constant");

    }

  }

  public void visit( OpExp exp, int offset, boolean isAddr) {
    if(exp instanceof OpExp) {
      // OpExp opExp = (OpExp) exp.rhs;
      Exp left = (Exp) exp.left;
      Exp right = (Exp) exp.right;

      int storeResult = 0;


      if(left instanceof VarExp){
        VarExp varExp = (VarExp) left;

        if(varExp.variable instanceof IndexVar) {
          IndexVar iVar = (IndexVar) varExp.variable;
          //implement
        }
        else if(varExp.variable instanceof SimpleVar) {
          SimpleVar sVar = (SimpleVar) varExp.variable;

          int varOffset = lookupOffset(sVar.name);
          if(varOffset == 404){
            System.out.println("Error getting offset value in assign exp");
          }
          else{
            //the current frameoffset would be pointing at the spot of the
            //result of the calculation
            storeResult = frameoffset;
            frameoffset--;
            emitRM("LD", AC_REG, varOffset, FP_REG, "load reg 0 with " + sVar.name);
            emitRM("ST", AC_REG, frameoffset, FP_REG, "store value of "+sVar.name);
          }
        }
      }
      else if(left instanceof IntExp){
        IntExp iExp = (IntExp) left;
        // System.out.println("stored = " + storeResult);
        // System.out.println("left before offset = " + frameoffset);
        storeResult = frameoffset;
        frameoffset--;
        emitComment("-> constant");
        emitRM("LDC", AC_REG, iExp.value, AC_REG, "load constant int " + iExp.value);
        emitComment("<- constant");
        emitRM("ST", AC_REG, frameoffset, FP_REG, "store the constant");
      }

      //implement, case of x = x + x;, x = x + fac;
      if(right instanceof VarExp){
        VarExp varExp = (VarExp) right;

        if(varExp.variable instanceof IndexVar) {
          IndexVar iVar = (IndexVar) varExp.variable;
        }
        else if(varExp.variable instanceof SimpleVar) {
          //implement
          SimpleVar sVar = (SimpleVar) varExp.variable;


        }
      }
      else if(right instanceof IntExp){
        IntExp iExp = (IntExp) right;
        // System.out.println("right before offset = " + frameoffset);
        // System.out.println("stored = " + storeResult);
        frameoffset--;
        emitComment("-> constant");
        emitRM("LDC", AC_REG, iExp.value, AC_REG, "load constant int " + iExp.value);
        emitComment("<- constant");
        emitRM("ST", AC_REG, frameoffset, FP_REG, "store the constant");

      }

      //add goes here
      // add
      frameoffset ++;
      if(exp.op == 0){
        emitRM("LD", AC_REG, frameoffset, FP_REG, "load value 1 for computation");
        frameoffset--;
        emitRM("LD", AC1_REG, frameoffset, FP_REG, "load value 2 for computation");
        emitRO("ADD", AC_REG, AC_REG, AC1_REG, "add values");
        emitRM("ST", AC_REG, storeResult, FP_REG, "store result of addition");
        storeValue(storeResult);
      }
      //subtract
      else if(exp.op == 1){
        emitRM("LD", AC_REG, frameoffset, FP_REG, "load value 1 for computation");
        frameoffset--;
        emitRM("LD", AC1_REG, frameoffset, FP_REG, "load value 2 for computation");
        emitRO("SUB", AC_REG, AC_REG, AC1_REG, "subtract values");
        emitRM("ST", AC_REG, storeResult, FP_REG, "store result of addition");
        storeValue(storeResult);
      }
      //multiply
      else if(exp.op == 2){
        emitRM("LD", AC_REG, frameoffset, FP_REG, "load value 1 for computation");
        frameoffset--;
        emitRM("LD", AC1_REG, frameoffset, FP_REG, "load value 2 for computation");
        emitRO("MUL", AC_REG, AC_REG, AC1_REG, "multiply values");
        emitRM("ST", AC_REG, storeResult, FP_REG, "store result of addition");
        storeValue(storeResult);
      }
      //divide
      else if(exp.op == 3){
        emitRM("LD", AC_REG, frameoffset, FP_REG, "load value 1 for computation");
        frameoffset--;
        emitRM("LD", AC1_REG, frameoffset, FP_REG, "load value 2 for computation");
        emitRO("DIV", AC_REG, AC_REG, AC1_REG, "divide values");
        emitRM("ST", AC_REG, storeResult, FP_REG, "store result of addition");
        storeValue(storeResult);
      }
      // ==
      else if(exp.op == 5){
        emitRM("LD", AC1_REG, frameoffset, FP_REG, "op >: load left");
        emitRO("SUB", AC_REG, AC1_REG, AC_REG, "subtract for op >");
        emitRM("JEQ", AC_REG, 2, PC_REG, "branch to true");
        emitRM("LDC", AC_REG, 0, AC_REG, "false case");
        emitRM("LDA", PC_REG, 1, PC_REG, "unconditional jump");
        emitRM("LDC", AC_REG, 1, AC_REG, "true case");
      }
      // !=
      else if(exp.op == 6){
        emitRM("LD", AC1_REG, frameoffset, FP_REG, "op >: load left");
        emitRO("SUB", AC_REG, AC1_REG, AC_REG, "subtract for op >");
        emitRM("JNE", AC_REG, 2, PC_REG, "branch to true");
        emitRM("LDC", AC_REG, 0, AC_REG, "false case");
        emitRM("LDA", PC_REG, 1, PC_REG, "unconditional jump");
        emitRM("LDC", AC_REG, 1, AC_REG, "true case");
      }
      // <
      else if(exp.op == 7){
        emitRM("LD", AC1_REG, frameoffset, FP_REG, "op >: load left");
        emitRO("SUB", AC_REG, AC1_REG, AC_REG, "subtract for op >");
        emitRM("JLT", AC_REG, 2, PC_REG, "branch to true");
        emitRM("LDC", AC_REG, 0, AC_REG, "false case");
        emitRM("LDA", PC_REG, 1, PC_REG, "unconditional jump");
        emitRM("LDC", AC_REG, 1, AC_REG, "true case");
      }
      // <=
      else if(exp.op == 8){
        emitRM("LD", AC1_REG, frameoffset, FP_REG, "op >: load left");
        emitRO("SUB", AC_REG, AC1_REG, AC_REG, "subtract for op >");
        emitRM("JLE", AC_REG, 2, PC_REG, "branch to true");
        emitRM("LDC", AC_REG, 0, AC_REG, "false case");
        emitRM("LDA", PC_REG, 1, PC_REG, "unconditional jump");
        emitRM("LDC", AC_REG, 1, AC_REG, "true case");
      }
      // >
      else if(exp.op == 9){
        // System.out.println("offset = " + frameoffset);

        emitRM("LD", AC1_REG, frameoffset, FP_REG, "op >: load left");
        emitRO("SUB", AC_REG, AC1_REG, AC_REG, "subtract for op >");
        emitRM("JGT", AC_REG, 2, PC_REG, "branch to true");
        emitRM("LDC", AC_REG, 0, AC_REG, "false case");
        emitRM("LDA", PC_REG, 1, PC_REG, "unconditional jump");
        emitRM("LDC", AC_REG, 1, AC_REG, "true case");
      }
      // >=
      else if(exp.op == 10){
        emitRM("LD", AC1_REG, frameoffset, FP_REG, "op >: load left");
        emitRO("SUB", AC_REG, AC1_REG, AC_REG, "subtract for op >");
        emitRM("JGE", AC_REG, 2, PC_REG, "branch to true");
        emitRM("LDC", AC_REG, 0, AC_REG, "false case");
        emitRM("LDA", PC_REG, 1, PC_REG, "unconditional jump");
        emitRM("LDC", AC_REG, 1, AC_REG, "true case");
      }



      frameoffset--;
    }



    exp.left.accept( this, offset, isAddr);
    exp.right.accept( this, offset, isAddr);

  }

  public void storeValue(int storeResult){
    emitRM("LD", AC_REG, assignCalc, FP_REG, "load register with address for calc");
    emitRM("LD", AC1_REG, storeResult, FP_REG, "load register with stored result");
    emitRM("ST", AC1_REG, 0, AC_REG, "store result");
    // emitRM("ST", AC1_REG, skippedOffset, FP_REG, "... save assignment for root node of tree structure");
  }

  public void visit( VarExp exp, int offset, boolean isAddr) {
    //need to add stuff
    if(isCallExp){
      int varOffset = lookupOffset(exp.name);
      if(varOffset == 404){
        System.out.println("Error getting offset value in assign exp");
      }
      else{
        // frameoffset--;
        emitComment("-> id");
        emitComment("looking up id: "+exp.name);
        emitRM("LD", AC_REG, varOffset, FP_REG, "load id " + exp.name);
        emitComment("<- id");
        emitRM("ST", AC_REG, frameoffset, FP_REG, "store arg value");
      }

      // System.out.println("Varex = " + exp.name);
    }
    else if(isreturnExp){
      int varOffset = lookupOffset(exp.name);
      if(varOffset == 404){
        System.out.println("Error getting offset value in assign exp");
      }
      else{
        emitComment("-> id");
        emitComment("looking up id: "+exp.name);
        emitRM("LD", AC_REG, varOffset, FP_REG, "load id " + exp.name);
        emitComment("<- id");
      }
    }
  }

  public void visit( ArrayDec exp, int offset, boolean isAddr){
    int insertReturn = 0;

    if(exp.typ.name == 2){
      exp.typ.name = 1;
    }

    NameTy type = exp.typ;
    insertReturn = insert(exp.name, offset, type, levelCounter, 0);
  }

  //need to check if the function name is input() or output() to call
  //the given input/output code in .tm file
  public void visit( CallExp exp, int offset, boolean isAddr){
    // System.out.println("CallExp " + frameoffset);
    isCallExp = true;
    emitComment("Call of function: "+exp.func);
    // emitComment("looking up id: "+ )
    // emitRM("")
    if(exp.args != null){
      exp.args.accept(this, offset, isAddr);
    }

    if(exp.func.equals("output")){
      // frameoffset--;
      emitRM("ST", FP_REG, frameoffset+2, FP_REG, "push ofp");
      emitRM("LDA", FP_REG, frameoffset+2, FP_REG, "push frame");
      emitRM("LDA", AC_REG, 1, PC_REG, "load ac with return pointer");

      //7 is the location for the output function, -1 because PC looks 1 ahead
      emitRM_Abs("LDA", PC_REG, 7, "jump to function location");
      // int location = 7 - emitLoc - 1;
      // emitRM("LDA", PC_REG, location, PC_REG, "jump to function location");
      emitRM("LD", FP_REG, 0, FP_REG, "pop frame");
    }
    else if(exp.func.equals("input")){
      // frameoffset--;
      emitRM("ST", FP_REG, frameoffset, FP_REG, "push ofp");
      emitRM("LDA", FP_REG, frameoffset, FP_REG, "push frame");
      emitRM("LDA", AC_REG, 1, PC_REG, "load ac with return pointer");

      //7 is the location for the output function, -1 because PC looks 1 ahead
      emitRM_Abs("LDA", PC_REG, 4, "jump to function location");
      // int location = 7 - emitLoc - 1;
      // emitRM("LDA", PC_REG, location, PC_REG, "jump to function location");
      emitRM("LD", FP_REG, 0, FP_REG, "pop frame");
    }
    else{
      NodeType nt = null;
      for(String key : symbolTable.keySet()) {
        if(exp.func.equals(symbolTable.get(key).get(0).name)){
          nt = symbolTable.get(key).get(0);
        }
      }
      frameoffset--;
      emitRM("ST", FP_REG, frameoffset, FP_REG, "push ofp");
      emitRM("LDA", FP_REG, frameoffset, FP_REG, "push frame");
      emitRM("LDA", AC_REG, 1, PC_REG, "load ac with return pointer");

      //7 is the location for the output function, -1 because PC looks 1 ahead
      emitRM_Abs("LDA", PC_REG, nt.funcAddr, "jump to function location");
      // int location = 7 - emitLoc - 1;
      // emitRM("LDA", PC_REG, location, PC_REG, "jump to function location");
      emitRM("LD", FP_REG, 0, FP_REG, "pop frame");

    }

    //after processing function call, op: load left
    emitComment("<- call");
    isCallExp = false;

  }

  public void visit( CompoundExp exp, int offset, boolean isAddr){
    if(exp.decs != null){
      exp.decs.accept(this,offset, isAddr);
    }
    if(exp.exps != null){
      exp.exps.accept(this,offset, isAddr);
    }
  }

  //check that main is defined here, if its not defeined, give output message
  //and end execution
  public void visit( DecList decList, int offset, boolean isAddr){
    while( decList != null ) {
      decList.head.accept( this, offset, isAddr);
      decList = decList.tail;
    }
  }

  public void visit( FunctionDec exp, int offset, boolean isAddr){
    int insertReturn = 0;
    currentFunction = exp.func;
    curFuncReturn = null;

    frameoffset = 0;
    numberDecs = 0;

    writeStringToFile("* Processing function: "+exp.func+"\n");
    int savedLocation = emitSkip(1);
    if(exp.func.equals("main")){
      mainEntry = savedLocation + 1;
    }
    //is this right?? not sure
    exp.funcAddr = emitLoc;

    //i think offset in this case is always -1, because the function's 'jump
    //around fn body' call is this line -1?
    frameoffset -= 1;
    offset -= 1;
    writeStringToFile(" "+emitLoc+":    ST  "+AC_REG+","+retFO+"("+FP_REG+")  save return address for "+exp.func+"\n");
    emitLoc++;

    levelCounter++;

    if(exp.params != null){
      if(exp.params.head != null){
        isFunctionArgument = true;
        exp.params.accept(this,offset, isAddr);
      }
    }
    emitComment("-> compound");
    if(exp.body != null){
      exp.body.accept(this,offset, isAddr);
    }

    allFunctions.add(exp.func);
    NameTy type = exp.result;

    insertReturn = insert(exp.func, frameoffset, type, levelCounter-1, exp.funcAddr);

    emitComment("<- compound");
    //delete function scope variables
    delete(levelCounter);
    levelCounter--;

    //-1 is hard coded as the offset here... check if that is always the case
    offset -= 1;
    writeStringToFile(" "+emitLoc+":    LD  "+PC_REG+","+retFO+"("+FP_REG+")  return back to the caller\n");
    emitLoc++;

    if(emitLoc > highEmitLoc){
      highEmitLoc = emitLoc;
    }

    int tempOffset = (highEmitLoc - 1) - savedLocation;

    emitBackup(savedLocation);

    writeStringToFile(" "+emitLoc+":   LDA  "+PC_REG+","+tempOffset+"("+PC_REG+")  jump around fn body\n");
    emitRestore();


    // System.out.println("Leaving the function scope");
  }

  public void visit( IndexVar exp, int offset, boolean isAddr){
    // genCode(exp.index);
    String temp = newtemp();
    //exp.index.temp = ?
    String codestr = "";
    //codestr += temp + " = " + exp.index.temp + " * elem_size(" + exp.name + ")";
    exp.index.accept(this, offset, isAddr);

  }

  public void visit( NameTy exp, int offset, boolean isAddr){

  }

  public void visit( NilExp exp, int offset, boolean isAddr){

  }

  public void visit( ReturnExp exp, int offset, boolean isAddr){
    levelCounter++;

    isreturnExp = true;

    if(exp.exp != null){
      exp.exp.accept(this,offset, isAddr);
    }

    emitRM("LD", PC_REG, -1, FP_REG, "return to caller");
    emitComment("<- return");

    isreturnExp = false;




    //gets the return type of the return statement
    //it is used when the FunctionDec visit function is finished visiting children
    if(exp.exp == null){
      curFuncReturn = new NameTy(exp.row, exp.col, 2);
    }
    else{
      curFuncReturn = new NameTy(exp.row, exp.col, 1);
    }

  }

  public void visit( SimpleDec exp, int offset, boolean isAddr){
    // System.out.println("SimpleDec, offset: " + offset);
    int insertReturn = 0;
    boolean mainVoid = false;
    boolean missingintID = false;

    //check if 'void' is passed in as the argument to function declaration
    //if it is void, we don't need to output any assembly code
    if(exp.name != null){
      numberDecs++;
      frameoffset -= 1;
      offset -= 1;
      exp.offset = frameoffset;
      emitComment("processing local var: " + exp.name);
      // emitRM("LDA", AC_REG, frameoffset, FP_REG, description);
    }

    if(mainVoid || missingintID){
      mainVoid = false;
      missingintID = false;
    }
    else{
      NameTy type = exp.typ;
      insertReturn = insert(exp.name, frameoffset, type, levelCounter, 0);
    }

  }

  public void visit( SimpleVar exp, int offset, boolean isAddr){
    // System.out.println("SimpleVar, offset: " + offset);
    // System.out.println("SimpleVar " + exp.name);

  }

  public void visit( VarDecList varDecList, int offset, boolean isAddr){
    while( varDecList != null ) {
      varDecList.head.accept( this, offset, isAddr);
      varDecList = varDecList.tail;
    }

  }

  public void visit( WhileExp exp, int offset, boolean isAddr){

    levelCounter++;

    //save current FrameOffset
    frameOffsetHolder = frameoffset;
    //then put frameoffset back to 0
    frameoffset = 0;
    frameoffset = frameoffset - 1;
    frameoffset -= numberDecs;
    // System.out.println("offset here = " + frameoffset);

    emitComment("-> while");
    emitComment("while: jump after body comes back here");
    int saveWhileStart = emitSkip(0);

    //this will go to op expression
    frameoffset--;
    exp.exp.accept(this,offset, isAddr);

    emitComment("while: jump to end belongs here");
    int savedLocation = emitSkip(1);

    emitComment("-> compound statement");
    exp.compound.accept(this,offset, isAddr);
    emitComment("<- compound statement");

    //we need to increment the frame pointer correctly
    //check to make sure the while loop condition is being called


    //to jump back to condition of while loop
    emitRM_Abs("LDA", PC_REG, saveWhileStart, "jump to while condition");

    emitBackup(savedLocation);
    int tempOffset = (highEmitLoc - 1) - savedLocation;
    emitRM("JEQ", AC_REG, tempOffset, PC_REG, "while: jump to end");

    emitRestore();
    emitComment("<- while");
    //restore correct label number
    //restore frameoffset to before while loop
    frameoffset = frameOffsetHolder;

    delete(levelCounter);
    levelCounter--;
  }

}

// int savedLocation2 = emitSkip(0);
// emitBackup(savedLocation);
// // emitLoc = savedLocation;
// writeStringToFile("  "+emitLoc+":   LDA  "+PC_REG+",7"+"("+PC_REG+")  jump around i/o code\n");
// emitRestore();
// // emitLoc = 11;
// writeStringToFile("* End of standard prelude\n");

// System.out.println("operator = " + exp.op);
//
// int varLeftOffset = 0;
//
// if(exp.left instanceof VarExp){
//   VarExp left = (VarExp) exp.left;
//
//   if(left.variable instanceof SimpleVar){
//     SimpleVar sVar = (SimpleVar) left.variable;
//     System.out.println("left found " + sVar.name);
//
//     varLeftOffset = lookupOffset(sVar.name);
//     if(varLeftOffset == 404){
//       System.out.println("Error getting offset value in assign exp");
//     }
//     // System.out.println("offset = " + frameoffset);
//     // emitRM("LD", AC_REG, varLeftOffset, FP_REG, "load " + sVar.name + " value");
//   }
//   else if(left.variable instanceof IndexVar){
//
//   }
// }
// else if(exp.left instanceof IntExp){
//   IntExp iVar = (IntExp) exp.left;
//   System.out.println("left found " + iVar.value);
// }
// else if(exp.left instanceof CallExp){
//
// }
//
// if(exp.right instanceof VarExp){
//   VarExp right = (VarExp) exp.right;
//
//   if(right.variable instanceof SimpleVar){
//     SimpleVar sVar = (SimpleVar) right.variable;
//     System.out.println("right found " + sVar.name);
//   }
//   else if(right.variable instanceof IndexVar){
//
//   }
// }
// else if(exp.right instanceof IntExp){
//   IntExp iVar = (IntExp) exp.right;
//   System.out.println("right found " + iVar.value);
// }
// else if(exp.left instanceof CallExp){
//
// }
