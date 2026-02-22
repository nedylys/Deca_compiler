package fr.ensimag.deca.tree;

import java.io.PrintStream;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.ErrorManager;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.RFLOAT;

/**
 *
 * @author gl43
 * @date 01/01/2026
 */
public class ReadFloat extends AbstractReadExpr {

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        //throw new UnsupportedOperationException("not yet implemented");
        Type type = compiler.environmentType.FLOAT;
        this.setType(type); // Décoration de Typage
        return type;
    }



    @Override
    public void decompile(IndentPrintStream s) {
        s.print("readFloat()");
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
    /* AT - MEA : implémentée le 03/01 */
    public void codeGenExpr(DecacCompiler compiler, GPRegister targetReg) {
        compiler.useError(ErrorManager.ErrorType.IO_ERROR);
        // RFLOAT lit un flottant et le met dans R1
        compiler.addInstruction(new RFLOAT());
        compiler.addInstruction(new BOV(ErrorManager.useAndGetLabel(compiler, ErrorManager.ErrorType.IO_ERROR)));
        // Si targetReg n'est pas R1, on copie dans targetReg
        if (targetReg != Register.R1) {
            compiler.addInstruction(new LOAD(Register.R1, targetReg));
        }
    }


}
