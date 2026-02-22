package fr.ensimag.deca.tree;

import fr.ensimag.deca.tools.IndentPrintStream;
import java.io.PrintStream;
import org.apache.commons.lang.Validate;

import java.util.Set;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

/**
 * Unary expression.
 *
 * @author gl43
 * @date 01/01/2026
 */
public abstract class AbstractUnaryExpr extends AbstractExpr {

    public AbstractExpr getOperand() {
        return operand;
    }
    private AbstractExpr operand;
    public AbstractUnaryExpr(AbstractExpr operand) {
        Validate.notNull(operand);
        this.operand = operand;
    }


    protected abstract String getOperatorName();
  
    @Override
    public void decompile(IndentPrintStream s) {
        //throw new UnsupportedOperationException("not yet implemented");
        s.print(getOperatorName());
        s.print("(");
        getOperand().decompile(s);
        s.print(")");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        operand.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        operand.prettyPrint(s, prefix, true);
    }

    //extension
    protected void setOperand(AbstractExpr operand) {
        Validate.notNull(operand);
        this.operand = operand;
    }

     @Override
    protected void collectReadVars(Set<Symbol> reads) {
        getOperand().collectReadVars(reads);
    }

}
