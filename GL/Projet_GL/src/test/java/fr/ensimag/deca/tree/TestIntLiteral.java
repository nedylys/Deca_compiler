package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestIntLiteral {
    @Test
    public void testVerifyExprInt() throws ContextualError {
        DecacCompiler compiler = new DecacCompiler(null, null);
        IntLiteral i = new IntLiteral(10);
        assertTrue(i.verifyExpr(compiler, null, null).isInt());
    }

    @Test
    public void testDecompile(){
        IntLiteral lit = new IntLiteral(5);
        assertDoesNotThrow(()->lit.decompile(System.out));
    }
}