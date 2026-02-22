package fr.ensimag.deca.tree;

import fr.ensimag.deca.CompilerOptions;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestFloatLiteral {
    @Test
    public void testVerifyExprFloat() throws ContextualError {
        DecacCompiler compiler = new DecacCompiler(new CompilerOptions(), null);
        FloatLiteral f = new FloatLiteral(1.5f);
        assertTrue(f.verifyExpr(compiler, null, null).isFloat());
    }

    @Test
    public void testDecompile(){
        FloatLiteral f = new FloatLiteral(1.5f);

        assertDoesNotThrow(()->f.decompile(System.out));
    }
}