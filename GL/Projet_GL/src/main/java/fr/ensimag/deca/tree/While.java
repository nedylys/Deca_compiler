package fr.ensimag.deca.tree;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.BRA;

import java.util.Set;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

/**
 *
 * @author gl43
 * @date 01/01/2026
 */
public class While extends AbstractInst {
    private AbstractExpr condition;
    private ListInst body;

    public AbstractExpr getCondition() {
        return condition;
    }

    public ListInst getBody() {
        return body;
    }

    public While(AbstractExpr condition, ListInst body) {
        Validate.notNull(condition);
        Validate.notNull(body);
        this.condition = condition;
        this.body = body;
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        int n = compiler.getLabelCounter() ;
        Label etiqDebut = new Label("E_Debut." + n) ;
        Label etiqCond = new Label("E_Cond." + n) ;
        compiler.addInstruction(new BRA(etiqCond));
        compiler.addLabel(etiqDebut);
        getBody().codeGenListInst(compiler);
        compiler.addLabel(etiqCond);
        getCondition().codeGenBool(compiler, true, etiqDebut);
    }

    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType)
            throws ContextualError {
            Type condType = condition.verifyExpr(compiler, localEnv, currentClass);
            if (!condType.isBoolean()) {
                throw new ContextualError("While condition must be of type boolean", getCondition().getLocation());
            }
            // Vérification du corps de la boucle
            this.body.verifyListInst(compiler, localEnv, currentClass, returnType);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("while (");
        getCondition().decompile(s);
        s.println(") {");
        s.indent();
        getBody().decompile(s);
        s.unindent();
        s.print("}");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        condition.iter(f);
        body.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        condition.prettyPrint(s, prefix, false);
        body.prettyPrint(s, prefix, true);
    }

    //extension
    @Override
    public boolean foldConstants(DecacCompiler compiler) {
        boolean changed = false;

        AbstractExpr n_cond = condition.foldExpr(compiler);
        if(n_cond != condition){
            condition = n_cond;
            changed = true;
        }
        changed |= body.foldConstants(compiler);
        return changed;
    }

        @Override
    protected void collectReadVars(Set<Symbol> reads) {
        getCondition().collectReadVars(reads);
        getBody().collectReadVars(reads);
    }

    @Override
    public void collectWrittenVars(Set<Symbol> writes) {
        getBody().collectWrittenVars(writes);
    }

    @Override
    protected boolean eliminateDeadCode(DecacCompiler compiler) {
        boolean changed = false;

        changed |= body.eliminateDeadCode(compiler);

        return changed;
    }

    // @Override
    // protected boolean eliminateDeadStores(DecacCompiler compiler) {
    //     return body.eliminateDeadStores(compiler);
    // }

    // @Override
    // protected boolean eliminateUnusedVars(DecacCompiler compiler, Set<Symbol> used) {
    //     return body.eliminateUnusedVars(compiler, used);
    // }




}
