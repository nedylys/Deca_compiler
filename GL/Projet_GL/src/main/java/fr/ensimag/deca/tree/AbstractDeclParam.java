package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;

/* I tried to follow the structure provided in AbstractDeclVar.java */
/**
 * Field declaration
 *
 * @author gl43
 * @date 24/12/2026
 */

public abstract class AbstractDeclParam extends Tree{
        /**
    * Pass 2 of step B : Method Signature 
    * @param compiler contains "env_types" attribute 
    * @param currentClass  */
    protected abstract Type verifyDeclParam(DecacCompiler compiler, EnvironmentExp localEnv) throws ContextualError;
    
    protected abstract void declareParam(EnvironmentExp localEnv) throws ContextualError;
    public abstract void setAdress(int index);    
    //extension
    public boolean foldConstants(DecacCompiler compiler){
        return false;
    }
    
}
