package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.Label;



/**
 *
 * @author gl43
 * @date 01/01/2026
 */
public class And extends AbstractOpBool {

    public And(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    protected String getOperatorName() {
        return "&&";
    }

    @Override
    protected void codeGenBool(DecacCompiler compiler, boolean b, Label E) {
        if (b) {
            int numLabel = compiler.getLabelCounter() ;
            Label etiq = new Label("E_Fin." + numLabel) ;
            getLeftOperand().codeGenBool(compiler, false, etiq);
            getRightOperand().codeGenBool(compiler, true, E);
            compiler.addLabel(etiq) ;
        } 
        else {
            getLeftOperand().codeGenBool(compiler, false, E);
            getRightOperand().codeGenBool(compiler, false, E);
        }
    }

    @Override
    public AbstractExpr foldExpr(DecacCompiler compiler) {
        
        AbstractExpr newL = getLeftOperand().foldExpr(compiler);
        AbstractExpr newR = getRightOperand().foldExpr(compiler);
        if (newL != getLeftOperand()) setLeftOperand(newL);
        if (newR != getRightOperand()) setRightOperand(newR);

        
        if (newL instanceof BooleanLiteral && newR instanceof BooleanLiteral) {
            boolean a = ((BooleanLiteral) newL).getValue();
            boolean b = ((BooleanLiteral) newR).getValue();
            BooleanLiteral res = new BooleanLiteral(a && b);
            res.setType(getType());
            res.setLocation(getLocation());
            return res;
        }

        return this;
    } //////// MDCT 16 Janv

    @Override
    protected boolean evalBool(boolean a, boolean b) { return a && b; }
}
