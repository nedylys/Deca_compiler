package fr.ensimag.deca.tree;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.DecacInternalError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.BNE;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.WFLOAT;
import fr.ensimag.ima.pseudocode.instructions.WFLOATX;
import fr.ensimag.ima.pseudocode.instructions.WINT;


/**
 * Expression, i.e. anything that has a value.
 *
 * @author gl43
 * @date 01/01/2026
 */
public abstract class AbstractExpr extends AbstractInst {
    /**
     * @return true if the expression does not correspond to any concrete token
     * in the source code (and should be decompiled to the empty string).
     */
    boolean isImplicit() {
        return false;
    }

    /**
     * Get the type decoration associated to this expression (i.e. the type computed by contextual verification).
     */
    public Type getType() {
        return type;
    }

    protected void setType(Type type) {
        Validate.notNull(type);
        this.type = type;
    }
    private Type type;

    @Override
    protected void checkDecoration() {
        if (getType() == null) {
            throw new DecacInternalError("Expression " + decompile() + " has no Type decoration");
        }
    }

    /**
     * Verify the expression for contextual error.
     * 
     * implements non-terminals "expr" and "lvalue" 
     *    of [SyntaxeContextuelle] in pass 3
     *
     * @param compiler  (contains the "env_types" attribute)
     * @param localEnv
     *            Environment in which the expression should be checked
     *            (corresponds to the "env_exp" attribute)
     * @param currentClass
     *            Definition of the class containing the expression
     *            (corresponds to the "class" attribute)
     *             is null in the main bloc.
     * @return the Type of the expression
     *            (corresponds to the "type" attribute)
     */
    public abstract Type verifyExpr(DecacCompiler compiler,
            EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError;

    /**
     * Verify the expression in right hand-side of (implicit) assignments 
     * 
     * implements non-terminal "rvalue" of [SyntaxeContextuelle] in pass 3
     *
     * @param compiler  contains the "env_types" attribute
     * @param localEnv corresponds to the "env_exp" attribute
     * @param currentClass corresponds to the "class" attribute
     * @param expectedType corresponds to the "type1" attribute            
     * @return this with an additional ConvFloat if needed...
     */
    public AbstractExpr verifyRValue(DecacCompiler compiler,
            EnvironmentExp localEnv, ClassDefinition currentClass, 
            Type expectedType)
            throws ContextualError {
        //throw new UnsupportedOperationException("not yet implemented");
        // rvalue ↓env_types ↓env_exp ↓class ↓type1
        Type rvalueType = this.verifyExpr(compiler, localEnv, currentClass);



        //this.checkInitializedIdentifiers();//ajout
        //ajout de vérification de l'initiamisation de right operand
        // if (this instanceof Identifier) {
        //     Identifier id = (Identifier) this;
        //     Definition def = id.getDefinition();

        //     if ((def instanceof VariableDefinition) && !(def instanceof ParamDefinition)) {

        //         VariableDefinition varDef = (VariableDefinition) def;
        //         if (!varDef.isInitialized()) {
        //             throw new ContextualError(
        //                 "Variable " + id.getName() + " is not initialized",
        //                 id.getLocation()
        //             );
        //         }
        //     }
        // }
        //
        
        // condition assign_compatible(env_types, type1, type2)
        if(!expectedType.assignCompatible(rvalueType)){
            throw new ContextualError("Incompatible assignment: expected  " + expectedType+ ", got " + rvalueType, this.getLocation());
        }

        if (rvalueType.isInt() && expectedType.isFloat() ) {
            ConvFloat convrvalue = new ConvFloat(this);
            convrvalue.verifyExpr(compiler, localEnv, currentClass);
            convrvalue.setLocation(this.getLocation());
            return convrvalue;
        }

        return this; //↑type2

    }

    // //ajout
    // protected void checkInitializedIdentifiers() throws ContextualError {
    // this.iter(new TreeFunction() {
    //     @Override
    //     public void apply(Tree t) {
    //                 if (t instanceof Identifier) {
    //                     Identifier id = (Identifier) t;
    //                     Definition d = id.getDefinition();
    //                     if (d instanceof VariableDefinition) {
    //                         VariableDefinition v = (VariableDefinition) d;
    //                         if (!v.isInitialized()) {
    //                             throw new ContextualError("Variable " + id.getName() + " is not initialized",id.getLocation());
    //                         }
    //                     }
    //                 }
    //             }
    //         });
    //     }
    //     //
    
    
    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType)
            throws ContextualError {
        //throw new UnsupportedOperationException("not yet implemented");
        Type t = this.verifyExpr(compiler, localEnv, currentClass);

        /*if (t.isVoid()) {
            throw new ContextualError("An expression of type void cannot be used as an instruction", this.getLocation()
            );
        } */
    }

