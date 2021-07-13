package absyn;


public class IfExp extends Exp {
    public Exp ifpart;
    public Exp thenpart;
    public Exp elsepart;

    public IfExp(int row, int col, Exp ifpart, Exp thenpart, Exp elsepart) {
        this.row = row;
        this.col = col;
        this.ifpart = ifpart;
        this.thenpart = thenpart;
        this.elsepart = elsepart;
    }

    public void accept( AbsynVisitor visitor, int level, boolean isAddr){
      // System.out.println("Testing IfExp");
      visitor.visit(this,level, isAddr);
    }
}
