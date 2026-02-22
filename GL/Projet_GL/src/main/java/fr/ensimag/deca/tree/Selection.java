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
import fr.ensimag.deca.context.FieldDefinition;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.BNE;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.LOAD;


/**
 * Deca Identifier
 *
 * @author gl43
 * @date 26/12/2026
 * I tried to follow the structure of Identifier.java
 */
public class Selection extends AbstractLValue {
    /*Selection 
        expr ↓env_types ↓env_exp ↓class ↑type_class(class2 )
        field_ident ↓env_exp2 ↑public ↑__ ↑type
        ] */
    final private AbstractExpr objectExpr; // expr
    final private AbstractIdentifier field; // field

    public Selection(AbstractIdentifier field, AbstractExpr objectExpr){
        Validate.notNull(objectExpr);
        Validate.notNull(field);
        this.objectExpr = objectExpr;
        this.field = field;
    }

    // F j'ai implémenté ça:
    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError { 
        // expr ↓env_types ↓env_exp ↓class 
        /*env_types ~ compiler; env_exp ~ localEnv; class ~ currentClass */
        //throw new UnsupportedOperationException("not yet implemented");
        
        Type objectExprType = objectExpr.verifyExpr(compiler, localEnv, currentClass); 

        if (!objectExprType.isClass()){
            // condition (implicit) : //↑type_class(class2 ) the Type of objectExpr must be that of a class
            throw new ContextualError("Can't select a field from " + objectExpr.getType().getName() +", It must be a Class" , getLocation()); //modifié le 3 janv
        }
        // (3.65)
        ClassType objectExprCType = (ClassType) objectExprType;
        ClassDefinition class2 = objectExprCType.getDefinition(); // class2 
        EnvironmentExp envExp2 = class2.getMembers(); // ↓env_exp2 
        Definition def = field.verifyIdent(envExp2); // field_ident ↓env_exp2
        
        if (!def.isField()){
            // condition (implicite) : field_ident must be a field (duuuh)
            throw new ContextualError(field.getName() + " is not a field ", field.getLocation());
        }
        
        FieldDefinition field_ident = (FieldDefinition) def;
        if (field_ident.getVisibility() == Visibility.PROTECTED){
            // (3.66)
            if (currentClass == null){
                // condition : access from main is not allowed
                throw new ContextualError("Access to " + field.getName() +" is not allowed in a main programm as it is protected", getLocation()); // modifié le 3 janv
            }

            if (!objectExprType.isSubTypeOf(currentClass.getType())){
                // condition :  subtype(env_types, type_class(class2), type_class(class))
                throw new ContextualError("Access to protected field not allowed : " + field+ " is being accessed through an object whose type isn't a subType of the current class " + currentClass, field.getLocation());
            }

            if (!currentClass.getType().isSubTypeOf(field_ident.getContainingClass().getType())){
                // subtype(type_class(class), type_class(class_field))
                throw new ContextualError("Access to "+ field + " is not allowed as " + currentClass + "is not a sub-Class of" + field +"'s containing class ", field.getLocation()
            );
            }
        }

        Type type = field_ident.getType(); // ↑type_class(class2 )
        setType(type);
        return type;
    }


/*     @Override
    public void codeGenExpr(DecacCompiler compiler, GPRegister targetReg) {
        DAddr addrPointeurObj = null;
        if (objectExpr instanceof Identifier){
            Identifier expr = (Identifier) objectExpr;
            addrPointeurObj = expr.getExpDefinition().getOperand();
        } else if (objectExpr instanceof Selection) {
            compiler.addComment("Selection");
            Selection expr = (Selection) objectExpr;
            addrPointeurObj = expr.getAdress(compiler);
        } else if (objectExpr instanceof MethodCall) {
            compiler.addComment("MethodCall");
            MethodCall mthd = (MethodCall) objectExpr;
            mthd.codeGenExpr(compiler, targetReg);
            int indexF = field.getFieldDefinition().getIndex();
            GPRegister R0 = GPRegister.R0;
            compiler.addInstruction(new LOAD(new RegisterOffset(indexF, R0),targetReg));
            return;
        } else{
            compiler.addComment("This");
            //GPRegister rReg = compiler.getRegManager().allocReg();
            GPRegister rReg = compiler.getRegManager().allocRegAnyway(compiler) ;
            objectExpr.codeGenExpr(compiler, rReg);
            int indexF = field.getFieldDefinition().getIndex();
            compiler.addInstruction(new LOAD(new RegisterOffset(indexF, rReg),targetReg));
            //compiler.getRegManager().freeReg(rReg);
            compiler.getRegManager().freeRegAnyway(compiler, rReg);
            return; // C'est le cas d'un THIS
        }
            int indexF = field.getFieldDefinition().getIndex();
            //GPRegister rReg = compiler.getRegManager().allocReg(); 
            GPRegister rReg = compiler.getRegManager().allocRegAnyway(compiler) ;
            compiler.addComment("Selection");
            
            compiler.addInstruction(new LOAD(addrPointeurObj, rReg));
            compiler.addInstruction(new LOAD(new RegisterOffset(indexF, rReg),targetReg));
            //compiler.getRegManager().freeReg(rReg);
            compiler.getRegManager().freeRegAnyway(compiler, rReg);
    } */

