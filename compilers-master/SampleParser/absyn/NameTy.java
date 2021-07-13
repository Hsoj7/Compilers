package absyn;

public class NameTy extends Absyn {
    public int name;
    public final static int INT  = 1;
    public final static int VOID = 2;

    public NameTy(int row, int col, int name) {
        this.row = row;
        this.col = col;
        this.name = name;
    }

    public void accept( AbsynVisitor visitor, int level, boolean isAddr){
      // System.out.println("Testing NameTy");
      visitor.visit(this,level, isAddr);
    }
}
