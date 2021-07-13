package absyn;

abstract public class VarDec extends Dec {
    public NameTy typ;
    public String name;
    //Either 0 for global scope or 1 for local scope
    public int nestLevel;
    //offset within the related stackframe for memory access
    public int offset;
}
