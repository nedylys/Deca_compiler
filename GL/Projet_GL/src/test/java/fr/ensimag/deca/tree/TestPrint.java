package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestPrint {
    @Test
    public void testPrintVerify() throws ContextualError {
        DecacCompiler compiler = new DecacCompiler(null, null);
        ListExpr args = new ListExpr();
        args.add(new IntLiteral(3));
        Print p = new Print(false, args);
        assertDoesNotThrow(() -> p.verifyInst(compiler, null, null, null));
    }

    @Test
    public void testPrintlnInst(){
        DecacCompiler compiler = new DecacCompiler(null, null);
        ListExpr args = new ListExpr();
        Print p = new Print(false, args);
        assertDoesNotThrow(() -> p.verifyInst(compiler, null, null, null));
    }
}