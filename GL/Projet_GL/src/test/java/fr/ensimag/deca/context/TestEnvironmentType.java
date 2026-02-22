package fr.ensimag.deca.context;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.deca.tree.IntLiteral;
import fr.ensimag.deca.tree.Location;
import fr.ensimag.ima.pseudocode.GPRegister;

public class TestEnvironmentType {

    @Test
    public void testDeclareNewClass() throws DoubleDefException {
        DecacCompiler compiler = new DecacCompiler(null, null);
        EnvironmentType envType = compiler.environmentType;

        // إنشاء symbol ديال class
        SymbolTable.Symbol symA = compiler.symbolTable.create("A");

        // تعريف class A
        ClassType classAType =
            new ClassType(symA, Location.BUILTIN, envType.OBJECT.getDefinition());

        ClassDefinition classADef =
            new ClassDefinition(classAType, Location.BUILTIN, null);

        // declare class
        envType.declareClass(symA, classADef);

        // check
        assertNotNull(envType.defOfType(symA));
        assertTrue(envType.defOfType(symA).getType().isClass());
    }

    @Test
    public void testDoubleClassDeclarationThrows() throws DoubleDefException {
        DecacCompiler compiler = new DecacCompiler(null, null);
        EnvironmentType envType = compiler.environmentType;

        SymbolTable.Symbol symA = compiler.symbolTable.create("A");

        ClassType classAType =
            new ClassType(symA, Location.BUILTIN, envType.OBJECT.getDefinition());

        ClassDefinition classADef =
            new ClassDefinition(classAType, Location.BUILTIN, null);

        // première déclaration
        envType.declareClass(symA, classADef);

        // deuxième déclaration → erreur
        assertThrows(
            DoubleDefException.class,
            () -> envType.declareClass(symA, classADef)
        );
    }

    @Test
    public void testLookupUnknownTypeReturnsNull() {
        DecacCompiler compiler = new DecacCompiler(null, null);
        EnvironmentType envType = compiler.environmentType;

        SymbolTable.Symbol unknown =
            compiler.symbolTable.create("Unknown");

        assertNull(envType.defOfType(unknown));
    }

    @Test
public void testIntLiteralCodeGen() {
    DecacCompiler compiler = new DecacCompiler(null, null);
    IntLiteral lit = new IntLiteral(42);
    lit.codeGenExpr(compiler, GPRegister.R1);
}
}