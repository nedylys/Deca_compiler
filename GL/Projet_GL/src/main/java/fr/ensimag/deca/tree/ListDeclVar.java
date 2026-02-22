package fr.ensimag.deca.tree;

//import static org.mockito.ArgumentMatchers.booleanThat;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;

import java.util.Set;
import java.util.List;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
/**
 * List of declarations (e.g. int x; float y,z).
 * 
 * @author gl43
 * @date 01/01/2026
 */
public class ListDeclVar extends TreeList<AbstractDeclVar> {

    @Override
    public void decompile(IndentPrintStream s) {
        //throw new UnsupportedOperationException("Not yet implemented");
        for (AbstractDeclVar var : getList()) {
            var.decompile(s);
            s.println();
        }
    }

    /**
     * Implements non-terminal "list_decl_var" of [SyntaxeContextuelle] in pass 3
     * @param compiler contains the "env_types" attribute
     * @param localEnv 
     *   its "parentEnvironment" corresponds to "env_exp_sup" attribute
     *   in precondition, its "current" dictionary corresponds to 
     *      the "env_exp" attribute
     *   in postcondition, its "current" dictionary corresponds to 
     *      the "env_exp_r" attribute
     * @param currentClass 
     *          corresponds to "class" attribute (null in the main bloc).
     */    
    void verifyListDeclVariable(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {

                // Partie ajoutée
                // Gérer chaque déclaration
                for (AbstractDeclVar decl : this.getList()) {
                    decl.verifyDeclVar(compiler, localEnv, currentClass);
                }
                //Fin partie ajoutée
    }

    /* MEA */
    public void codeGenListDeclVar(DecacCompiler compiler) {
        for (AbstractDeclVar declVAr : getList()) {
            if (declVAr.getType().getDefinition() instanceof ClassDefinition){
               declVAr.codeGenDeclVarClass(compiler); 
            }else{
                declVAr.codeGenDeclVar(compiler);
            }
        }
    }
   
    public void codeGenListDeclVarMthd(DecacCompiler compiler){
        int index = 1;
        for (AbstractDeclVar declVar : getList()){
            declVar.codeGenDeclVarMthd(compiler,index);
            index++;
        }
    }

    //extension
    public boolean foldConstants(DecacCompiler compiler){
        boolean changed = false;

        for (AbstractDeclVar declVar : getList()){
            changed |= declVar.foldConstants(compiler);
        }
        return changed;
    }

    public boolean eliminateUnusedVars(DecacCompiler compiler, Set<Symbol> reads, List<AbstractExpr> sideEffects) {
        boolean changed = false;
        List<AbstractDeclVar> vars = getModifiableList();

        for (int i = 0; i < vars.size(); i++) {
            DeclVar dv = (DeclVar) vars.get(i);
            Symbol x = dv.getName();

            if (!reads.contains(x)) {
                AbstractInitialization ini = dv.getInitialization();
                AbstractExpr init = ini.isInitialization() ? ((Initialization) ini).getExpression() : null;
                if (init != null && !isPure(init)) {
                    sideEffects.add(init);
                }
                vars.remove(i);
                i--;
                changed = true;
            }
        }
        return changed;
    }

    // private boolean isPure(AbstractExpr e) {
    //     return (e instanceof IntLiteral)
    //         || (e instanceof FloatLiteral)
    //         || (e instanceof BooleanLiteral)
    //         || (e instanceof StringLiteral)
    //         || (e instanceof Identifier);
    // }

    private boolean isPure(AbstractExpr e) {
        if (e == null) return true;

        // Effets de bord => NON pur
        if (e instanceof MethodCall || e instanceof New ||
             e instanceof ReadInt || e instanceof ReadFloat || e instanceof Cast) {
            return false;
        }

        // Littéraux / ident / null / this => pur
        if (e instanceof IntLiteral || e instanceof FloatLiteral || e instanceof BooleanLiteral
            || e instanceof StringLiteral || e instanceof Identifier || e instanceof Null
            || e instanceof This) {
            return true;
        }

        // Unaires / cast / conv => pur si operande pur
        if (e instanceof AbstractUnaryExpr) {
            return isPure(((AbstractUnaryExpr) e).getOperand());
        }
        /* if (e instanceof Cast) {
            return isPure(((Cast) e).getExpression());
        } */
        if (e instanceof ConvFloat) {
            return isPure(((ConvFloat) e).getOperand());
        }

        // Binaires => pur si 2 côtés purs
        if (e instanceof AbstractBinaryExpr) {
            return isPure(((AbstractBinaryExpr) e).getLeftOperand())
                && isPure(((AbstractBinaryExpr) e).getRightOperand());
        }

        return false;
    }


    public void collectReadVars(Set<Symbol> reads) {
        for (AbstractDeclVar dv : getList()) {
            dv.collectReadVars(reads);
        }
    }

}
