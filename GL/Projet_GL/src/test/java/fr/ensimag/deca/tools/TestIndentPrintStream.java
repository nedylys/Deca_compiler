package fr.ensimag.deca.tools;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.Test;

public class TestIndentPrintStream {

    @Test
    public void testIndentation() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IndentPrintStream ips = new IndentPrintStream(new PrintStream(out));

        ips.println("a");
        ips.indent();
        ips.println("b");
        ips.unindent();
        ips.println("c");

        String result = out.toString();

        // basic sanity checks
        assertTrue(result.contains("a"));
        assertTrue(result.contains("b"));
        assertTrue(result.contains("c"));

        // check that b is indented (not at column 0)
        String[] lines = result.split("\n");
        boolean foundIndentedB = false;

        for (String line : lines) {
            if (line.trim().equals("b") && !line.startsWith("b")) {
                foundIndentedB = true;
            }
        }

        assertTrue(foundIndentedB, "Line 'b' should be indented");
    }

    @Test
    public void testMultipleIdent(){
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IndentPrintStream i = new IndentPrintStream(new PrintStream(out));

        i.indent();
        i.indent();
        i.println("X");

        assertTrue(out.toString().contains("X"));
    }
}