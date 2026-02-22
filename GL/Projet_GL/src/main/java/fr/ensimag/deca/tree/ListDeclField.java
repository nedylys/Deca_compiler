package fr.ensimag.deca.tree;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.tools.IndentPrintStream;

public class ListDeclField extends TreeList<AbstractDeclField> {

     public void verifyListDeclField(DecacCompiler compiler, ClassDefinition currentClass) throws ContextualError {
        // This Method serves for ddeclaring Fields in Pass 2, as a result we don't check initializations as they imply verifyExp (which is part of Pass 3)      
        for (AbstractDeclField field : getList()) {
            field.verifyDeclField(compiler, currentClass);
        }
    }

    public void verifyListFieldInitialization(DecacCompiler compiler, ClassDefinition currentClass) throws ContextualError {
        // This one is to be called in Passe 3
        for (AbstractDeclField f : getList()) {
            f.verifyInitializationOfField(compiler, currentClass);
        }
    }

    @Override
    public void decompile(IndentPrintStream s) {
        //throw new UnsupportedOperationException("not yet implemented");
        for (AbstractDeclField field : getList()) {
            field.decompile(s);
            s.println();
        }
    }

    /* Il faut un print sinon ça skip
    protected void prettyPrintChildren(PrintStream s, String prefix){
        for(AbstractDeclField f : this.getList()){
            f.prettyPrintChildren(s, prefix);
        }
    }
    */

    /* Pas besoin
    protected void iterChildren(TreeFunction f){
        for(AbstractDeclField field : this.getList()){
            field.iterChildren(f);
        }
    }
    */

    //extension
    public boolean foldConstants(DecacCompiler compiler){
        boolean changed = false;

        for(AbstractDeclField declField : getList()){
            changed |= declField.foldConstants(compiler);
        }
        return changed;
    }
}