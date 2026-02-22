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

import java.util.HashSet;
import java.util.Set;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
/**
 * Full if/else if/else statement.
 *
 * @author gl43
 * @date 01/01/2026
 */
public class IfThenElse extends AbstractInst {
    
    private AbstractExpr condition; 
    private final ListInst thenBranch;
    private ListInst elseBranch;

    public IfThenElse(AbstractExpr condition, ListInst thenBranch, ListInst elseBranch) {
        // Validation des arguments non nuls, donc on n'a pas besoin de les vérifier ailleurs (dans VerifyInst par exemple)
        Validate.notNull(condition);
        Validate.notNull(thenBranch);
        Validate.notNull(elseBranch);
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }
    
    // F j'ai implémenté cette méthode :
    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType)
            throws ContextualError {
            Type cond_type = condition.verifyExpr(compiler, localEnv, currentClass);
            if (!cond_type.isBoolean()) {
                // Règle : la condition doit être de type booléen
                throw new ContextualError("If condition must be of type boolean", condition.getLocation());
            }
            // Vérification des instructions dans then et else
            thenBranch.verifyListInst(compiler, localEnv, currentClass, returnType);
            elseBranch.verifyListInst(compiler, localEnv, currentClass, returnType);
    }

    @Override
    // voir page 225
    protected void codeGenInst(DecacCompiler compiler) {
        int n = compiler.getLabelCounter() ;
        Label etiqSinon = new Label("E_Sinon." + n) ;
        Label etiqFin = new Label("E_Fin." + n) ;
        
        condition.codeGenBool(compiler, false, etiqSinon);
        thenBranch.codeGenListInst(compiler);
        compiler.addInstruction(new BRA(etiqFin));

        compiler.addLabel(etiqSinon);
        if (elseBranch != null) {
            elseBranch.codeGenListInst(compiler);
        }

        compiler.addLabel(etiqFin);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        //throw new UnsupportedOperationException("not yet implemented");
        s.print("if (");
        condition.decompile(s);
        s.println(") {");
        
        s.indent();
        thenBranch.decompile(s);
        s.unindent();
        
        s.print("}");
        
        if (elseBranch != null) {
            s.println(" else {");
            s.indent();
            elseBranch.decompile(s);
            s.unindent();
            s.println("}");
        } else {
            s.println();
        }
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        condition.iter(f);
        thenBranch.iter(f);
        elseBranch.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        condition.prettyPrint(s, prefix, false);
        thenBranch.prettyPrint(s, prefix, false);
        elseBranch.prettyPrint(s, prefix, true);
    }

    //extension
    @Override
    public boolean foldConstants(DecacCompiler compiler){
        boolean changed = false;

        AbstractExpr n_cond = condition.foldExpr(compiler);

        if(n_cond != condition){
            condition = n_cond;
            changed = true;
        }

        changed |= thenBranch.foldConstants(compiler);
        changed |= elseBranch.foldConstants(compiler);

        return changed;
    }

    @Override
    public void collectReadVars(Set<Symbol> reads) {
        getCondition().collectReadVars(reads);
        getThenBranch().collectReadVars(reads);
        if (getElseBranch() != null) {
            getElseBranch().collectReadVars(reads);
        }
    }


    public AbstractExpr getCondition(){
        return condition;
    }

    public ListInst getThenBranch(){
        return thenBranch;
    }

    public ListInst getElseBranch(){
        return elseBranch;
    }

    @Override
    public void collectWrittenVars(Set<Symbol> writes) {
        getThenBranch().collectWrittenVars(writes);
        if (getElseBranch() != null) getElseBranch().collectWrittenVars(writes);
    }

    @Override
    protected boolean eliminateDeadCode(DecacCompiler compiler) {
        boolean changed = false;

        changed |= thenBranch.eliminateDeadCode(compiler);

        if (elseBranch != null) {
            changed |= elseBranch.eliminateDeadCode(compiler);
        }

        return changed;
    }

    // @Override
    // protected boolean eliminateDeadStores(DecacCompiler compiler) {
    //     boolean changed = false;

    //     changed |= thenBranch.eliminateDeadStores(compiler);

    //     if (elseBranch != null) {
    //         changed |= elseBranch.eliminateDeadStores(compiler);
    //     }

    //     return changed;
    // }

    // @Override
    // protected boolean eliminateUnusedVars(DecacCompiler compiler, Set<Symbol> used) {
    //     boolean changed = false;

    //     changed |= thenBranch.eliminateUnusedVars(compiler, new HashSet<>(used));

    //     if (elseBranch != null) {
    //         changed |= elseBranch.eliminateUnusedVars(compiler, new HashSet<>(used));
    //     }

    //     return changed;
    // }




}
