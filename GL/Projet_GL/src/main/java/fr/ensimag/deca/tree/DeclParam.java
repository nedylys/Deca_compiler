package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.VariableDefinition;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import net.bytebuddy.dynamic.DynamicType.Builder.MethodDefinition.ParameterDefinition;

import fr.ensimag.deca.context.ParamDefinition;

import java.io.PrintStream;
import java.lang.reflect.Parameter;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ExpDefinition;

import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.RegisterOffset;

public class DeclParam extends AbstractDeclParam{
    final private AbstractIdentifier type;
    final private AbstractIdentifier paramName;

    public DeclParam(AbstractIdentifier type, AbstractIdentifier paramName){
        Validate.notNull(paramName);
        Validate.notNull(type);
        this.type = type;
        this.paramName = paramName;
    }

    @Override
    protected Type verifyDeclParam(DecacCompiler compiler, EnvironmentExp localEnv) throws ContextualError{
        // This Method can't be void as the signature expects a Type to be provided for each parameter
        
        // decl_param ↓env_types ↑type
        Type ttype = type.verifyType(compiler); //↑type

        // Condution: type != void
        if (ttype.isVoid()) {
            throw new ContextualError(
                "Parameter cannot be of type void",
                type.getLocation()
            );
        }

        // Implicit Condition : No doubls are allowed
        Symbol symbol = paramName.getName();
        ParamDefinition def = new ParamDefinition(ttype, paramName.getLocation());
        try {
            localEnv.declare(symbol, def);
        } catch (EnvironmentExp.DoubleDefException e) {
            throw new ContextualError(
                "Parameter already declared: " + symbol.getName(),
                paramName.getLocation()
            );
        }

        paramName.setDefinition(def);
        //System.out.println(def.isParam());
        //System.out.println(paramName.getName());
        paramName.setType(ttype);
        return ttype;
    }

    @Override
    public void declareParam(EnvironmentExp localEnv) throws ContextualError{
        // Passe 3 : Declare this param in the method's body
        Type paramType = paramName.getType();
        VariableDefinition def = new VariableDefinition(paramType, paramName.getLocation());

        Symbol symbol = paramName.getName();
        
        try {
            localEnv.declare(symbol, def);
        } catch (EnvironmentExp.DoubleDefException e) {
            // It should never Happen as we already verify there are no doubles in Pass 2
            throw new ContextualError(
                "Parameter already declared (Pass 3): " + paramName.getName().getName(),
                paramName.getLocation()
            );
        }

        paramName.setDefinition(def); // Unlike method and Field we have to reset the definition : the env of method and field is already defined in pass 2(members),
                                      // a parameter on the other hand has to be defined in the env of the method's body 
        
    }

    @Override
    public void decompile(IndentPrintStream s) {
        //throw new UnsupportedOperationException("not yet implemented");
        type.decompile(s);
        s.print(" ");
        paramName.decompile(s);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        type.iter(f);
        paramName.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        paramName.prettyPrint(s, prefix, true);
    }

    @Override
    public void setAdress(int index){
        ExpDefinition paramDef = paramName.getExpDefinition();
        DAddr addr = new RegisterOffset(-index-2,GPRegister.LB);
        paramDef.setOperand(addr);
    }
    
}
