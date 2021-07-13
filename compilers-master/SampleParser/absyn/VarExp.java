package absyn;

public class VarExp extends Exp {
    public Var variable;
    public String name;

    public VarExp(int row, int col, Var variable) {
        this.row = row;
        this.col = col;
        this.variable = variable;
        this.name = variable.name;
    }

    public void accept( AbsynVisitor visitor, int level, boolean isAddr){
      // System.out.println("Testing VarExp " + name);
      visitor.visit(this,level, isAddr);
    }
}
