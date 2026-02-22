package fr.ensimag.deca.tree;
import  fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.Label;



/**
 *
 * @author gl43
 * @date 01/01/2026
 */
public class Or extends AbstractOpBool {

    public Or(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    protected String getOperatorName() {
        return "||";
    }

    //début section ajoutée
    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
       
        return super.verifyExpr(compiler, localEnv, currentClass);
    }
    //Fin section ajoutée

    @Override
    protected void codeGenBool(DecacCompiler compiler, boolean b, Label E) {
        if (b) {
            getLeftOperand().codeGenBool(compiler, true, E) ;
            getRightOperand().codeGenBool(compiler, true, E) ;
        }
        else {
            int numLabel = compiler.getLabelCounter() ;
            Label etiq = new Label("E_fin." + numLabel) ;
            getLeftOperand().codeGenBool(compiler, true, etiq);
            getRightOperand().codeGenBool(compiler, false, E);
            compiler.addLabel(etiq);
        }
    }

    //extension
    @Override
    protected boolean evalBool(boolean a, boolean b) { return a || b; }
}
