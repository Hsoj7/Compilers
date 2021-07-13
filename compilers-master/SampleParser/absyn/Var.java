package absyn;

abstract public class Var extends Absyn {
    public String name;
    //might need to add a "link" to its related definition, whether SimpleDec.
    // ArrayDec or FunctionDec. that's where we can find the memory location
    //or the function address
}
