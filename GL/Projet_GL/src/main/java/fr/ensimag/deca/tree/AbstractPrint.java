package fr.ensimag.deca.tree;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.ErrorManager;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;


import java.util.Set;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
/**
 * Print statement (print, println, ...).
 *
 * @author gl43
 * @date 01/01/2026
 */
public abstract class AbstractPrint extends AbstractInst {

    private boolean printHex;
    private ListExpr arguments = new ListExpr();
    
    abstract String getSuffix();

    public AbstractPrint(boolean printHex, ListExpr arguments) {
        Validate.notNull(arguments);
        this.arguments = arguments;
        this.printHex = printHex;
    }

    public ListExpr getArguments() {
        return arguments;
    }

    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType)
            throws ContextualError {
        /* throw new UnsupportedOperationException("not yet implemented"); */
        for (AbstractExpr a : this.arguments.getList()) {
            Type type = a.verifyExpr(compiler, localEnv, currentClass); // type dynamique de a va permettre de tester si il est 
            //                                                          valide via les methodes isXXX de Type
            a.verifyRValue(compiler, localEnv, currentClass, type);
            if (type.isString() || type.isInt() || type.isFloat()){
                // Pour Hello world il faut avoir le type string

            }
            else{
                throw new ContextualError("Print only accepts string, int, and float arguments ", a.getLocation());
            }
        }
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        compiler.useError(ErrorManager.ErrorType.IO_ERROR);
        for (AbstractExpr a : getArguments().getList()) {
            a.codeGenPrint(compiler, printHex);
        }
    }

    private boolean getPrintHex() {
        return printHex;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        //throw new UnsupportedOperationException("not yet implemented");
        s.print("print");
        s.print(getSuffix());

        if (getPrintHex()) {
            s.print("x");
        }
        s.print("(");

        arguments.decompile(s);

        s.println(");");
        
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        arguments.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        arguments.prettyPrint(s, prefix, true);
    }

    //extension
    @Override
    public boolean foldConstants(DecacCompiler compiler){
        return arguments.foldConstants(compiler);
    }

    @Override
    protected void collectReadVars(Set<Symbol> reads) {
        for (AbstractExpr e : getArguments().getList()) {
            e.collectReadVars(reads);
        }
    }

}
