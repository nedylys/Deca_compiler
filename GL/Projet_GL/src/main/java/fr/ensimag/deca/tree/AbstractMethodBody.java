package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.Label;

import fr.ensimag.deca.tools.SymbolTable.Symbol;
import java.util.Set;

/* I tried to follow the structure provided in AbstractMain.java */
/**
 * Field declaration
 *
 * @author gl43
 * @date 25/12/2026
 */


public abstract class AbstractMethodBody extends Tree {
    /**
     * Implements non-terminal "MethhodBody" of [SyntaxeContextuelle] in pass 3 
     */

        // method_body ↓env_types ↓env_exp ↓env_exp_params ↓class ↓return
    protected abstract void verifyMethodBody(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass, Type type) throws ContextualError;
        // The arguments of the function above are the translation of the rule above it:
    /*compiler ~ env_types (as one of its fields is public final EnvironmentType environmentType = new EnvironmentType(this);) 
      env_exp + env_exp_params ~ localEnv
      class ~ currentClass
      return ~ type*/

    public boolean foldConstants(DecacCompiler compiler){
        return false;
    }
    public abstract void codeGenClass(DecacCompiler compiler,Label nomFinMthd);

    protected boolean eliminateDeadCode(DecacCompiler compiler){
        return false;
    }

    protected void collectReadVars(Set<Symbol> reads) {
        
    }

    protected void collectWrittenVars(Set<Symbol> writes) {
        
    }

    protected boolean eliminateDeadStores(DecacCompiler compiler) {
        return false;
    }


    protected boolean eliminateUnusedVars(DecacCompiler compiler, Set<Symbol> used) {
        return false;
    }
}
