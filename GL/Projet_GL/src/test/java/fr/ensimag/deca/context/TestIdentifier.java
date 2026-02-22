package fr.ensimag.deca.context;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.lang.instrument.ClassDefinition;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tree.AbstractProgram;
import fr.ensimag.deca.tree.Identifier;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.FieldDefinition;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.TypeDefinition;
import fr.ensimag.deca.context.VariableDefinition;
import fr.ensimag.deca.syntax.DecaLexer;
import fr.ensimag.deca.syntax.DecaParser;
import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.deca.syntax.AbstractDecaParser;
import fr.ensimag.deca.context.EnvironmentExp.*;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.tree.Location;



public class TestIdentifier {
    @Test
    public void testIdentifierNotDeclared() {
        DecacCompiler compiler = new DecacCompiler(null, null);
        EnvironmentExp env = new EnvironmentExp(null);

        Identifier id = new Identifier(
            compiler.symbolTable.create("x")
        );

        assertThrows(
            ContextualError.class,
            () -> id.verifyExpr(compiler, env, null)
        );
    }


    @Test
    public void identifierDeclared() throws DoubleDefException {
        DecacCompiler compiler = new DecacCompiler(null, null);
        EnvironmentExp env = new EnvironmentExp(null);

        Identifier id = new Identifier(
            compiler.symbolTable.create("x")
        );

        env.declare(
            id.getName(),
            new fr.ensimag.deca.context.VariableDefinition(
                compiler.environmentType.INT,
                null
            )
        );

        assertDoesNotThrow(() -> {
            id.verifyExpr(compiler, env, null);
        });
    }

    @Test
    public void verifyTypeIntValid() throws ContextualError {
        DecacCompiler compiler = new DecacCompiler(null, null);
        Identifier id = new Identifier(
            compiler.symbolTable.create("int")
        );
        Type t = id.verifyType(compiler);
        assertTrue(t.isInt());
    }

// This test is never valid as every single ExpDefinition is an expression
/*     @Test
    void identifierDeclaredButNotExpression_throwsError() throws DoubleDefException {
        DecacCompiler compiler = new DecacCompiler(null, null);
        EnvironmentExp env = new EnvironmentExp(null);

        Identifier id = new Identifier(
            compiler.symbolTable.create("x")
        );

        // On déclare un TYPE dans l'environnement d'expressions (interdit)
/*         env.declare(
            id.getName(),
            (new TypeDefinition(compiler.environmentType.INT, Location.BUILTIN))
        ); */
        /*env.declare(
            id.getName(),
            (new FieldDefinition(compiler.environmentType.INT, Location.BUILTIN, Visibility.PUBLIC, null, 0)));

        assertThrows(
            ContextualError.class,
            () -> id.verifyExpr(compiler, env, null)
        );
    } */

    @Test
    public void verifyTypeUnknownTypeError() {
        DecacCompiler compiler = new DecacCompiler(null,null);

        Identifier id = new Identifier(
            compiler.symbolTable.create("UnknownType")
        );

        assertThrows(
            ContextualError.class,
            () -> id.verifyType(compiler)
        );
    }

    

}

