package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
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
public class Modulo extends AbstractOpArith {

    public Modulo(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        //throw new UnsupportedOperationException("not yet implemented");
        Type leftOperand_Type = this.getLeftOperand().verifyExpr(compiler, localEnv, currentClass); // les verifyExpr retournent tous les types
        Type rightOperand_Type = this.getRightOperand().verifyExpr(compiler, localEnv, currentClass);
        // Règle : L'opérateur modulo n'est défini que pour des int
        if (!leftOperand_Type.isInt()) {
            throw new ContextualError("Invalid left operand type for modulo operation: expected integer ", this.getLocation());
        }
        if (!rightOperand_Type.isInt()) {
            throw new ContextualError("Invalid right operand type for modulo operation: expected integer", this.getLocation());
        }
        setType(leftOperand_Type); // Décoration de type (Il s'agit d'un Int)
        return leftOperand_Type;
    }


    @Override
    protected String getOperatorName() {
        return "%";
    }

    // @Override
    // /* AT - MEA : implémentée le 03/01 */
    // public void codeGenExpr(DecacCompiler compiler, GPRegister targetReg) {
    //     // Evalue l'opérande gauche, résultat dans targetReg
    //     getLeftOperand().codeGenExpr(compiler, targetReg);
    //     // Evalue l'opérande droite, résultat dan targetReg
    //     getRightOperand().codeGenExpr(compiler, GPRegister.getR(2));
    //     // REM = remainder (modulo)
    //     compiler.addInstruction(new REM(GPRegister.getR(2), targetReg));
    // }

    @Override
    public void codeGenExpr(DecacCompiler compiler, GPRegister targetReg) {
        BinaryArithOpHelper.genCodeBinaryArithOp(compiler, getLeftOperand(),
        getRightOperand(), targetReg, BinaryArithOpHelper.ArithOp.MOD);
    }

    //extension
    @Override
    protected boolean canFoldInt(int a, int b) { return b != 0; }

    @Override
    protected boolean canFoldFloat(float a, float b) { return false; }

    @Override
    protected int evalInt(int a, int b) { return a % b; }

    @Override
    protected float evalFloat(float a, float b) {
        throw new UnsupportedOperationException("Modulo not defined on float");
    }


}