    @Override
    public void codeGenExpr(DecacCompiler compiler, GPRegister targetReg) {
        int indexF = field.getFieldDefinition().getIndex();
    
        // evaluer l'objet directement dans targetReg
        objectExpr.codeGenExpr(compiler, targetReg);
    
        if (!compiler.isNoCheck()) {
            compiler.addInstruction(new CMP(new NullOperand(), targetReg));
            Label err = ErrorManager.useAndGetLabel(compiler, ErrorManager.ErrorType.DEREFERENCEMENT_NULL);
            compiler.addInstruction(new BEQ(err));
        }
    
        // targetReg = *(targetReg + indexF)
        compiler.addInstruction(new LOAD(new RegisterOffset(indexF, targetReg), targetReg));
    }
    
    

    @Override
    public void decompile(IndentPrintStream s) {
        objectExpr.decompile(s); //a
        s.print("."); // a.
        field.decompile(s); // a.field
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        objectExpr.iter(f);
        field.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        objectExpr.prettyPrint(s, prefix, false);
        field.prettyPrint(s, prefix, true);
}


/*     @Override
    public DAddr getAdress(DecacCompiler compiler){
        // Il me faut le Decac Compiler car je n'ai que le pointeur
        // Il me faut l'adresse contenue dans le pointeur
        
        int indexF = field.getFieldDefinition().getIndex();
        //GPRegister rReg = compiler.getRegManager().allocReg(); 
        GPRegister rReg = compiler.getRegManager().allocRegAnyway(compiler) ;
        objectExpr.codeGenExpr(compiler, rReg);

        compiler.addInstruction(new CMP(new NullOperand(),rReg));

        
        if (!compiler.isNoCheck()) {
            Label dereferencement_null = ErrorManager.getErrorLabel(ErrorManager.ErrorType.DEREFERENCEMENT_NULL);
            compiler.useError(ErrorManager.ErrorType.DEREFERENCEMENT_NULL);
            compiler.addInstruction(new BEQ(dereferencement_null));
        }

        compiler.getRegManager().freeReg(rReg);
        return new RegisterOffset(indexF, rReg);
    } */

    @Override
    public DAddr getAdress(DecacCompiler compiler) {
        // Une sélection n'a pas d'adresse "stable" sans conserver un registre vivant.
        // On force l'utilisation de codeGenExpr / codeGenStore dans Assign.
        return null;
    }


    @Override
    protected void codeGenBool(DecacCompiler compiler, boolean b, Label E) {
        GPRegister r = compiler.getRegManager().allocRegAnyway(compiler);

        // évaluation de l'expression du champs
        this.codeGenExpr(compiler, r) ;

        compiler.addInstruction(new CMP(0, r)) ;
        if (b) {
            compiler.addInstruction(new BNE(E)) ;
        }
        else {
            compiler.addInstruction(new BEQ(E)) ;
        }
        compiler.getRegManager().freeRegAnyway(compiler, r);
    }

    //ajot le 18 janv
    @Override
    protected void collectReadVars(Set<Symbol> reads) {
        objectExpr.collectReadVars(reads);
    }


    public AbstractExpr getObjectExpr() { return objectExpr; }
    public AbstractIdentifier getField() { return field; }
}
