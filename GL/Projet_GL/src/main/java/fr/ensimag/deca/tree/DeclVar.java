package fr.ensimag.deca.tree;

import java.io.PrintStream;
import java.util.Set;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.VariableDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.STORE;

/**
 * @author gl43
 * @date 01/01/2026
 */
public class DeclVar extends AbstractDeclVar {

    
    final private AbstractIdentifier type;
    final private AbstractIdentifier varName;
    final private AbstractInitialization initialization;

    public DeclVar(AbstractIdentifier type, AbstractIdentifier varName, AbstractInitialization initialization) {
        Validate.notNull(type);
        Validate.notNull(varName);
        Validate.notNull(initialization);
        this.type = type;
        this.varName = varName;
        this.initialization = initialization;
    }

    @Override
    protected void verifyDeclVar(DecacCompiler compiler,
            EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {

            Type ttype = type.verifyType(compiler);
            if (ttype.isVoid()) {
                throw new ContextualError(
                    "Variable cannot be declared with type void",
                    type.getLocation()
                );
            }
            /*if(localEnv.get(symbol) != null){
                throw new ContextualError(
                    "Variable already declared: " + symbol.getName(),
                    varName.getLocation()
                );
            } */

            VariableDefinition def = new VariableDefinition(ttype, varName.getLocation());
            Symbol symbol = varName.getName();
            try {
                //System.out.println(localEnv.get(symbol));
                //System.out.println(localEnv.get(symbol));
                localEnv.declare(symbol, def);
            } catch (EnvironmentExp.DoubleDefException e) {
                throw new ContextualError(
                    "Variable already declared: " + symbol.getName(),
                    varName.getLocation()
                );
            }

            varName.setDefinition(def);
            varName.setType(ttype);

            //def.setInitialized(false); // ajout
            initialization.verifyInitialization(compiler,ttype,localEnv,currentClass);

            // if(initialization.isInitialization()){
            //     def.setInitialized(true);
            // }


    }

    
    @Override
    public void decompile(IndentPrintStream s) {
        //throw new UnsupportedOperationException("not yet implemented");
        type.decompile(s);
        s.print(" ");
        varName.decompile(s);
        initialization.decompile(s);
        s.print(";");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        type.iter(f);
        varName.iter(f);
        initialization.iter(f);
    }
    
    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        varName.prettyPrint(s, prefix, false);
        initialization.prettyPrint(s, prefix, true);
    }

    @Override
    protected void codeGenDeclVar(DecacCompiler compiler) {
        VariableDefinition varDef = varName.getVariableDefinition() ;
        int k = compiler.allocGB() ;
        DAddr addr = new RegisterOffset(k, Register.GB) ;
        varDef.setOperand(addr);
        initialization.codeGenInitialization(compiler, addr);
    }

    @Override
    public void codeGenDeclVarClass(DecacCompiler compiler){
        if (initialization instanceof NoInitialization){
            VariableDefinition varDef = varName.getVariableDefinition() ;
            int k = compiler.allocGB() ;
            DAddr addr = new RegisterOffset(k, Register.GB) ;
            varDef.setOperand(addr);
            GPRegister rReg = compiler.getRegManager().allocReg();
            compiler.addInstruction(new LOAD(new NullOperand(),rReg));
            compiler.addInstruction(new STORE(rReg,addr));
            compiler.getRegManager().freeReg(rReg);
            return;
        }
        Initialization init = (Initialization) initialization;
        if (init.getExpression() instanceof New){
            GPRegister R2 = GPRegister.getR(2);
            compiler.addComment("new ligne " + Integer.toString(type.getClassDefinition().getLocation().getLine()));
            VariableDefinition varDef = varName.getVariableDefinition() ;
            int k = compiler.allocGB() ;
            DAddr addr = new RegisterOffset(k, Register.GB) ;
            varDef.setOperand(addr);
            init.codeGenInitializationTargetR(compiler, addr, R2 );
        } else {
            codeGenDeclVar(compiler);
        }
    }
   
    @Override
    public void codeGenDeclVarMthd(DecacCompiler compiler,int index){
        VariableDefinition varDef = varName.getVariableDefinition() ;
        DAddr addr = new RegisterOffset(index, Register.LB) ;
        varDef.setOperand(addr);
        initialization.codeGenInitialization(compiler, addr);
    }

    public AbstractIdentifier getType(){
        return type;
    }

    //extension
    @Override
    public boolean foldConstants(DecacCompiler compiler){
        boolean changed = false;
        changed |= initialization.foldConstants(compiler);
        return changed;
    }

    @Override
    public void collectReadVars(Set<Symbol> reads) {
        // le nom déclaré n'est pas une lecture

        AbstractInitialization init = initialization;
        if (init != null && !(init instanceof NoInitialization)) {
            
            AbstractExpr expr = ((Initialization) init).getExpression();
            if (expr != null) {
                expr.collectReadVars(reads);
            }
        }
    }

    public Symbol getName(){
        return varName.getName();
    }

    public AbstractInitialization getInitialization(){
        return initialization;
    }
}
