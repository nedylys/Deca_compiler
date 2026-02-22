package fr.ensimag.deca.tree;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.instructions.STORE;


import java.util.Set;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
/**
 * @author gl43
 * @date 01/01/2026
 */
public class Initialization extends AbstractInitialization {

    public AbstractExpr getExpression() {
        return expression;
    }

    private AbstractExpr expression;

    public void setExpression(AbstractExpr expression) {
        Validate.notNull(expression);
        this.expression = expression;
    }

    public Initialization(AbstractExpr expression) {
        Validate.notNull(expression);
        this.expression = expression;
    }

    // F j'ai implémenté cette méthode :
    @Override
    protected void verifyInitialization(DecacCompiler compiler, Type t,
            EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        //throw new UnsupportedOperationException("not yet implemented");
        Type expression_type = this.expression.verifyExpr(compiler, localEnv, currentClass);
        //Ajouter la conversion int to float ici
/*         if(t.isFloat() && expression_type.isInt()){
            ConvFloat conv = new ConvFloat(expression);
            conv.setLocation(expression.getLocation());
            expression = conv;
            expression_type = compiler.environmentType.FLOAT;
            conv.setType(expression_type);
        } */
        //
        this.expression = expression.verifyRValue(compiler, localEnv, currentClass, t);
/*         if (!expression_type.assignCompatible(t)) {
            // Règle : les types doivent compatiblepour une initialisation
            throw new ContextualError("Incompatible Types: Can't initialize a variable of type " + t.getName() + " with an expression of type " + expression_type.getName(), this.getLocation());
        } */


    }


    @Override
    public void decompile(IndentPrintStream s) {
        //throw new UnsupportedOperationException("Not yet implemented");
        s.print(" = ");
        expression.decompile(s);
    }

    @Override
    protected
    void iterChildren(TreeFunction f) {
        expression.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        expression.prettyPrint(s, prefix, true);
    }

    @Override 
    protected void codeGenInitialization(DecacCompiler compiler, DAddr adrr) {
        GPRegister r = GPRegister.R1 ;
        expression.codeGenExpr(compiler, r);
        compiler.addInstruction(new STORE(r, adrr));
    }

    @Override
    public void codeGenInitializationField(DecacCompiler compiler,DAddr adrr,Type type){
        codeGenInitialization(compiler, adrr);
    }
    @Override
    public boolean isInitialization() {
        return true;
    }
    
    public void codeGenInitializationTargetR(DecacCompiler compiler, DAddr adrr,GPRegister targetR) {
        expression.codeGenExpr(compiler, targetR);
        compiler.addInstruction(new STORE(targetR, adrr));
    }

    //extension
    @Override
    public boolean foldConstants(DecacCompiler compiler){
        // boolean changed =  false;

        AbstractExpr n_expression = expression.foldExpr(compiler);

        if(n_expression != expression){
            expression = n_expression;
            return true;
        }

        return false;
    }

    @Override
    public void collectReadVars(Set<Symbol> reads) {
        AbstractInitialization init = this;
        if (init != null) {
            AbstractExpr expr = getExpression(); 
            if (expr != null) expr.collectReadVars(reads);
        }
    }
}
