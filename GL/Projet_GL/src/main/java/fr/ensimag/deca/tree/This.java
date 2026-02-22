package fr.ensimag.deca.tree;

import java.io.PrintStream;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.LOAD;

/**
 *
 * @author gl43
 * @date 25/12/2026
 * I am getting inspiration from ReadInt.java
 */
public class This extends AbstractExpr {

    boolean addedThis; // Je sais pas si c'est nécessaire mais c'est pour marquer que le This 
                       // a été ajouté artificiellement

    public This(boolean addedThis){
        this.addedThis = addedThis;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        //throw new UnsupportedOperationException("not yet implemented");
        //condition:  class̸ = 0 (3.43)
        if (currentClass == null) {
            throw new ContextualError("Token 'this' can only be used in a class", getLocation()
            );
        }
        Type type = currentClass.getType();
        this.setType(type);
        return type;
    }
    
    @Override
    public void decompile(IndentPrintStream s) {
        s.print("this");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        // leaf node => nothing to do
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // leaf node => nothing to do
    }

    @Override
    public void codeGenExpr(DecacCompiler compiler, GPRegister targetReg) {
        compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB),targetReg));
    }
    
}
