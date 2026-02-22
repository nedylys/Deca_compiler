package fr.ensimag.deca.tree;

import java.util.Set;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.Definition;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

/**
 * Class declaration.
 *
 * @author gl43
 * @date 01/01/2026
 */
public abstract class AbstractDeclClass extends Tree {

    /**
     * Pass 1 of [SyntaxeContextuelle]. Verify that the class declaration is OK
     * without looking at its content.
     * Must typically:
     *    create/install the {@link fr.ensimag.deca.context.ClassDefinition};
     *    resolve and validate the super-class (if any);
     *    reject redefinitions / forbidden inheritance.
     */
    protected abstract void verifyClass(DecacCompiler compiler)
            throws ContextualError;

    /**
     * Pass 2 of [SyntaxeContextuelle]. Verify that the class members (fields and
     * methods) are OK, without looking at method body and field initialization.
     * Must typically:
     *       declare fields in the class member environment;
     *       declare methods with their {@link fr.ensimag.deca.context.Signature};
     *       check name conflicts and overriding rules.
     */
    protected abstract void verifyClassMembers(DecacCompiler compiler)
            throws ContextualError;

    /**
     * Pass 3 of [SyntaxeContextuelle]. Verify that instructions and expressions
     * contained in the class are OK.
     * Must typically:
     *       check method bodies via {@code verifyInst}/{@code verifyExpr};
     *       check field initializations;
     *       decorate the AST with computed types.
     */
    protected abstract void verifyClassBody(DecacCompiler compiler)
            throws ContextualError;

    // Je sais pas si c'est la meilleure solution de les mettre ici 
    public abstract void codeGenClass(DecacCompiler compiler);
    public abstract void codeGenClassMthd(DecacCompiler compiler);
    public abstract AbstractIdentifier getName();

    //extension
    public boolean foldConstants(DecacCompiler compiler){
        return false;
    }

    protected boolean eliminateDeadCode(DecacCompiler compiler){
        return false;
    }

    protected boolean eliminateDeadStores(DecacCompiler compiler) {
        return false;
    }


    protected boolean eliminateUnusedVars(DecacCompiler compiler) {
        return false;
    }

    protected void collectReadVars(Set<Symbol> reads) {
        
    }


    protected void collectWrittenVars(Set<Symbol> writes) {
        
    }
    
}
