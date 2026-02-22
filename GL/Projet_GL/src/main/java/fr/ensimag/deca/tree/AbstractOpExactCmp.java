package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
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
public abstract class AbstractOpExactCmp extends AbstractOpCmp {

    public AbstractOpExactCmp(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

@Override
public Type verifyExpr(DecacCompiler compiler,
        EnvironmentExp localEnv,
        ClassDefinition currentClass) throws ContextualError {

    Type leftOperandType = getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
    Type rightOperandType = getRightOperand().verifyExpr(compiler, localEnv, currentClass);

    // We can compare any 2 objects 1 and B 
    /* type_binary_op(op, T1, T2) ≜ boolean,
            si op ∈ {eq, neq}
            et (T1 = type_class(A) ou T1 = null)
            et (T2 = type_class(B) ou T2 = null) */
    if ((leftOperandType.isClass() || leftOperandType.isNull()) &&(rightOperandType.isClass() || rightOperandType.isNull())) {
        setType(compiler.environmentType.BOOLEAN);
        return compiler.environmentType.BOOLEAN;
    }

    if (leftOperandType.sameType(rightOperandType)) {
        setType(compiler.environmentType.BOOLEAN);
        return getType();
    }


    /* type_binary_op(op, T1, T2) ≜ boolean,
        si op ∈ {eq, neq, lt, gt, leq, geq}
        et (T1, T2) ∈ dom(type_arith_op) */
    else if (leftOperandType.isInt() && rightOperandType.isFloat()) {
        ConvFloat convLeft = new ConvFloat(getLeftOperand());
        convLeft.verifyExpr(compiler, localEnv, currentClass);
        convLeft.setLocation(this.getLocation());

        setLeftOperand(convLeft);

        setType(compiler.environmentType.BOOLEAN);
        return compiler.environmentType.BOOLEAN;
    }

    else if (rightOperandType.isInt() && leftOperandType.isFloat()) {
        ConvFloat convRight = new ConvFloat(getRightOperand());
        convRight.verifyExpr(compiler, localEnv, currentClass);
        convRight.setLocation(this.getLocation());

        setRightOperand(convRight);

        setType(compiler.environmentType.BOOLEAN);
        return compiler.environmentType.BOOLEAN;
    }


    else{
        throw new ContextualError("Incompatible types: cannot compare "+ leftOperandType.getName()+ " with " + rightOperandType.getName(), getLocation());
    }
}


    @Override
    public void codeGenExpr(DecacCompiler compiler, GPRegister r) {
        codeGenExprBool(compiler, r);
    }

    //extension
    @Override
    public AbstractExpr foldExpr(DecacCompiler compiler) {
        AbstractExpr left = getLeftOperand().foldExpr(compiler);
        AbstractExpr right = getRightOperand().foldExpr(compiler);
        if (left != getLeftOperand()) setLeftOperand(left);
        if (right != getRightOperand()) setRightOperand(right);

        // int-int
        if (left instanceof IntLiteral && right instanceof IntLiteral) {
            int a = ((IntLiteral) left).getValue();
            int b = ((IntLiteral) right).getValue();
            boolean r = evalInt(a, b); // abstract
            BooleanLiteral res = new BooleanLiteral(r);
            res.setType(getType());
            res.setLocation(getLocation());
            return res;
        }
        // float-float
        if (left instanceof FloatLiteral && right instanceof FloatLiteral) {
            float a = ((FloatLiteral) left).getValue();
            float b = ((FloatLiteral) right).getValue();
            boolean r = evalFloat(a, b);
            BooleanLiteral res = new BooleanLiteral(r);
            res.setType(getType());
            res.setLocation(getLocation());
            return res;
        }

        //bool-bool
        if (left instanceof BooleanLiteral && right instanceof BooleanLiteral) {
            boolean a = ((BooleanLiteral) left).getValue();
            boolean b = ((BooleanLiteral) right).getValue();
            boolean r = evalBool(a, b);
            BooleanLiteral res = new BooleanLiteral(r);
            res.setType(getType());
            res.setLocation(getLocation());
            return res;
        }

        //null
        if (left.getType().isNull() && right.getType().isNull()) {
            boolean r = evalNull();
            BooleanLiteral res = new BooleanLiteral(r);
            res.setType(getType());
            res.setLocation(getLocation());
            return res;
        }
        return this;
    }

    protected abstract boolean evalInt(int a, int b);
    protected abstract boolean evalFloat(float a, float b);
    protected abstract boolean evalBool(boolean a, boolean b);
    protected abstract boolean evalNull();
}
