package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.ima.pseudocode.Label;
import java.util.Set;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
/* I tried to follow the structure provided in AbstractDeclVar.java */

/**
 * Method declaration (signature)
 *
 * @author gl43
 * @date 23/12/2026
 */
public abstract class AbstractDeclMethod extends Tree {

    /**
    * Pass 2 of step B : Method Signature 
    * @param compiler contains "env_types" attribute 
    * @param currentClass  */

    protected abstract void verifyDeclMethod(DecacCompiler compiler, ClassDefinition currentClass) throws ContextualError;

    /**
     * Pass 3: validate the method body.
     *
     * Must typically:
     *   build a local {@link fr.ensimag.deca.context.EnvironmentExp} (params + local vars);
     *   validate each instruction using {@code verifyInst};
     *   validate expressions using {@code verifyExpr}/{@code verifyRValue};
     *   check return constraints w.r.t. the method return type.
     *
     * @param compiler contains (among others) the type environment {@code env_types}
     * @param currentClass definition of the current class
     */

    protected abstract void verifyDeclMethodBody(DecacCompiler compiler, ClassDefinition currentClass) throws ContextualError;

    /** @return identifier corresponding to the method name. */

    public abstract AbstractIdentifier getMethodName();
    public abstract void codeGenClass(DecacCompiler compiler,Label nomFinMthd);

    //extension
    public boolean foldConstants(DecacCompiler vompiler){
        return false;
    }

    protected boolean eliminateDeadCode(DecacCompiler compiler){
        return false;
    }

    protected boolean eliminateDeadStores(DecacCompiler compiler) {
        return false;
    }

    protected void collectReadVars(Set<Symbol> reads) {
        
    }

    protected void collectWrittenVars(Set<Symbol> writes) {
        
    }

    protected boolean eliminateUnusedVars(DecacCompiler compiler) {
        return false;
    }
}
