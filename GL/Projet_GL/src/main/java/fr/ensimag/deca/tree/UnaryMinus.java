package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.instructions.OPP;
/**
 * @author gl43
 * @date 01/01/2026
 */
public class UnaryMinus extends AbstractUnaryExpr {

    public UnaryMinus(AbstractExpr operand) {
        super(operand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        //throw new UnsupportedOperationException("not yet implemented");
        Type otype = getOperand().verifyExpr(compiler, localEnv, currentClass);
        if (!otype.isInt() && !otype.isFloat()) {
        throw new ContextualError(
            "Unary minus can only be applied to int or float ",
            getOperand().getLocation()
        );
    }   
        setType(otype);
        return otype;

    }


    @Override
    protected String getOperatorName() {
        return "-";
    }

    @Override
    public void codeGenExpr(DecacCompiler compiler, GPRegister targetReg) {
        // Evalue l'opérande, résultat dans targetReg
        getOperand().codeGenExpr(compiler, targetReg);
        // Applique l'opposé : target = -targetReg
        compiler.addInstruction(new OPP(targetReg, targetReg));
    }

}
