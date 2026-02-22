package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.VariableDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

import fr.ensimag.deca.context.Signature;

import static org.mockito.ArgumentMatchers.booleanThat;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;

public class ListDeclParam extends TreeList<AbstractDeclParam> {
    public Signature verifyListDeclParam(DecacCompiler compiler, EnvironmentExp envParam) throws ContextualError {
        // list_decl_param ↓env_types ↑sig
        Signature signature = new Signature(); // {sig := []}
        // Again it is not like ListDeclVar : this is pass 2 
        //declareParams(localEnv);
        for(AbstractDeclParam param : this.getList()){
            Type paramtype = param.verifyDeclParam(compiler, envParam); // decl_param ↓env_types ↑type
            signature.add(paramtype);   //sig := sig @ [type]
        }

        return signature;

    }


    public void declareParams(EnvironmentExp localEnv) throws ContextualError {
            /** Passe 3 : declare parametres in the env  of the body */
        for (AbstractDeclParam p : getList()) {
            
            p.declareParam(localEnv);
        }
    }

    @Override
    public void decompile(IndentPrintStream s) {
        //throw new UnsupportedOperationException("not yet implemented");
        boolean f = true;
    for (AbstractDeclParam param : getList()) {
        if (!f) {
            s.print(", ");
        }
        param.decompile(s);
        f = false;
    }
    }

    //extension
    public boolean foldConstants(DecacCompiler compiler){
        boolean changed = false;

        for(AbstractDeclParam declParam : getList()){
            changed |= declParam.foldConstants(compiler);
        }

        return changed;
    }
}