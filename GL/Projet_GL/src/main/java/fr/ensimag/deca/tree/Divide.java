package fr.ensimag.deca.tree;
import  fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.BinaryArithOpHelper;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.GPRegister;

/**
 *
 * @author gl43
 * @date 01/01/2026
 */
public class Divide extends AbstractOpArith {
    public Divide(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }


    @Override
    protected String getOperatorName() {
        return "/";
    }

    //début section ajoutée
    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
       
        return super.verifyExpr(compiler, localEnv, currentClass);
    }
    //Fin section ajoutée

    @Override
    /* AT - MEA : implémentée le 03/01 
    modifiée le 07/01 par MEA suite à la factorisation */
    public void codeGenExpr(DecacCompiler compiler, GPRegister targetReg) {
        BinaryArithOpHelper.genCodeBinaryArithOp(compiler, 
            getLeftOperand(), getRightOperand(), targetReg, 
            getType().isInt() ? BinaryArithOpHelper.ArithOp.DIV_INT 
            : BinaryArithOpHelper.ArithOp.DIV_FLOAT);
    }

    //extension
    @Override
    protected boolean canFoldInt(int a, int b) { return b != 0; }

    @Override
    protected boolean canFoldFloat(float a, float b) { return b != 0.0f; }

    @Override
    protected int evalInt(int a, int b) { return a / b; }

    @Override
    protected float evalFloat(float a, float b) { return a / b; }

}
