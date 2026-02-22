package fr.ensimag.deca.tools;

import java.io.PrintStream;

/**
 *
 * @author gl43
 * @date 01/01/2026
 */


public class PrettyPrintStream extends IndentPrintStream {
    public PrettyPrintStream(PrintStream s) {
        super(s);
    }

    @Override
    protected void printIndent() {
        if (this.indented) {
            return;
        }
        for (int i = 0; i < getIndent(); i++) {
            /* super.print("    "); */ // This gives a recursion error as super.print calls printIdent itself
            stream.print("    ");
        }
        this.indented = true;
    }


}
