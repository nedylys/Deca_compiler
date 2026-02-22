package fr.ensimag.deca.tree;
import java.util.Set;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

public class ListDeclMethod extends TreeList<AbstractDeclMethod> {

     public void verifyListDeclMethod(DecacCompiler compiler, ClassDefinition currentClass) throws ContextualError {
        // This Method serves for ddeclaring Fields in Pass 2, as a result we don't check initializations as they imply verifyExp (which is part of Pass 3)      
        for (AbstractDeclMethod method : getList()) {
            method.verifyDeclMethod(compiler, currentClass);
                }
    }

    public void verifyListMethodBody(DecacCompiler compiler, ClassDefinition currentClass) throws ContextualError {
        // This one is to be called in Passe 3
        for (AbstractDeclMethod method : getList()) {
            method.verifyDeclMethodBody(compiler, currentClass);
        }
    }

    @Override
    public void decompile(IndentPrintStream s) {
        //throw new UnsupportedOperationException("not yet implemented");
        for (AbstractDeclMethod method : getList()) {
            method.decompile(s);
            s.println();
        }
    }

    /* Il faut un print sinon ça skip
    protected void prettyPrintChildren(PrintStream s, String prefix){
        for(AbstractDeclMethod method : this.getList()){
            method.prettyPrintChildren(s, prefix);
        }
    }
    */

    /* Pas besoin
    protected void iterChildren(TreeFunction m){
        for(AbstractDeclMethod method : this.getList()){
            method.iterChildren(m);
        }
    }
    */

    //extension
    public boolean foldConstants(DecacCompiler compiler){
        boolean changed = false;

        for(AbstractDeclMethod declMethod : getList()){
            changed |= declMethod.foldConstants(compiler);
        }
        return changed;
    }

    protected boolean eliminateDeadCode(DecacCompiler compiler){
        boolean changed = false;

        for(AbstractDeclMethod declMethod : getList()){
            changed |= declMethod.eliminateDeadCode(compiler);
        }
        return changed;
    }

    protected boolean eliminateDeadStores(DecacCompiler compiler) {
        boolean changed = false;
        for (AbstractDeclMethod m : getList()) {
            changed |= m.eliminateDeadStores(compiler);
        }
        return changed;
    }

    protected boolean eliminateUnusedVars(DecacCompiler compiler) {
        boolean changed = false;
        for (AbstractDeclMethod m : getList()) {
            changed |= m.eliminateUnusedVars(compiler);
        }
        return changed;
    }

    protected void collectReadVars(Set<Symbol> reads) {
        for (AbstractDeclMethod m : getList()) {
            m.collectReadVars(reads);
        }
    }

    protected void collectWrittenVars(Set<Symbol> writes) {
        for (AbstractDeclMethod m : getList()) {
            m.collectWrittenVars(writes);
        }
    }
}