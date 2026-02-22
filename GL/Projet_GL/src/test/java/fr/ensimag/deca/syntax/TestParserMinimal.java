package fr.ensimag.deca.syntax;

import static org.junit.jupiter.api.Assertions.*;

import org.antlr.v4.runtime.*;
import org.junit.jupiter.api.Test;

import fr.ensimag.deca.tree.AbstractProgram;

public class TestParserMinimal {

    @Test
    public void testEmptyMainBlock() throws Exception {
        String source = "{ }";

        DecaLexer lexer = new DecaLexer(CharStreams.fromString(source));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        DecaParser parser = new DecaParser(tokens);

        AbstractProgram prog =
            parser.parseProgramAndManageErrors(System.err);

        assertNotNull(prog);
    }
}