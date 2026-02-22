package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;

/**
 *
 * @author gl43
 * @date 01/01/2026
 */
public abstract class AbstractOpCmp extends AbstractBinaryExpr {

    public AbstractOpCmp(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    // F j'ai implémenté cette méthode :
    @Override
    public abstract Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError ;
            // J'ai mis des vérifs des opérands mais je pourrais bien rendre cette méthode abstraite 
            // et forcer les sous-classes à l'implémenter
            // en effet chaque type de comparaison a ses propres règles
            /* getLeftOperand().verifyExpr( compiler, localEnv, currentClass);
            getRightOperand().verifyExpr(compiler, localEnv, currentClass);
            return compiler.environmentType.BOOLEAN; */

   
   //extension

    protected abstract boolean evalInt(int a, int b);
    protected abstract boolean evalFloat(float a, float b);

}
