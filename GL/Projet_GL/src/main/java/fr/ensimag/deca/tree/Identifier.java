package fr.ensimag.deca.tree;

import java.io.PrintStream;
import java.util.Set;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.Definition;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.context.FieldDefinition;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.context.ParamDefinition;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.TypeDefinition;
import fr.ensimag.deca.context.VariableDefinition;
import fr.ensimag.deca.tools.DecacInternalError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.BNE;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
/**
 * Deca Identifier
 *
 * @author gl43
 * @date 01/01/2026
 */
public class Identifier extends AbstractIdentifier {
    
    @Override
    protected void checkDecoration() {
        if (getDefinition() == null) {
            throw new DecacInternalError("Identifier " + this.getName() + " has no attached Definition");
        }
    }

    @Override
    public Definition getDefinition() {
        return definition;
    }

    /**
     * Like {@link #getDefinition()}, but works only if the definition is a
     * ClassDefinition.
     * 
     * This method essentially performs a cast, but throws an explicit exception
     * when the cast fails.
     * 
     * @throws DecacInternalError
     *             if the definition is not a class definition.
     */
    @Override
    public ClassDefinition getClassDefinition() {
        try {
            return (ClassDefinition) definition;
        } catch (ClassCastException e) {
            throw new DecacInternalError(
                    "Identifier "
                            + getName()
                            + " is not a class identifier, you can't call getClassDefinition on it");
        }
    }

    /**
     * Like {@link #getDefinition()}, but works only if the definition is a
     * MethodDefinition.
     * 
     * This method essentially performs a cast, but throws an explicit exception
     * when the cast fails.
     * 
     * @throws DecacInternalError
     *             if the definition is not a method definition.
     */
    @Override
    public MethodDefinition getMethodDefinition() {
        try {
            return (MethodDefinition) definition;
        } catch (ClassCastException e) {
            throw new DecacInternalError(
                    "Identifier "
                            + getName()
                            + " is not a method identifier, you can't call getMethodDefinition on it");
        }
    }

    /**
     * Like {@link #getDefinition()}, but works only if the definition is a
     * FieldDefinition.
     * 
     * This method essentially performs a cast, but throws an explicit exception
     * when the cast fails.
     * 
     * @throws DecacInternalError
     *             if the definition is not a field definition.
     */
    @Override
    public FieldDefinition getFieldDefinition() {
        try {
            return (FieldDefinition) definition;
        } catch (ClassCastException e) {
            throw new DecacInternalError(
                    "Identifier "
                            + getName()
                            + " is not a field identifier, you can't call getFieldDefinition on it");
        }
    }

    /**
     * Like {@link #getDefinition()}, but works only if the definition is a
     * VariableDefinition.
     * 
     * This method essentially performs a cast, but throws an explicit exception
     * when the cast fails.
     * 
     * @throws DecacInternalError
     *             if the definition is not a field definition.
     */
    @Override
    public VariableDefinition getVariableDefinition() {
        try {
            return (VariableDefinition) definition;
        } catch (ClassCastException e) {
            throw new DecacInternalError(
                    "Identifier "
                            + getName()
                            + " is not a variable identifier, you can't call getVariableDefinition on it");
        }
    }

    /**
     * Like {@link #getDefinition()}, but works only if the definition is a ExpDefinition.
     * 
     * This method essentially performs a cast, but throws an explicit exception
     * when the cast fails.
     * 
     * @throws DecacInternalError
     *             if the definition is not a field definition.
     */
    @Override
    public ExpDefinition getExpDefinition() {
        try {
            return (ExpDefinition) definition;
        } catch (ClassCastException e) {
            throw new DecacInternalError(
                    "Identifier "
                            + getName()
                            + " is not a Exp identifier, you can't call getExpDefinition on it");
        }
    }

    @Override
    public void setDefinition(Definition definition) {
        this.definition = definition;
    }

    @Override
    public Symbol getName() {
        return name;
    }

    private Symbol name;

    public Identifier(Symbol name) {
        Validate.notNull(name);
        this.name = name;
    }

    // F j'ai implémenté ça:
    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        //throw new UnsupportedOperationException("not yet implemented");
        Definition ident_def = localEnv.get(name);
        
        if (ident_def == null && currentClass != null) {
            ident_def = currentClass.getMembers().get(name);
        }
        // Règle : utilisation d'un identifiant non déclaré
        if (ident_def == null){
            throw new ContextualError("Identifier " + name.toString() + " is not defined in the current scope nor in its parent scopes", this.getLocation());
        }

        if (ident_def.isMethod()){
            throw new ContextualError("Identifier " + name.toString() + " is used as a method name", 
            this.getLocation());
        }
        
        // Règle : utilisation d'un identifiant qui n'est pas une expression 
        if (! (ident_def instanceof ExpDefinition)){ 
            throw new ContextualError("Identifier " + name.toString() + " is not an expression", 
            this.getLocation());
        }

