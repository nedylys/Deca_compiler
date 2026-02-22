package fr.ensimag.deca.tree;

import java.io.PrintStream;
import java.util.Set;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.ErrorManager;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ClassType;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.Definition;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.VoidType;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol ;
import fr.ensimag.ima.pseudocode.GPRegister ;
import fr.ensimag.ima.pseudocode.ImmediateInteger ;
import fr.ensimag.ima.pseudocode.Label ;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset ;
import fr.ensimag.ima.pseudocode.instructions.ADDSP;
import fr.ensimag.ima.pseudocode.instructions.BEQ ;
import fr.ensimag.ima.pseudocode.instructions.BNE;
import fr.ensimag.ima.pseudocode.instructions.BSR ;
import fr.ensimag.ima.pseudocode.instructions.CMP ;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import fr.ensimag.ima.pseudocode.instructions.SUBSP ;


/**
 *
 * @author gl43
 * @date 25/12/2026
 * I am getting inspiration from ReadInt.java
 */
public class MethodCall extends AbstractExpr {

    final private AbstractExpr objectExpr;
    final private AbstractIdentifier methodIdent;
    final private ListExpr rvalueStar;

    public MethodCall(AbstractExpr objectExpr,
                      AbstractIdentifier methodIdent,
                      ListExpr rvalueStar) {
        Validate.notNull(objectExpr);
        Validate.notNull(methodIdent);
        Validate.notNull(rvalueStar);
        this.objectExpr = objectExpr;
        this.methodIdent = methodIdent;
        this.rvalueStar = rvalueStar;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        //throw new UnsupportedOperationException("not yet implemented");
        //method_call ↓env_types ↓env_exp ↓class ↑type
   
        
        Type objectExprType = objectExpr.verifyExpr(compiler, localEnv, currentClass);
        // condition type = type_class(__) (3.42)
        if (!objectExprType.isClass()) {
            throw new ContextualError("Can't Call a method on a non class object", objectExpr.getLocation());
        }

        ClassType class2Type = (ClassType) objectExprType;  // ↑type_class(class2 )
        ClassDefinition class2Def = class2Type.getDefinition();   //(class(__, env_exp2), __) ≜ env_types(class2)
        EnvironmentExp class2Env = class2Def.getMembers(); // env_exp2
        // method_ident ↓env_exp2 ↑(method(sig), type)
        Definition def = methodIdent.verifyIdent(class2Env); //↓env_exp2

        // condition: def must be that of a method
        if (!(def.isMethod())){ // Things are getting a little bit too complicated: i could do def.isField() as everything
                                // till here comes from a getMembers(), but i don't want to take the risk in this code
            throw new ContextualError(methodIdent.getName() + "is not a method", methodIdent.getLocation());
        }
        MethodDefinition methodDef = (MethodDefinition) def; // We downcast to be able to use MethodDefinition methods (such as getSignature)

        // rvalueStar ↓env_types ↓env_exp ↓class ↓sig
        rvalueStar.verifyRvalueStar(compiler, localEnv, methodDef.getSignature(),currentClass
        );

        //type := type
        Type type = methodDef.getType();
        this.setType(type);
        return type;
        }

    
    public AbstractIdentifier getMethodIdent(){
        return methodIdent;
    } 

    public AbstractExpr getObjectExpr(){
        return objectExpr;
    }

    public ListExpr getRvalueStar(){
        return rvalueStar;
    }
    
    @Override
    public void decompile(IndentPrintStream s) {
        s.print("this");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        objectExpr.iter(f);
        methodIdent.iter(f);
        rvalueStar.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        objectExpr.prettyPrint(s, prefix, false);
        methodIdent.prettyPrint(s, prefix, false);
        rvalueStar.prettyPrint(s, prefix, true);
    }

     @Override
    // voir page 223
    public void codeGenExpr(DecacCompiler compiler, GPRegister targetReg) {
        int nbExplicitArgs = rvalueStar.size() ;
        int totalNbParams = nbExplicitArgs + 1 ;        // + 1 pour le paramètre implicite

        // réservation de la place dans la pile pour les paramètres
        compiler.getStackManager().addToCurrNbTemp(totalNbParams); // Afin d'avoir 
                                                                // le nbr de places
                                                                // réservées dans la pile
        compiler.addInstruction(new ADDSP(totalNbParams));

        GPRegister r = compiler.getRegManager().allocRegAnyway(compiler) ;

        // empilement du paramètre implicite à 0(SP) via r
        objectExpr.codeGenExpr(compiler, r);
        compiler.addInstruction(new STORE(r, new RegisterOffset(0, Register.SP))) ;


        // empilement des paramètres explicites
        AbstractExpr implicitParam ;
        for (int i = 0 ; i < nbExplicitArgs ; i++) {
            implicitParam = rvalueStar.getList().get(i) ;
            implicitParam.codeGenExpr(compiler, r);
            compiler.addInstruction(new STORE(r, new RegisterOffset(-(i + 1), Register.SP)));
        }

        // vérifier que le paramètre implicite est diff de null
        compiler.addInstruction(new LOAD(new RegisterOffset(0, Register.SP), r));
        if (!compiler.isNoCheck()) {
            compiler.addInstruction(new CMP(new NullOperand(), r)) ;
            Label err = ErrorManager.useAndGetLabel(compiler, ErrorManager.ErrorType.DEREFERENCEMENT_NULL) ;
            compiler.addInstruction(new BEQ(err)); 
        }

        // récupérer la vtable 
        compiler.addInstruction(new LOAD(new RegisterOffset(0, r), r));

        // appel de la méthode selon son index 
        int idx = methodIdent.getMethodDefinition().getIndex() ; // à vérifier
        //compiler.addInstruction(new LOAD(new RegisterOffset(idx, r), r)); 
        compiler.addInstruction(new BSR(new RegisterOffset(idx, r)));

        // désempilement des paramètres
        compiler.addInstruction(new SUBSP(totalNbParams));

        // résultat dans R0
        if (targetReg.getNumber() != 0 && !(methodIdent.getMethodDefinition().getType() instanceof VoidType)) {
            compiler.addInstruction(new LOAD (GPRegister.R0, targetReg));
        }

        compiler.getRegManager().freeRegAnyway(compiler, r);
        compiler.getStackManager().addToCurrNbTemp(-totalNbParams);// On reset après le retour du
                                                                   // MethodCall
    }
                                                          
    @Override
    protected void codeGenBool(DecacCompiler compiler, boolean b, Label E) {
        GPRegister r0 = compiler.getRegManager().allocRegAnyway(compiler) ;
        this.codeGenExpr(compiler, r0) ;
        compiler.addInstruction(new CMP(new ImmediateInteger(0), r0));
        if (b) {
            compiler.addInstruction(new BNE(E));
        }
        else {
            compiler.addInstruction(new BEQ(E));
        }
        compiler.getRegManager().freeRegAnyway(compiler, r0);
    }

    @Override
    public void collectReadVars(Set<Symbol> reads) {
        if (objectExpr != null) { // ex: obj.m(...)
            objectExpr.collectReadVars(reads);
        }
        for (AbstractExpr a : rvalueStar.getList()) {
            a.collectReadVars(reads);
        }
    }
}
