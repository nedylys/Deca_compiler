package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tree.Location;
import fr.ensimag.ima.pseudocode.Label;

import fr.ensimag.deca.context.Definition;
import java.util.Set;
import java.util.HashSet;

/*les imports ajoutés*/
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.context.MethodDefinition;

import java.io.PrintStream;
import fr.ensimag.deca.context.Signature;

import org.apache.commons.lang.Validate;

/**Again i am trying to follow the strcture fo DeclVar.java */
/**
 * @author gl43
 * @date 01/01/2026
 */

public class DeclMethod extends AbstractDeclMethod {

    //this.visibility = visibility; Unlike Java Deca doesn't support Method vsiibility
    /* final private Visibility visibility;*/
    final private AbstractIdentifier type;
    final private AbstractIdentifier methodName;
    final private ListDeclParam params;
    final private AbstractMethodBody methodBody;
    /* final private AbstractInitialization initialization; */

    public DeclMethod(AbstractIdentifier type, AbstractIdentifier methodName, 
        ListDeclParam params, AbstractMethodBody methodBody){
        // Unlike DeclVar there is no sign of localENv in here as we don't dive into the body of methods nor the programm in pass 2
        Validate.notNull(type);
        Validate.notNull(methodName);
        Validate.notNull(params);
        Validate.notNull(methodBody);

        this.type = type;
        this.methodName = methodName;
        this.params = params;
        this.methodBody = methodBody;
    }

    @Override
    protected void verifyDeclMethod(DecacCompiler compiler, ClassDefinition currentClass) throws ContextualError {
            Type ttype = type.verifyType(compiler);
            EnvironmentExp env = new EnvironmentExp(null);
            Signature signature = params.verifyListDeclParam(compiler,env);

            // A method can be of type void
            /* if (ttype.isVoid()) {
                throw new ContextualError(
                    "Method cannot be declared with type void",
                    type.getLocation()
                );
            } */

            // First condition : field name must not hide a method name of the superClass
            //(class(__, env_exp_super), __) ≜ env_types(super)
            // + env_exp_super(name) est défini ⇒ env_exp_super(name) = method

             //(class(__, env_exp_super), __) ≜ env_types(super)
            int idx ;
            ClassDefinition superClass = currentClass.getSuperClass();
            if (superClass != null){
                ExpDefinition mfWithName = superClass.getMembers().get(methodName.getName());
            // + env_exp_super(name) est défini ⇒ env_exp_super(name) = method
                if (mfWithName != null ){
                    if (mfWithName.isField()){ // I decided not to go with !methodWithname.isMethod() as only Field and Methods are allowed in here 
                    throw new ContextualError("Name already used for a field in super class: you can not hide a field with a method", methodName.getLocation());
                }

                MethodDefinition methodToOverride = mfWithName.asMethodDefinition("I the developer say this message will never pop out as i made sure this is a  method (I could be wrong ... I hope not", getLocation());

                if (!signature.equals(methodToOverride.getSignature())){
                    // Violated sig = sig2
                    throw new ContextualError("Overrided method's signature does not match the one in the super class", methodName.getLocation()
                    );
                }
                
                if (!ttype.isSubTypeOf(methodToOverride.getType())){
                    //  Violated subtype(env_types, type, type2)
                    throw new ContextualError("Overrided method type must be a subtype of the super class method's type", getLocation());
                }
                idx = methodToOverride.getIndex() ;
            }
            else {
                idx = currentClass.incNumberOfMethods() ;
            }
        }
        else {
            idx = currentClass.incNumberOfMethods() ;
        }

            MethodDefinition def = new MethodDefinition(ttype, methodName.getLocation(), signature, idx);
            Symbol symbol = methodName.getName();
            try {
                // we declare the method in the members env
                currentClass.getMembers().declare(symbol, def);
            } catch (EnvironmentExp.DoubleDefException e) {

                throw new ContextualError("method or field already declared: " + symbol.getName(), methodName.getLocation()
                );
            }

            methodName.setDefinition(def);
            methodName.setType(ttype);

            
            // initialization.verifyInitialization(compiler, ttype, currentClass.getMembers(), currentClass);
        }

