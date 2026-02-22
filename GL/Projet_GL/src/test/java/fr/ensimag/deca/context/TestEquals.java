package fr.ensimag.deca.context;

import fr.ensimag.deca.CompilerOptions;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tree.AbstractExpr;
import fr.ensimag.deca.tree.ConvFloat;
import fr.ensimag.deca.tree.Equals;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;

/**
 * Test for the Equals node using mockito, using @Mock and @Before annotations.
 *
 * @author Ensimag
 * @date 01/01/2026
 */
public class TestEquals {

    


    @Mock
    AbstractExpr intexpr1;
    @Mock
    AbstractExpr intexpr2;
    @Mock
    AbstractExpr floatexpr1;
    @Mock
    AbstractExpr floatexpr2;
    @Mock
    AbstractExpr booleanexpr1;
    @Mock
    AbstractExpr booleanexpr2;




    DecacCompiler compiler;

    
    @BeforeEach
    public void setup() throws ContextualError {
        MockitoAnnotations.initMocks(this);
        compiler = new DecacCompiler(null, null);

        Type INT = compiler.environmentType.INT;
        Type FLOAT = compiler.environmentType.FLOAT;
        Type BOOLEAN = compiler.environmentType.BOOLEAN;

        when(intexpr1.verifyExpr(compiler, null, null)).thenReturn(INT);
        when(intexpr2.verifyExpr(compiler, null, null)).thenReturn(INT);
        when(floatexpr1.verifyExpr(compiler, null, null)).thenReturn(FLOAT);
        when(floatexpr2.verifyExpr(compiler, null, null)).thenReturn(FLOAT);
        when(booleanexpr1.verifyExpr(compiler, null, null)).thenReturn(BOOLEAN);
        when(booleanexpr2.verifyExpr(compiler, null, null)).thenReturn(BOOLEAN);

    }

    @Test
    public void testIntInt() throws ContextualError {
        Equals t = new Equals(intexpr1, intexpr2);
        // check the result
        assertTrue(t.verifyExpr(compiler, null, null).isBoolean());
        // check that the mocks have been called properly.
        verify(intexpr1, atMost(2)).verifyExpr(compiler, null, null);
        verify(intexpr2).verifyExpr(compiler, null, null);
    }

    @Test
    public void testIntFloat() throws ContextualError {
        Equals t = new Equals(intexpr1, floatexpr1);
        // check the result
        assertTrue(t.verifyExpr(compiler, null, null).isBoolean());
        // ConvFloat should have been inserted on the right side
/*         assertTrue(t.getLeftOperand() instanceof ConvFloat);
        assertFalse(t.getRightOperand() instanceof ConvFloat); */
        // check that the mocks have been called properly.
        verify(intexpr1, atMost(2)).verifyExpr(compiler, null, null);
        verify(floatexpr1, atMost(2)).verifyExpr(compiler, null, null);
    }

    @Test
    public void testFloatInt() throws ContextualError {
        Equals t = new Equals(floatexpr1, intexpr1);
        // check the result
        assertTrue(t.verifyExpr(compiler, null, null).isBoolean());
        // ConvFloat should have been inserted on the right side
/*         assertTrue(t.getRightOperand() instanceof ConvFloat);
        assertFalse(t.getLeftOperand() instanceof ConvFloat); */
        // check that the mocks have been called properly.
        verify(intexpr1, atMost(2)).verifyExpr(compiler, null, null);
        verify(floatexpr1, atMost(2)).verifyExpr(compiler, null, null);
    }

    @Test
    public void testIntBoolean() throws ContextualError {
        Equals t = new Equals(intexpr1, booleanexpr1);
        // check the result
        assertThrows(
            ContextualError.class,() -> t.verifyExpr(compiler, null, null));
        /* // ConvFloat should have been inserted on the right side
        assertTrue(t.getRightOperand() instanceof ConvFloat);
        assertFalse(t.getLeftOperand() instanceof ConvFloat); */
        // check that the mocks have been called properly.
        verify(intexpr1, atMost(2)).verifyExpr(compiler, null, null);
        verify(booleanexpr1).verifyExpr(compiler, null, null);
    }

    @Test
    public void testBooleanBoolean() throws ContextualError {
        Equals t = new Equals(booleanexpr2, booleanexpr1);
        // check the result
        assertTrue(t.verifyExpr(compiler, null, null).isBoolean());
/*         assertTrue(t.getRightOperand() instanceof ConvFloat);
        assertFalse(t.getLeftOperand() instanceof ConvFloat); */
        // check that the mocks have been called properly.
        verify(booleanexpr2).verifyExpr(compiler, null, null);
        verify(booleanexpr1).verifyExpr(compiler, null, null);
    }

    //ajout
    @Test
    public void testEqualsBooleanInt_Error() throws ContextualError {
        when(booleanexpr1.verifyExpr(compiler, null, null))
            .thenReturn(compiler.environmentType.BOOLEAN);
        when(intexpr1.verifyExpr(compiler, null, null))
            .thenReturn(compiler.environmentType.INT);

        Equals t = new Equals(booleanexpr1, intexpr1);

        assertThrows(
            ContextualError.class,
            () -> t.verifyExpr(compiler, null, null)
        );
    }

//     @Test
// public void testDeclareClass() {
//    compiler = new DecacCompiler(new CompilerOptions(), null)
//    envType = compiler.environmentType

//    Symbol A
//    ClassDefinition def

//    envType.declareClass(A, def)
//    assertNotNull(envType.defOfType(A))
// }

// @Test
// public void testDoubleClassDeclarationThrows() {
//    declare same class twice
//    expect DoubleDefException
// }


}