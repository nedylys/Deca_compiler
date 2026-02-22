package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;

import java.util.ArrayList;
import java.util.List;
import fr.ensimag.deca.context.Signature;


/**
 * List of expressions (eg list of parameters).
 *
 * @author gl43
 * @date 01/01/2026
 */
public class ListExpr extends TreeList<AbstractExpr> {

    public void verifyRvalueStar(DecacCompiler compiler, EnvironmentExp localEnv, Signature signature, ClassDefinition currentClass) throws ContextualError{
        // condition (implicit) : the number of arguments of a method must be the same 
        if (this.size() != signature.size()){
            throw new ContextualError("Nombre incorrect d'arguments : attendu" + signature.size() + ", trouvé " + this.size(), getLocation());
        }

        List<AbstractExpr> listOfArgs = new ArrayList<>(this.getList()); //MODIF 5 JANV
        for(int i = 0; i < signature.size(); i++){  
            AbstractExpr rvalue = listOfArgs.get(i);
            Type expectedType = signature.get(i);
            AbstractExpr rvalueVerified = rvalue.verifyRValue(compiler, localEnv, currentClass, expectedType);

            listOfArgs.set(i, rvalueVerified); // We can put the new verified value in here (to avoid missing out on the convFloat)
            set(i, rvalueVerified);
            }
        
        }

    @Override
    public void decompile(IndentPrintStream s) {
        //throw new UnsupportedOperationException("Not yet implemented");
        boolean first = true;
        for (AbstractExpr expr : getList()) {
            if (!first) {
                s.print(", ");
            }
            expr.decompile(s);
            first = false;
        }
    }

    //extension
    public boolean foldConstants(DecacCompiler compiler) {
        boolean changed = false;
        for (int i = 0; i < size(); i++) {
            AbstractExpr oldE = getList().get(i);
            AbstractExpr newE = oldE.foldExpr(compiler);
            if (newE != oldE) {
                set(i, newE);
                changed = true;
            }
        }
        return changed;
    }


}
