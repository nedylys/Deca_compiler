package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;

import java.util.Set;
import java.util.List;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

/**
 * Entry point for contextual verifications and code generation from outside the package.
 * 
 * @author gl43
 * @date 01/01/2026
 *
 */
public abstract class AbstractProgram extends Tree {
    protected boolean isOOP = false;
    public abstract boolean getIsOOP();
    public abstract void verifyProgram(DecacCompiler compiler) throws ContextualError;
    public abstract void codeGenProgram(DecacCompiler compiler) ;
    //extension 
    public abstract boolean foldConstants(DecacCompiler compiler);

    public abstract boolean eliminateDeadCode(DecacCompiler compiler);

    public abstract void collectReadVars(Set<Symbol> readsMain, List<Set<Symbol>> readsClasses);
    public abstract boolean eliminateUnusedVars(DecacCompiler compiler, Set<Symbol> readsMain);

    public abstract void collectWrittenVars(Set<Symbol> writesMain);

    // public boolean eliminateDeadStores(DecacCompiler compiler) {
    //     return false;
    // }

    public abstract boolean eliminateDeadStores(DecacCompiler compiler);
}
