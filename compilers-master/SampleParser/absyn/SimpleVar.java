package absyn;

public class SimpleVar extends Var {

    public SimpleVar(int row, int col, String name) {
        this.row = row;
        this.col = col;
        this.name = name;
    }

    public void accept( AbsynVisitor visitor, int level, boolean isAddr){
      // System.out.println("Testing SimpleVar");
      visitor.visit(this,level, isAddr);
    }
}
