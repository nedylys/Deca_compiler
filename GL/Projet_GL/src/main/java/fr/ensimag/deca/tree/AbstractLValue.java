package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.ima.pseudocode.DAddr;


import fr.ensimag.deca.context.*;

/**
 * Left-hand side value of an assignment.
 * 
 * @author gl43
 * @date 01/01/2026
 */
public abstract class AbstractLValue extends AbstractExpr {
    public abstract DAddr getAdress(DecacCompiler compiler) ;
    //public abstract DAddr getAdressMthd(); // Adresse pour les variable de méthodes.

    //ajout
    public Type verifyLValue(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        // vérif “lvalue” : existence + type, mais PAS de check d'initialisation
        return verifyExpr(compiler, localEnv, currentClass);
    }
    //
}
