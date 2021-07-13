JAVA=java
JAVAC=javac
JFLEX=jflex
CUP=cup
CLASSPATH=-cp /usr/share/java/cup.jar:.


all: Main.class

Main.class: absyn/*.java parser.java sym.java Lexer.java ShowTreeVisitor.java SemanticAnalyzer.java CodeGenerator.java NodeType.java Scanner.java Main.java

%.class: %.java
	$(JAVAC) $(CLASSPATH) $^

Lexer.java: CMinus.flex
	$(JFLEX) CMinus.flex

parser.java: CMinus.cup
	#$(CUP) -dump -expect 3 CMinus.cup
	$(CUP) -expect 5 CMinus.cup

clean:
	rm -f parser.java Lexer.java sym.java *.class absyn/*.class *~

# Josh Makefile header
# JAVA=java
# JAVAC=javac
# JFLEX=jflex
# CUP=cup
# CLASSPATH=-cp /usr/share/java/cup.jar:.

#Laiba Makefile Header
# JAVA=java
# JAVAC=javac
# JFLEX=jflex
# CLASSPATH=-cp ~/java/cup/java-cup-bin/java-cup-11b.jar:.
# CUP=$(JAVA) $(CLASSPATH) java_cup.Main
