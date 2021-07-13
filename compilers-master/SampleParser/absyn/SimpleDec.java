package absyn;

public class SimpleDec extends VarDec {

    public SimpleDec(int row, int col, NameTy typ, String name) {
        this.row = row;
        this.col = col;
        this.typ = typ;
        this.name = name;
    }

    public void accept( AbsynVisitor visitor, int level, boolean isAddr){
      // System.out.println("Testing SimpleDec");
      visitor.visit(this,level, isAddr);
    }

    public String getName(NameTy ty) {
      if(ty.name == 1) {
        return "int";
      }
      return "void";
    }
}
