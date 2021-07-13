package absyn;

public class WhileExp extends Exp {
    public Exp exp;
    public Exp compound;

    public WhileExp(int row, int col, Exp exp, Exp compound) {
        this.row = row;
        this.col = col;
        this.exp = exp;
        this.compound = compound;
    }

    public void accept( AbsynVisitor visitor, int level, boolean isAddr){
      // System.out.println("Testing WhileExp" + exp + " here " + compound);
      visitor.visit(this,level, isAddr);
    }
}
