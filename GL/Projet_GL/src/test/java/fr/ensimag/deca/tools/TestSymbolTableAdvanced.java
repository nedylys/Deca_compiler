package fr.ensimag.deca.tools;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class TestSymbolTableAdvanced {

    @Test
    public void testCreateSameSymbolReturnsSameObject() {
        SymbolTable table = new SymbolTable();

        SymbolTable.Symbol s1 = table.create("x");
        SymbolTable.Symbol s2 = table.create("x");

        assertSame(s1, s2);
    }

    @Test
    public void testDifferentSymbols() {
        SymbolTable table = new SymbolTable();

        SymbolTable.Symbol s1 = table.create("x");
        SymbolTable.Symbol s2 = table.create("y");

        assertNotEquals(s1, s2);
    }

    @Test
    public void testSymbolName() {
        SymbolTable table = new SymbolTable();

        SymbolTable.Symbol s = table.create("hello");

        assertEquals("hello", s.getName());
    }
}