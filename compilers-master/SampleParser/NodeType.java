import absyn.*;

public class NodeType{
  String name;
  NameTy def;
  int level;
  int offset;
  int funcAddr;


  public NodeType(String name, NameTy def, int level, int offset, int funcAddr){
    this.name = name;
    this.def = def;
    this.level = level;
    this.offset = offset;
    this.funcAddr = funcAddr;
  }
}
