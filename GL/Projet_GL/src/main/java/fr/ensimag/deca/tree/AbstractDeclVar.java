package fr.ensimag.deca.tree;

import java.util.Set;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

/**
 * Variable declaration
 *
 * @author gl43
 * @date 01/01/2026
 */
public abstract class AbstractDeclVar extends Tree {
    
    /**
     * Implements non-terminal "decl_var" of [SyntaxeContextuelle] in pass 3
     * @param compiler contains "env_types" attribute
     * @param localEnv 
     *   its "parentEnvironment" corresponds to the "env_exp_sup" attribute
     *   in precondition, its "current" dictionary corresponds to 
     *      the "env_exp" attribute
     *   in postcondition, its "current" dictionary corresponds to 
     *      the synthetized attribute
     * @param currentClass 
     *          corresponds to the "class" attribute (null in the main bloc).
     */    
    protected abstract void verifyDeclVar(DecacCompiler compiler,
            EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError;

    /* MEA */
    protected abstract void codeGenDeclVar(DecacCompiler compiler) ;
    public abstract void codeGenDeclVarClass(DecacCompiler compiler) ;
    public abstract void codeGenDeclVarMthd(DecacCompiler compiler,int index);
    
    public abstract AbstractIdentifier getType();

    //extension
    public boolean foldConstants(DecacCompiler compiler){
        return false;
    }

    public void collectReadVars(Set<Symbol> reads) {}

}