    /**
     * Verify the expression as a condition, i.e. check that the type is
     * boolean.
     *
     * @param localEnv
     *            Environment in which the condition should be checked.
     * @param currentClass
     *            Definition of the class containing the expression, or null in
     *            the main program.
     */
    void verifyCondition(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        //throw new UnsupportedOperationException("not yet implemented");
        Type t = this.verifyExpr(compiler, localEnv, currentClass);

        if (!t.isBoolean()) {
            throw new ContextualError("Condition must be of type boolean", this.getLocation()
            );
        }
    }

    /**
     * Generate code to print the expression
     *
     * @param compiler
     */
    protected void codeGenPrint(DecacCompiler compiler, boolean printHex) {
        // fait par MEA
        // R1 <- évaluation de l'expression
        this.codeGenExpr(compiler, GPRegister.R1);

        if (getType().isInt()) {
            compiler.addInstruction(new WINT());
        }
        else if (getType().isFloat()) {
            if (printHex) {
                compiler.addInstruction(new WFLOATX());
            }
            else  {
                compiler.addInstruction(new WFLOAT());
            }
            
        }
        else {
            throw new UnsupportedOperationException("codeGenPrint : not supprted for type " + getType()) ;
        }
    }

    protected void codeGenPrint(DecacCompiler compiler) {
        codeGenPrint(compiler, false);
    }

    @Override
    /* MEA :  */
    protected void codeGenInst(DecacCompiler compiler) {
        //throw new UnsupportedOperationException("not yet implemented");
        GPRegister targetReg = compiler.getRegManager().allocReg();
        this.codeGenExpr(compiler, targetReg);
        compiler.getRegManager().freeReg(targetReg);
    }
    

    @Override
    protected void decompileInst(IndentPrintStream s) {
        decompile(s);
        s.print(";");
    }

    @Override
    protected void prettyPrintType(PrintStream s, String prefix) {
        Type t = getType();
        if (t != null) {
            s.print(prefix);
            s.print("type: ");
            s.print(t);
            s.println();
        }
    }

    /** Generate code for an expression
     * @param compiler
     * @param targetReg 
     */
    public abstract void codeGenExpr(DecacCompiler compiler, GPRegister targetReg) ;


    /** Generate code for a boolean expression 
     * @param compiler
     * @param b : boolean of branch 
     * @param E : (E for Etiquette) : label to branch into 
     * 
     * b = true -> branch to E if the expression is true
     * b = false -> branch to E if the expression if false
    */
    protected void codeGenBool(DecacCompiler compiler, boolean b, Label E) {
        // correction suite à la soutenance
        GPRegister r = compiler.getRegManager().allocRegAnyway(compiler) ;
        this.codeGenExpr(compiler, r);
        compiler.addInstruction(new CMP(0, r));
        if (b) {
            compiler.addInstruction(new BNE(E));
        }
        else {
            compiler.addInstruction(new BEQ(E));
        }
        compiler.getRegManager().freeRegAnyway(compiler, r);
    }

    /** Helper pour factoriser le code de génération de code
     * pour les expressions booléennes
     */
    protected void codeGenExprBool(DecacCompiler compiler, GPRegister r) {
        int numLabel = compiler.getLabelCounter() ;
        Label etiqTrue = new Label("Etiq_True." + numLabel) ;
        Label etiqFin = new Label("Etiq_Fin." + numLabel) ;
        
        this.codeGenBool(compiler, true, etiqTrue);
        compiler.addInstruction(new LOAD(0, r));
        compiler.addInstruction(new BRA(etiqFin));

        compiler.addLabel(etiqTrue);
        compiler.addInstruction(new LOAD(1, r)) ;

        compiler.addLabel(etiqFin);
    }

    // extension
    public AbstractExpr foldExpr(DecacCompiler compiler) {
        return this; // par défaut: rien à faire
    }
}

