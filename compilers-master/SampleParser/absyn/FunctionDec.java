package absyn;

public class FunctionDec extends Dec {
    public NameTy result;
    public String func;
    public VarDecList params;
    public CompoundExp body;
    //records the start address of the corresponding function, which is needed
    //for a function call
    public int funcAddr;

    public FunctionDec(int row, int col, NameTy result, String func, VarDecList params, CompoundExp body) {
        this.row = row;
        this.col = col;
        this.func = func;
        this.params = params;
        this.body = body;
        //if missing type specifier, we set it do void by default
        if(result == null){
          this.result = new NameTy(row, col, 2);
        }
        else{
          this.result = result;
        }
    }

    public void accept( AbsynVisitor visitor, int level, boolean isAddr){
      // System.out.println("Testing FunctionDec");
      visitor.visit(this,level, isAddr);
    }

    public String getName(NameTy ty) {
      if(ty.name == 1) {
        return "int";
      }
      return "void";
    }
}
