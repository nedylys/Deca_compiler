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
public abstract class AbstractOpIneq extends AbstractOpCmp {

    public AbstractOpIneq(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        // Il s'agit du non terminal Op_Ineq_Cmp qui derive vers tout operation de comparaison donc les types des operand doivent etre comparables
        //throw new UnsupportedOperationException("not yet implemented");
        Type leftOperand_Type = this.getLeftOperand().verifyExpr(compiler, localEnv, currentClass); // les verifyExpr retournent tous les types
        Type rightOperand_Type = this.getRightOperand().verifyExpr(compiler, localEnv, currentClass);
        if (!(leftOperand_Type.isInt() || leftOperand_Type.isFloat()) ||
            !(rightOperand_Type.isInt() || rightOperand_Type.isFloat()))  {
                // Règle : les types doivent être comparables pour une comparaison
                throw new ContextualError("Incompatible types: cannot compare  " + leftOperand_Type.getName() + " with " + rightOperand_Type.getName() + " as " + rightOperand_Type.getName() + " is not int/float",  this.getLocation());
            }
        // Conversion
        if (leftOperand_Type.isInt() && rightOperand_Type.isFloat()) {
            ConvFloat convLeft = new ConvFloat(getLeftOperand());
            convLeft.verifyExpr(compiler, localEnv, currentClass);
            convLeft.setLocation(this.getLocation());

            setLeftOperand(convLeft);
        } else if (leftOperand_Type.isFloat() && rightOperand_Type.isInt()) {
            ConvFloat convRight = new ConvFloat(getRightOperand());
            convRight.verifyExpr(compiler, localEnv, currentClass);
            convRight.setLocation(this.getLocation());

            setRightOperand(convRight);
        }
        setType(compiler.environmentType.BOOLEAN); // Décoration de type : le resultat d'une comparaison est toujours un boolean
        return compiler.environmentType.BOOLEAN;
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
        //int-float
        FloatLiteral lf = unwrapToFloatLiteral(left);
        FloatLiteral rf = unwrapToFloatLiteral(right);
        if (lf != null && rf != null) {
            boolean r = evalFloat(lf.getValue(), rf.getValue());
            BooleanLiteral res = new BooleanLiteral(r);
            res.setType(getType());
            res.setLocation(getLocation());
            return res;
        }

        return this;
    }

    private FloatLiteral unwrapToFloatLiteral(AbstractExpr e) {
        if (e instanceof FloatLiteral) return (FloatLiteral) e;
        if (e instanceof ConvFloat) {
            AbstractExpr op = ((ConvFloat) e).getOperand();
            if (op instanceof IntLiteral) {
                float v = ((IntLiteral) op).getValue();
                FloatLiteral f = new FloatLiteral(v);
                f.setType(e.getType()); // float
                f.setLocation(e.getLocation());
                return f;
            }
        }
        return null;
    }


    protected abstract boolean evalInt(int a, int b);
    protected abstract boolean evalFloat(float a, float b);

 }

