package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mockito;

public class TestNot {

    @Test
    public void notOnBoolean() throws ContextualError {
        DecacCompiler compiler = new DecacCompiler(null, null);

        AbstractExpr expr = Mockito.mock(AbstractExpr.class);
        Mockito.when(expr.verifyExpr(compiler, null, null))
               .thenReturn(compiler.environmentType.BOOLEAN);

        Not n = new Not(expr);
        assertTrue(n.verifyExpr(compiler, null, null).isBoolean());
    }

}