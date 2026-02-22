package fr.ensimag.deca.tools;

import java.io.PrintStream;

/**
 *
 * @author gl43
 * @date 01/01/2026
 */
public class IndentPrintStream {
    //private PrintStream stream;
    protected final PrintStream stream;
    public IndentPrintStream(PrintStream stream) {
        this.stream = stream;
    }
    protected int indent = 0;
    protected boolean indented = false;

    protected void printIndent() {
        if (indented) {
            return;
        }
        for (int i = 0; i < indent; i++) {
            stream.print("\t");
        }
        indented = true;
    }
    public void print(String s) {
        printIndent();
        stream.print(s);
    }

    public void println() {
        stream.println();
        indented = false;
    }

    public void println(String s) {
        print(s);
        println();
    }

    public void indent() {
        indent++;
    }

    public void unindent() {
        indent--;
    }

    public void print(char charAt) {
        printIndent();
        stream.print(charAt);
    }

    public int getIndent() {
    return indent;
}

}
