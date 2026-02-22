package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;

/**
 *
 * @author gl43
 * @date 01/01/2026
 */
public class Not extends AbstractUnaryExpr {

    public Not(AbstractExpr operand) {
        super(operand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        //throw new UnsupportedOperationException("not yet implemented");
        Type operand_Type = this.getOperand().verifyExpr(compiler, localEnv, currentClass); // les verifyExpr retournent tous les types
        // Règle : L'opérateur Not n'est défini que pour des booléens
        if (!operand_Type.isBoolean()) {
            throw new ContextualError("Invalid operand type for Not operation: expected boolean", this.getLocation());
        }
        setType(operand_Type); // Décoration de type (Il s'agit d'un Bool)
        return operand_Type;
    }


    @Override
    protected String getOperatorName() {
        return "!";
    }

    @Override
    /* MEA - 04/01 */
    /* Voir page 221 */
    protected void codeGenBool(DecacCompiler compiler, boolean b, Label E) {
        getOperand().codeGenBool(compiler, !b, E);
    }

    @Override
    public void codeGenExpr(DecacCompiler compiler, GPRegister r) {
        codeGenExprBool(compiler, r);
    }

    //extension
    @Override
    public AbstractExpr foldExpr(DecacCompiler compiler) {
        AbstractExpr op = getOperand().foldExpr(compiler);
        if (op != getOperand()) setOperand(op);

        if (op instanceof BooleanLiteral) {
            boolean v = ((BooleanLiteral) op).getValue();
            BooleanLiteral res = new BooleanLiteral(!v);
            res.setType(getType());
            res.setLocation(getLocation());
            return res;
        }
        return this;
    }
}
