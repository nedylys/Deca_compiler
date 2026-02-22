package fr.ensimag.deca.tree;

import java.io.PrintStream;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.BooleanType;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.FloatType;
import fr.ensimag.deca.context.IntType;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import fr.ensimag.ima.pseudocode.ImmediateFloat;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
/**
 * Absence of initialization (e.g. "int x;" as opposed to "int x =
 * 42;").
 *
 * @author gl43
 * @date 01/01/2026
 */
public class NoInitialization extends AbstractInitialization {

    @Override
    protected void verifyInitialization(DecacCompiler compiler, Type t,
            EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        // throw new UnsupportedOperationException("not yet implemented");
        // Rien à vérifier dans le cas de l'absence d'initialisation
    }


    /**
     * Node contains no real information, nothing to check.
     */
    @Override
    protected void checkLocation() {
        // nothing
    }

    @Override
    public void decompile(IndentPrintStream s) {
        // nothing
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
    protected void codeGenInitialization(DecacCompiler compiler, DAddr addr) {
        // nothing : variable not initialized
    }

    @Override
    public boolean isInitialization() {
        return false;
    }

    @Override
    public void codeGenInitializationField(DecacCompiler compiler,DAddr addr,Type type){
        GPRegister R1 = GPRegister.getR(1);
        if (type instanceof IntType || 
            type instanceof BooleanType ){
            compiler.addInstruction(new LOAD(new ImmediateInteger(0), R1));
        } else if (type instanceof FloatType){
            compiler.addInstruction(new LOAD(new ImmediateFloat(0), R1));
        }
        else{
            compiler.addInstruction(new LOAD(new NullOperand(), R1));
        }
        compiler.addInstruction(new STORE(R1, addr));
    }
}
