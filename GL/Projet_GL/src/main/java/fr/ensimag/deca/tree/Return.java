package fr.ensimag.deca.tree;

import java.io.PrintStream;
import java.util.Set;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.BRA;


/**
 *
 * @author gl43
 * @date 25/12/2025 
 * Je m'inspire de While.java
 */
public class Return extends AbstractInst {
    private AbstractExpr rvalue;

    public AbstractExpr getRvalue() {
        return rvalue;
    }

    public Return(AbstractExpr rvalue) {
        Validate.notNull(rvalue);
        this.rvalue = rvalue;
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        rvalue.codeGenExpr(compiler,GPRegister.getR(0));
        Label fin = compiler.getCurrentMethodEndLabel();
        if (fin != null) {
            compiler.addInstruction(new BRA(fin)); 
        }
    }

    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType)
            throws ContextualError {
            //Return [ rvalue ↓env_types ↓env_exp ↓class ↓return ]
            /*compiler ~ env_types, localEnv ~ env_exp, currentClass ~ class, returnType ~ return =rvalue */
            
            //condition (implicit) : return is not allowed in main
            if (currentClass == null){
                throw new ContextualError("Return Statement is not allowed in main", getLocation());
            } 
            //First Condition : return̸ = void (This will also make sure we never do return something_not_void; in main as
            //Main bloc↓void so returnType will be void, However it is not enough as return only makes since if inherited from
            //a method : MethodBody bloc ↓env_types ↓env_exp ↓env_exp_params ↓class ↓return)
            if (returnType.isVoid()){
                throw new ContextualError("Return value can't be of type void", getLocation());
            }

            //Second Condition: assign_compatible(env_types, type1, type2) : 3.24
            Type rvalueType = rvalue.verifyExpr(compiler, localEnv, currentClass); // verify and get Type

            if(!returnType.assignCompatible(rvalueType)){
                throw new ContextualError("Incompatible return type : expected " + returnType + ", got " + rvalueType, rvalue.getLocation());
            }


    }   

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("return ");
        getRvalue().decompile(s);
        s.print(";");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        rvalue.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        rvalue.prettyPrint(s, prefix, false);
    }

    //extension
    @Override
    public boolean foldConstants(DecacCompiler compiler){
        //boolean changed = false;

        AbstractExpr n_rvalue = rvalue.foldExpr(compiler);

        if(n_rvalue != rvalue){
            rvalue = n_rvalue;
            return true;
        }

        return false;
    }

    @Override
    public void collectReadVars(Set<Symbol> reads) {
        if (rvalue != null) {
            rvalue.collectReadVars(reads);
        }
    }



}
