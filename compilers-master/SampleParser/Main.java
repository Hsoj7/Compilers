/*
  Created by: Fei Song
  File Name: Main.java
  To Build:
  After the scanner, tiny.flex, and the parser, tiny.cup, have been created.
    javac Main.java

  To Run:
    java -classpath /usr/share/java/cup.jar:. Main gcd.tiny

  where gcd.tiny is an test input file for the tiny language.
*/

import java.io.*;
import absyn.*;
import java.util.*;

class Main {
  public static boolean SHOW_TREE = false;
  public static boolean POST_ORDER = false;
  public static boolean CODE_GEN = false;
  static public void main(String argv[]) {
    /* Start the parser */
    for(int i = 0; i < argv.length; i++){
      if(argv[i].equals("-a")){
        SHOW_TREE = true;
      }
      else if(argv[i].equals("-s")){
        POST_ORDER = true;
      }
      else if(argv[i].equals("-c")){
        CODE_GEN = true;
      }
    }
    // if (argv.length > 1 && argv[1].equals("-a")) {
    //   SHOW_TREE = true;
    // }
    try {
      parser p = new parser(new Lexer(new FileReader(argv[0])));
      Absyn result = (Absyn)(p.parse().value);
      if(SHOW_TREE && result != null) {
         System.out.println("The abstract syntax tree is:");
         ShowTreeVisitor visitor = new ShowTreeVisitor();
         result.accept(visitor, 0, false);
      }
      //maybe this shouldn't be else if... are they supposed to both run if -a -s?
      else if(POST_ORDER && result != null){
        System.out.println("Entering the global scope:");
        SemanticAnalyzer semantic = new SemanticAnalyzer();
        result.accept(semantic, 0, false);

        HashMap<String, ArrayList<NodeType>> map = semantic.symbolTable;
        ArrayList<String> funcs =semantic.allFunctions;
        int position = 0;

        //System.out.println("MAP SIZE == " + map.size());
        for(String key : map.keySet()) {
          int type = map.get(key).get(0).def.name;
          if(funcs.contains(key)) {

            String insert = "(";
            for(int i = 0; i < semantic.funcParams.size(); i++){
              if(semantic.funcParams.get(position) < 2){
                if(semantic.funcParams.get(position) == 1){
                  insert = insert + "int";
                  if(semantic.funcParams.get(position + 1) < 2){
                    insert = insert + ", ";
                  }
                  else if(semantic.funcParams.get(position + 1) > 2){
                    insert = insert + ")";
                  }
                  position++;
                }
              }
              else{
                position++;
                break;
              }
            }

            if(insert.length() == 1){
              insert = insert + "void)";
            }

            if(type == 1) {
              System.out.println("    " + map.get(key).get(0).name + ": "+insert+" -> " + "(int)");
            }
            else {
              System.out.println("    " + map.get(key).get(0).name + ": "+insert+" -> " + "(void)");
            }
          }
          else {
            if(type == 1) {
              System.out.println("    " + map.get(key).get(0).name + ": int");
            }
            else {
              System.out.println("    " + map.get(key).get(0).name + ": void");
            }
          }
        }


        System.out.println("Leaving the global scope:");
      }
      else if(CODE_GEN && result != null){
        System.out.println("Generating assembly code:");
        String[] split = argv[0].split("/");
        String fileName = split[split.length - 1];

        CodeGenerator codeGen = new CodeGenerator(fileName);
        codeGen.addPrelude();
        result.accept(codeGen, 0, false);
        codeGen.addFinale();

      }
      //might need to make a case where there's -a and -s in argv
    } catch (Exception e) {
      /* do cleanup here -- possibly rethrow e */
      e.printStackTrace();
    }
  }
}
