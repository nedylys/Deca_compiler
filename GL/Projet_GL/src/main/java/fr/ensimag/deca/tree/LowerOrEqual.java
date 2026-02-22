package fr.ensimag.deca.tree;
import  fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.CompareBoolHelper;
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
public class LowerOrEqual extends AbstractOpIneq {
    public LowerOrEqual(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }


    @Override
    protected String getOperatorName() {
        return "<=";
    }

    //début section ajoutée
    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
       
        return super.verifyExpr(compiler, localEnv, currentClass);
    }
    //Fin section ajoutée

    @Override
    protected void codeGenBool(DecacCompiler compiler, boolean b, Label E) {
        CompareBoolHelper.genCodeCmpOp(compiler, getLeftOperand(), getRightOperand(),
         b, E, CompareBoolHelper.CmpOp.LE);
    }

    //extension
    @Override protected boolean evalInt(int a, int b) { return a <= b; }
    @Override protected boolean evalFloat(float a, float b) { return a <= b; }


}
