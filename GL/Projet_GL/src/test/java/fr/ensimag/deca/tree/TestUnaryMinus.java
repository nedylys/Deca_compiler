package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mockito;

public class TestUnaryMinus {

    @Test
    public void unaryMinusOnInt() throws ContextualError {
        DecacCompiler compiler = new DecacCompiler(null, null);

        AbstractExpr expr = Mockito.mock(AbstractExpr.class);
        Mockito.when(expr.verifyExpr(compiler, null, null))
               .thenReturn(compiler.environmentType.INT);

        UnaryMinus u = new UnaryMinus(expr);
        assertTrue(u.verifyExpr(compiler, null, null).isInt());
    }

    @Test
    public void unaryMinusOnFloat() throws ContextualError {
        DecacCompiler compiler = new DecacCompiler(null, null);

        AbstractExpr expr = Mockito.mock(AbstractExpr.class);
        Mockito.when(expr.verifyExpr(compiler, null, null))
               .thenReturn(compiler.environmentType.FLOAT);

        UnaryMinus u = new UnaryMinus(expr);
        assertTrue(u.verifyExpr(compiler, null, null).isFloat());
    }

}