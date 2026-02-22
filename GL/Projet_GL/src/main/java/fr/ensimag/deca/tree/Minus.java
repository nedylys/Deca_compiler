package fr.ensimag.deca.tree;
import  fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.BinaryArithOpHelper;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.GPRegister;

/**
 * @author gl43
 * @date 01/01/2026
 */
public class Minus extends AbstractOpArith {
    public Minus(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }


    @Override
    protected String getOperatorName() {
        return "-";
    }

    //début section ajoutée
    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
       
        return super.verifyExpr(compiler, localEnv, currentClass);
    }
    //Fin section ajoutée
    
    @Override
    /* AT - MEA : implémentée le 03/01 */
    public void codeGenExpr(DecacCompiler compiler, GPRegister targetReg) {
        BinaryArithOpHelper.genCodeBinaryArithOp(compiler, getLeftOperand(), 
            getRightOperand(), targetReg, BinaryArithOpHelper.ArithOp.SUB);
    }

    //extension
    @Override
    public AbstractExpr foldExpr(DecacCompiler compiler) {
        AbstractExpr left = getLeftOperand().foldExpr(compiler);
        AbstractExpr right = getRightOperand().foldExpr(compiler);

        if (left != getLeftOperand()) setLeftOperand(left);
        if (right != getRightOperand()) setRightOperand(right);

        // int - int
        if (left instanceof IntLiteral && right instanceof IntLiteral) {
            int a = ((IntLiteral) left).getValue();
            int b = ((IntLiteral) right).getValue();
            IntLiteral resultat = new IntLiteral(a - b);
            resultat.setType(getType());      // garder la déco si nécessaire
            resultat.setLocation(getLocation());
            return resultat;
        }

        // float - float
        if (left instanceof FloatLiteral && right instanceof FloatLiteral) {
            float a = ((FloatLiteral) left).getValue();
            float b = ((FloatLiteral) right).getValue();
            FloatLiteral resultat = new FloatLiteral(a - b);
            resultat.setType(getType());
            resultat.setLocation(getLocation());
            return resultat;
        }

        return this;
    }

    //extension
    @Override
    protected int evalInt(int a, int b) { return a - b; }

    @Override
    protected float evalFloat(float a, float b) { return a - b; }

}
