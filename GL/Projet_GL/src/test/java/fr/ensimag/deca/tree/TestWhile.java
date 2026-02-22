package fr.ensimag.deca.tree;

import fr.ensimag.deca.CompilerOptions;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mockito;

public class TestWhile {

    @Test
    public void testWhileWithBooleanCondition() throws ContextualError {
        DecacCompiler compiler = new DecacCompiler(new CompilerOptions(), null);

        // condition = boolean
        AbstractExpr condition = Mockito.mock(AbstractExpr.class);
        Mockito.when(condition.verifyExpr(compiler, null, null))
               .thenReturn(compiler.environmentType.BOOLEAN);

        ListInst body = new ListInst();

        While whileInst = new While(condition, body);

        assertDoesNotThrow(() -> {
            whileInst.verifyInst(compiler, null, null, null);
        });
    }
    
}