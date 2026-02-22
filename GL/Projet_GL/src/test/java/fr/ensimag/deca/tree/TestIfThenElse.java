package fr.ensimag.deca.tree;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.CompilerOptions;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ClassDefinition;

public class TestIfThenElse {

    @Test
    public void testIfThenElseValid() throws ContextualError {
        DecacCompiler compiler = new DecacCompiler(new CompilerOptions(), null);

        // condition boolean 
        AbstractExpr condition = Mockito.mock(AbstractExpr.class);
        Mockito.when(condition.verifyExpr(compiler, null, null))
               .thenReturn(compiler.environmentType.BOOLEAN);

        ListInst listthen = new ListInst();
        ListInst listelse = new ListInst();

        IfThenElse ifthenelse = new IfThenElse(condition, listthen, listelse);

        // doit passer sans exception
        assertDoesNotThrow(() -> {
            ifthenelse.verifyInst(compiler, null, null, null);
        });
    }

    @Test
    public void testIfThenElseNonBooleanCondition() throws ContextualError {
        DecacCompiler compiler = new DecacCompiler(new CompilerOptions(), null);

        // 
        AbstractExpr condition = Mockito.mock(AbstractExpr.class);
        Mockito.when(condition.verifyExpr(compiler, null, null))
            .thenReturn(compiler.environmentType.INT);

        ListInst listthen = new ListInst();
        ListInst listelse = new ListInst();

        IfThenElse ifthenelse = new IfThenElse(condition, listthen, listelse);

        assertThrows(ContextualError.class, () -> {
            ifthenelse.verifyInst(compiler, null, null, null);
        });
    }

    @Test
    public void testIfThenWithoutElse() throws ContextualError {
        DecacCompiler compiler = new DecacCompiler(new CompilerOptions(), null);

        AbstractExpr condition = Mockito.mock(AbstractExpr.class);
        Mockito.when(condition.verifyExpr(compiler, null, null))
            .thenReturn(compiler.environmentType.BOOLEAN);

        ListInst listthen = new ListInst();

        IfThenElse ifthenelse = new IfThenElse(condition, listthen, new ListInst());

        assertDoesNotThrow(() -> {
            ifthenelse.verifyInst(compiler, null, null, null);
        });
    }


}
