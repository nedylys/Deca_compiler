package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;

import fr.ensimag.deca.tools.SymbolTable.Symbol;
import java.util.Set;
/**
 * Main block of a Deca program.
 *
 * @author gl43
 * @date 01/01/2026
 */
public abstract class AbstractMain extends Tree {

    protected abstract void codeGenMain(DecacCompiler compiler);
    /**
     * Implements non-terminal "main" of [SyntaxeContextuelle] in pass 3 
     */

    protected abstract void verifyMain(DecacCompiler compiler) throws ContextualError;
    
    //extension
    public boolean foldConstants(DecacCompiler compiler) {
        return false;
    }

    protected boolean eliminateDeadCode(DecacCompiler compiler){
        return false;
    }
    protected boolean eliminateUnusedVars(DecacCompiler compiler, Set<Symbol> reads) {
        return false;
    }

    protected void collectReadVars(Set<Symbol> reads) {}

    protected void collectWrittenVars(Set<Symbol> writes) {}

    protected boolean eliminateDeadStores(DecacCompiler compiler) {
        return false;
    }


}
