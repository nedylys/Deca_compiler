package fr.ensimag.deca.tree;

import java.io.PrintStream;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
public class Null extends AbstractExpr {
    /*I named it NullType and not Null as Null is an existing token */
    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
        //return compiler.environmentType.NULL;
        Type t = compiler.environmentType.NULL;
        this.setType(t);
        return t;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("null");
    }

    @Override
    public void codeGenExpr(DecacCompiler compiler, GPRegister register) {
        compiler.addInstruction(new LOAD(new NullOperand(),register));
    }
    
    @Override
    public void iterChildren(TreeFunction f){
        // nothing to do as it is  leaf
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // nothing to do as it is  leaf
    }
}
