package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;

/* I tried to follow the structure provided in AbstractDeclVar.java */
/**
 * Field declaration
 *
 * @author gl43
 * @date 24/12/2026
 */
public abstract class AbstractDeclField extends Tree{
    /**
     * Pass 2 : checks and declares a field in the current class.
     *
     * Exemples :
     *   The field type is valid.
     *   No duplicate names in the members.
     *
    * @param compiler contains "env_types" attribute 
    * @param currentClass
    * @throws ContextualError if type is invalide, duplicate, conflict with inheritance, etc.  */

    protected abstract void verifyDeclField(DecacCompiler compiler, ClassDefinition currentClass) throws ContextualError;

    /**
     * Pass 3: validate the field initialization (if an initialization exists).
     *
     * Must typically:
     *   check assignment compatibility using {@code rvalue} / type rules;
     *   insert an implicit conversion if needed (e.g. int -> float).
     *
     * @param compiler contains (among others) the type environment {@code env_types}
     * @param currentClass definition of the current class
     * @throws fr.ensimag.deca.context.ContextualError if the initialization is incompatible
     */

    protected abstract void verifyInitializationOfField(DecacCompiler compiler, ClassDefinition currentClass)  throws ContextualError;

    //extension
    public boolean foldConstants(DecacCompiler compiler){
        return false;
    }
}
