package absyn;

public class IndexVar extends Var {
    public Exp index;

    public IndexVar(int row, int col, String name, Exp index) {
        this.row = row;
        this.col = col;
        this.name = name;
        this.index = index;
    }

    public void accept( AbsynVisitor visitor, int level, boolean isAddr){
      // System.out.println("Testing IndexVar");
      visitor.visit(this,level, isAddr);
    }
}
