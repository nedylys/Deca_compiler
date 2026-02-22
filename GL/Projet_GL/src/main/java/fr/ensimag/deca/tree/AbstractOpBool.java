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
public abstract class AbstractOpBool extends AbstractBinaryExpr {

    public AbstractOpBool(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        //throw new UnsupportedOperationException("not yet implemented");
        Type leftOperand_Type = this.getLeftOperand().verifyExpr(compiler, localEnv, currentClass); // les verifyExpr retournent tous les types
        Type rightOperand_Type = this.getRightOperand().verifyExpr(compiler, localEnv, currentClass);
        // Règle : On ne peut faire des opérations booléennes qu'entre des bool
        if (!leftOperand_Type.isBoolean()) {
            throw new ContextualError("Invalid left operand type for boolean operation: expected boolean", this.getLocation());
        }

        if (!rightOperand_Type.isBoolean()) {
            throw new ContextualError("Invalid right operand type for boolean operation: expected boolean", this.getLocation());
        }

        setType(leftOperand_Type); // Décoration de type (Il s'agit d'un Boolean)
        return leftOperand_Type;
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

        // cas 2 littéraux
        if (left instanceof BooleanLiteral && right instanceof BooleanLiteral) {
            boolean a = ((BooleanLiteral) left).getValue();
            boolean b = ((BooleanLiteral) right).getValue();
            boolean r = evalBool(a, b);
            BooleanLiteral res = new BooleanLiteral(r);
            res.setType(getType());
            res.setLocation(getLocation());
            return res;
        }

        // simplifications locales sûres
        if (this instanceof And) {
            // false && x => false ; x && false => false
            if (left instanceof BooleanLiteral && !((BooleanLiteral) left).getValue()) return left;
            if (right instanceof BooleanLiteral && !((BooleanLiteral) right).getValue()) return right;
            // true && x => x ; x && true => x
            if (left instanceof BooleanLiteral && ((BooleanLiteral) left).getValue()) return right;
            if (right instanceof BooleanLiteral && ((BooleanLiteral) right).getValue()) return left;
        }
        if (this instanceof Or) {
            // true || x => true ; x || true => true
            if (left instanceof BooleanLiteral && ((BooleanLiteral) left).getValue()) return left;
            if (right instanceof BooleanLiteral && ((BooleanLiteral) right).getValue()) return right;
            // false || x => x ; x || false => x
            if (left instanceof BooleanLiteral && !((BooleanLiteral) left).getValue()) return right;
            if (right instanceof BooleanLiteral && !((BooleanLiteral) right).getValue()) return left;
        }

        return this;
    }

    protected abstract boolean evalBool(boolean a, boolean b);
    
}