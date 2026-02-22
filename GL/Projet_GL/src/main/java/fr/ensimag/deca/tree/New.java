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
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.BSR;
import fr.ensimag.ima.pseudocode.instructions.LEA;
import fr.ensimag.ima.pseudocode.instructions.NEW;
import fr.ensimag.ima.pseudocode.instructions.STORE;
/**
 *
 * @author gl43
 * @date 25/12/2026
 * I am getting inspiration from ReadInt.java
 */
public class New extends AbstractExpr {

    final private AbstractIdentifier type;

    public New(AbstractIdentifier type) {
        Validate.notNull(type);
        this.type = type;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        //throw new UnsupportedOperationException("not yet implemented");
        //New [ type ↓env_types ↑type ]
   
        
        Type ttype = type.verifyType(compiler); // type ↓env_types ↑type
        // condition type = type_class(__) (3.42)
        if (!ttype.isClass()) {
            throw new ContextualError("new can only be applied to a class", type.getLocation());
        }

        //type := type
        this.setType(ttype);
        return ttype;
        }


    public AbstractIdentifier getTypeIdent() {
        return type;
    }
    
    @Override
    public void decompile(IndentPrintStream s) {
        s.print("new ");
        type.decompile(s);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        type.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, true);
    }

    @Override
    public void codeGenExpr(DecacCompiler compiler, GPRegister targetReg) {
        int numerFields = type.getClassDefinition().getNumberOfFields();
        GPRegister R0 = GPRegister.getR(0);


        
        compiler.addInstruction(new NEW(numerFields + 1,targetReg));
  
        if (!compiler.isNoCheck()) {
            Label tasPlein = ErrorManager.getErrorLabel(ErrorManager.ErrorType.TAS_PLEIN); 
            compiler.useError(ErrorManager.ErrorType.TAS_PLEIN);
            compiler.addInstruction(new BOV(tasPlein));
        }
        //int kClassGB = type.getClassDefinition().getdGB(); // Le k ou se trouve le 
                                                                // pointeur de la table
                                                                // des classes de méthodes 

        DAddr mtAdrr = type.getClassDefinition().getMethodTabAddr() ;
        compiler.addInstruction(new LEA(mtAdrr, R0));
        //compiler.addInstruction(new LEA(new RegisterOffset(kClassGB, Register.GB),R0));
        
        
        compiler.addInstruction(new STORE(R0,new RegisterOffset(0, targetReg)));// 0(targetReg) correspond au pointeur vers la table des méthodes.
        
        compiler.getStackManager().pushRegTemp(compiler, targetReg);
        String nomC = "init." + this.type.getName().getName();
        compiler.addInstruction(new BSR(new Label(nomC)));
        
        compiler.getStackManager().popRegTemp(compiler, targetReg);// On recupere l'objet Initialisé 
                                                     // dans targetR
    }

}