        else {  
            ExpDefinition ident_def_exp = (ExpDefinition) ident_def; // il faut un downcasting ici car get retouren Definition 
            this.setDefinition(ident_def_exp);

            //ajout
            // if (ident_def_exp instanceof VariableDefinition) {
            //     VariableDefinition varDef = (VariableDefinition) ident_def_exp;
            //     if (!varDef.isInitialized()) {
            //         throw new ContextualError(
            //             "Variable " + name.toString() + " is not initialized",
            //             this.getLocation()
            //         );
            //     }
            // }
            //

            Type ident_type = ident_def_exp.getType(); // on récupère le type de l'expression
            this.setType(ident_type); // !!!!!!!!!!!!décoration de typage

            // if (checkInit && ident_def_exp instanceof VariableDefinition) {
            //     VariableDefinition varDef = (VariableDefinition) ident_def_exp;
            //     if (!varDef.isInitialized()) {
            //         throw new ContextualError("Variable " + name + " is not initialized", getLocation());
            //     }
            // }

            return ident_type;
        }
    }

    /**
     * Implements non-terminal "type" of [SyntaxeContextuelle] in the 3 passes
     * @param compiler contains "env_types" attribute
     */

    // F j'ai implémenté ça
    @Override
    public Type verifyType(DecacCompiler compiler) throws ContextualError {
        //throw new UnsupportedOperationException("not yet implemented");
        // cette méthode est appelée pour vérifier les types dans les déclarations de variables, de méthodes, Classes etc.
        TypeDefinition ident_def = compiler.environmentType.defOfType(name);

        // Règle : utilisation d'un type non défini dans l'environnement
        if (ident_def == null){
            throw new ContextualError("Type " + name.toString() + " is not defined in the environmentType", this.getLocation());
        }

        // Pas besoin car defOfType retourne déjà un TypeDefinition
        /* // Règle : utilisation d'un identifiant qui n'est pas un type 
        if (!(ident_def instanceof TypeDefinition)){
            throw new ContextualError("Identifier " + name.toString() + " n'est pas un type", this.getLocation());
        } */

        else {
            this.setDefinition(ident_def); // on attache la définition du type à l'identifiant

            Type ident_type = ident_def.getType(); // on récupère le type
            this.setType(ident_type); // !!!!!!!!!!!décoration de typage

            return ident_type;
        }
    }
    
    
    private Definition definition;


    @Override
    protected void iterChildren(TreeFunction f) {
        // leaf node => nothing to do
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // leaf node => nothing to do
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print(name.toString());
    }

    @Override
    String prettyPrintNode() {
        return "Identifier (" + getName() + ")";
    }

    @Override
    protected void prettyPrintType(PrintStream s, String prefix) {
        Definition d = getDefinition();
        if (d != null) {
            s.print(prefix);
            s.print("definition: ");
            s.print(d);
            s.println();
        }
    }

/*     @Override
    public DAddr getAdress(DecacCompiler compiler) {
        if (getDefinition() instanceof FieldDefinition){
            GPRegister reg = compiler.getRegManager().allocReg();
            int indexF = this.getFieldDefinition().getIndex();
            compiler.addInstruction(new LOAD(new RegisterOffset(-2,GPRegister.LB ),reg));
            return new RegisterOffset(indexF, reg);
        } else{
            return getExpDefinition().getOperand() ;
        }
    }
 */

    @Override
    public DAddr getAdress(DecacCompiler compiler) {
        return getExpDefinition().getOperand();
    }

    

/*     @Override
    public void codeGenExpr(DecacCompiler compiler, GPRegister targetReg) {
        compiler.addInstruction(
            new LOAD(
                getAdress(compiler),
                targetReg)
        );
    }
 */

    @Override
    public void codeGenExpr(DecacCompiler compiler, GPRegister targetReg) {
        if (getDefinition() instanceof FieldDefinition) {
            int indexF = getFieldDefinition().getIndex();

            GPRegister rThis = compiler.getRegManager().allocRegAnyway(compiler);

            // rThis =  this à -2(LB)
            compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), rThis));

            compiler.addInstruction(new LOAD(new RegisterOffset(indexF, rThis), targetReg));
            compiler.getRegManager().freeRegAnyway(compiler, rThis);
            return;
        }

        // var/param
        compiler.addInstruction(new LOAD(getAdress(compiler), targetReg));
    }



    @Override
    // I've added this Method to respect the book's notations and rules
    public Definition verifyIdent(EnvironmentExp localEnv) throws ContextualError {
        Definition def = localEnv.get(name);
        if (def == null) {
            throw new ContextualError("Identifier " + name + " is not defined", getLocation());
        }
        this.setDefinition(def);
        return def;
    }

/*     @Override
    protected void codeGenPrint(DecacCompiler compiler) {
    
    } */

    @Override
    protected void codeGenBool(DecacCompiler compiler, boolean  b, Label E) {
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

    @Override
    public void collectReadVars(Set<Symbol> reads) {
        Definition def = getDefinition();
        if (def instanceof VariableDefinition || def instanceof ParamDefinition) {
            reads.add(getName());
        }
    }
}
