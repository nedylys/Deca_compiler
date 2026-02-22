package fr.ensimag.deca.context;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.deca.tree.Location;


public class TestEnvironmentExp {
    @Test
    public void lookupUnknownSymbol_returnsNull() {
        DecacCompiler compiler = new DecacCompiler(null, null);
        EnvironmentExp env = new EnvironmentExp(null);

        SymbolTable.Symbol x = compiler.symbolTable.create("x");

        assertNull(env.get(x));
    }

    @Test
    public void declareVariableValide() throws DoubleDefException { //test2
        DecacCompiler compiler = new DecacCompiler(null, null);
        EnvironmentExp env = new EnvironmentExp(null);

        SymbolTable.Symbol x = compiler.symbolTable.create("x");

        VariableDefinition def = new VariableDefinition(
            compiler.environmentType.INT,
            Location.BUILTIN
        );

        env.declare(x, def);

        assertEquals(def, env.get(x));
    }

    @Test
    public void doubleDeclaration_throwsException() throws DoubleDefException {
        DecacCompiler compiler = new DecacCompiler(null, null);
        EnvironmentExp env = new EnvironmentExp(null);

        SymbolTable.Symbol x = compiler.symbolTable.create("x");

        VariableDefinition def1 = new VariableDefinition(
            compiler.environmentType.INT,
            Location.BUILTIN
        );

        VariableDefinition def2 = new VariableDefinition(
            compiler.environmentType.INT,
            Location.BUILTIN
        );

        assertThrows(
        DoubleDefException.class,
        () -> {
            env.declare(x, def1);
            env.declare(x, def2);
        }
    );
    }

    @Test
    public void lookupInParentEnv() throws DoubleDefException {
        DecacCompiler compiler = new DecacCompiler(null, null);
        EnvironmentExp envparent = new EnvironmentExp(null);
        EnvironmentExp envchild = new EnvironmentExp(envparent);

        SymbolTable.Symbol x = compiler.symbolTable.create("x");

        VariableDefinition def = new VariableDefinition(
            compiler.environmentType.INT,
            Location.BUILTIN
        );


        envparent.declare(x, def);


        assertEquals(def, envchild.get(x));
    }

    @Test
    public void childDeclarationOverridesParent() throws DoubleDefException {
        DecacCompiler compiler = new DecacCompiler(null, null);
        EnvironmentExp envparent = new EnvironmentExp(null);
        EnvironmentExp envchild = new EnvironmentExp(envparent);

        SymbolTable.Symbol x = compiler.symbolTable.create("x");

        VariableDefinition defparent = new VariableDefinition(
            compiler.environmentType.INT,
            Location.BUILTIN
        );

        VariableDefinition defchild = new VariableDefinition(
            compiler.environmentType.FLOAT,
            Location.BUILTIN
        );


        envparent.declare(x, defparent);
        envchild.declare(x, defchild);


        assertEquals(defchild, envchild.get(x));
    }

}