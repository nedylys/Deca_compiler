package fr.ensimag.deca.tree;

import fr.ensimag.deca.CompilerOptions;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestBooleanLiteral {

    @Test
    public void testTrueLiteral() throws ContextualError {
        DecacCompiler compiler = new DecacCompiler(new CompilerOptions(), null);

        BooleanLiteral b = new BooleanLiteral(true);

        assertTrue(
            b.verifyExpr(compiler, null, null).isBoolean()
        );
    }

    @Test
    public void testFalseLiteral() throws ContextualError {
        DecacCompiler compiler = new DecacCompiler(new CompilerOptions(), null);

        BooleanLiteral b = new BooleanLiteral(false);

        assertTrue(
            b.verifyExpr(compiler, null, null).isBoolean()
        );
    }
}