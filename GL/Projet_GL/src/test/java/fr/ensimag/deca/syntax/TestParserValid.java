package fr.ensimag.deca.syntax;

import static org.junit.jupiter.api.Assertions.*;

import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tree.AbstractProgram;

public class TestParserValid {

    @Test
    public void testSimpleValidProgram() {
        String source = "{ int a; a = 5; }";

        DecaLexer lexer = new DecaLexer(
            org.antlr.v4.runtime.CharStreams.fromString(source)
        );
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        DecaParser parser = new DecaParser(tokens);

        DecacCompiler compiler = new DecacCompiler(null, null);
        parser.setDecacCompiler(compiler);

        AbstractProgram program = parser.parseProgramAndManageErrors(System.err);

        assertNotNull(program);
    }

    
}