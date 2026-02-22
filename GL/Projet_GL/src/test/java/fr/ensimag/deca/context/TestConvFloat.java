package fr.ensimag.deca.context;

import fr.ensimag.deca.CompilerOptions;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tree.AbstractExpr;
import fr.ensimag.deca.tree.ConvFloat;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;

/**
 * Test for the ConvFloat node using mockito, without using advanced features.
 * @see TestConvFloat for more advanced examples.
 * @see TestConvFloatWithoutMock too see what would need to be written if the test
 * was done without using Mockito.
 *
 * @author Ensimag
 * @date 01/01/2026
 */
public class TestConvFloat {

    final Type INT = new IntType(null);
    final Type FLOAT = new FloatType(null);

    @Test
    public void testType() throws ContextualError {
        DecacCompiler compiler = new DecacCompiler(null, null);
        AbstractExpr intValue = Mockito.mock(AbstractExpr.class);
        when(intValue.verifyExpr(compiler, null, null)).thenReturn(INT);
        ConvFloat t = new ConvFloat(intValue);
        Type ttype = t.verifyExpr(compiler, null, null);
        // check the result
        assertTrue(ttype.isFloat());
        // check that the mocks have been called properly.
        verify(intValue).verifyExpr(compiler, null, null);
    }

    //ajout
    @Test
    public void testConvFloatOnFloat_NoError() throws ContextualError {
        DecacCompiler compiler = new DecacCompiler(null, null);

        AbstractExpr floatValue = Mockito.mock(AbstractExpr.class);
        when(floatValue.verifyExpr(compiler, null, null))
            .thenReturn(compiler.environmentType.FLOAT);

        ConvFloat t = new ConvFloat(floatValue);

        Type result = t.verifyExpr(compiler, null, null);

        assertTrue(result.isFloat());
        verify(floatValue).verifyExpr(compiler, null, null);
    }



}