package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mockito;

public class TestAnd {

    @Test
    public void andBooleanBoolean() throws ContextualError {
        DecacCompiler compiler = new DecacCompiler(null, null);

        AbstractExpr left = Mockito.mock(AbstractExpr.class);
        AbstractExpr right = Mockito.mock(AbstractExpr.class);

        Mockito.when(left.verifyExpr(compiler, null, null))
               .thenReturn(compiler.environmentType.BOOLEAN);
        Mockito.when(right.verifyExpr(compiler, null, null))
               .thenReturn(compiler.environmentType.BOOLEAN);

        And a = new And(left, right);
        assertTrue(a.verifyExpr(compiler, null, null).isBoolean());
    }


}