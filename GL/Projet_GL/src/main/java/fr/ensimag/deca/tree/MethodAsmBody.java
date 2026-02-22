package fr.ensimag.deca.tree;

import java.io.PrintStream;
import java.util.Set;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.AsmInstruction;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.deca.context.Definition;


public class MethodAsmBody extends AbstractMethodBody {
    // MethodAsmBody [ StringLiteral ] (page 85)
    /*It is a code that is directly implemented in assembly */
    
    final private StringLiteral assemblyLiteral;

    public MethodAsmBody(StringLiteral assemLiteral){
        this.assemblyLiteral = assemLiteral;
    }

    @Override
    protected void verifyMethodBody(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass, Type type) throws ContextualError{
        // Contextualy, there is nothing to verify as this is an asssembly code (step C),
        // In fact there is no condition associated to it  (MethodAsmBody [ StringLiteral ])
        Type t = assemblyLiteral.verifyExpr(compiler, localEnv, currentClass);
    }


    @Override
    public void decompile(IndentPrintStream s) {
        // Decompile Children
        s.print("asm(");
        assemblyLiteral.decompile(s);
        s.print(")");
    }


    @Override
    protected void iterChildren(TreeFunction f) {
        // iterate on Children
        assemblyLiteral.iter(f);
    }


    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // print Children
        assemblyLiteral.prettyPrint(s, prefix, true);
    }

    @Override
    public void codeGenClass(DecacCompiler compiler,Label nomFinmthd){
        compiler.addInstruction(new AsmInstruction(assemblyLiteral.getValue()));
        compiler.addLabel(nomFinmthd);
    }
    

}