    // Méthodes ajoutées
    public Type getType() throws ContextualError{
        return methodName.getDefinition().asMethodDefinition("Not a method", getLocation()).getType();
    }

    
/*     public void verifyMethodBody(DecacCompiler compiler,EnvironmentExp envexp,ClassDefinition currentClass)throws ContextualError {

        Type returnType = methodName.getDefinition().asMethodDefinition("Not a method", getLocation()).getType();

        methodBody.verifyMethodBody(compiler,envexp,currentClass,returnType);
    
    } */

    @Override
    protected void verifyDeclMethodBody(DecacCompiler compiler, ClassDefinition currentClass) throws ContextualError {
        // Pass 3
        //method_body ↓env_types ↓env_exp ↓env_exp_params ↓class ↓return
        /* MethodDefinition def = (MethodDefinition) currentClass.getMembers().get(methodName.getName()); */
        MethodDefinition def = currentClass.getMembers().get(methodName.getName()).asMethodDefinition( "Identifier is not a method",methodName.getLocation()); // we already verified that it is a method and cast it as such in pass 2
        /* EnvironmentExp localEnv = new EnvironmentExp(currentClass.getMembers()); */
        EnvironmentExp envExp = currentClass.getMembers(); // This Way we separate env_exp & env_exp_params (it is not necessary but it 
                                                            //translats rules 3.14 & 3.18 in a more literal way)
        EnvironmentExp envParams = new EnvironmentExp(envExp);
        params.verifyListDeclParam(compiler, envParams); 
        //EnvironmentExp localEnv = new EnvironmentExp(envParams);
        //System.out.println("passée par là");
        
        //params.declareParams(envParams);

        methodBody.verifyMethodBody(compiler, envParams, currentClass, def.getType());
    }
    
    @Override
    public AbstractIdentifier getMethodName(){
        return methodName;
    }


    @Override
    public void decompile(IndentPrintStream s) {
        //throw new UnsupportedOperationException("Not yet implemented");
        type.decompile(s);
        s.print(" ");

        methodName.decompile(s);

        s.print("(");
        params.decompile(s);
        s.print(")");

        methodBody.decompile(s);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        type.iter(f);
        methodName.iter(f);
        params.iter(f);
        methodBody.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        methodName.prettyPrint(s, prefix, false);
        params.prettyPrint(s, prefix, false);
        methodBody.prettyPrint(s, prefix, true);
    }

    @Override
    public void codeGenClass(DecacCompiler compiler,Label nomFinMthd){
        int index = 1;
        for (AbstractDeclParam param : params.getList()){
            param.setAdress(index);
            index++;
        }
        methodBody.codeGenClass(compiler, nomFinMthd);
    }

    @Override
    public boolean foldConstants(DecacCompiler compiler){
        boolean changed = false;

        //changed |= params.foldConstants(compiler);
        changed |= methodBody.foldConstants(compiler);

        return changed;
    }

    @Override
    protected boolean eliminateDeadCode(DecacCompiler compiler) {
        return methodBody.eliminateDeadCode(compiler);
    }

    @Override
    protected boolean eliminateDeadStores(DecacCompiler compiler) {
        return methodBody.eliminateDeadStores(compiler);
    }

    @Override
    protected void collectReadVars(Set<Symbol> reads) {
        methodBody.collectReadVars(reads);
    }

    @Override
    protected void collectWrittenVars(Set<Symbol> writes) {
        methodBody.collectWrittenVars(writes);
    }

    @Override
    protected boolean eliminateUnusedVars(DecacCompiler compiler) {
        // calcul local: used = reads ∪ writes pour CE corps de méthode
        Set<Symbol> reads = new HashSet<>();
        Set<Symbol> writes = new HashSet<>();
        methodBody.collectReadVars(reads);
        methodBody.collectWrittenVars(writes);
        reads.addAll(writes); // used

        return methodBody.eliminateUnusedVars(compiler, reads);
    }



}
