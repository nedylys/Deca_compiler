package fr.ensimag.deca.tree;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

/**
 * @author gl43
 * @date 01/01/2026
 */
public class Main extends AbstractMain {
    private static final Logger LOG = Logger.getLogger(Main.class);
    
    private ListDeclVar declVariables;
    private ListInst insts;
    public Main(ListDeclVar declVariables,
            ListInst insts) {
        Validate.notNull(declVariables);
        Validate.notNull(insts);
        this.declVariables = declVariables;
        this.insts = insts;
    }

    @Override
    protected void verifyMain(DecacCompiler compiler) throws ContextualError {
        LOG.debug("verify Main: start ");
        // A FAIRE: Appeler méthodes "verify*" de ListDeclVarSet et ListInst.
        // Vous avez le droit de changer le profil fourni pour ces méthodes
        // (mais ce n'est à priori pas nécessaire).
        LOG.debug("verify Main: end");
        //throw new UnsupportedOperationException("not yet implemented");
        // Initialisaton d'un environneent d'exp à l'ensemble vide
        EnvironmentExp envexp = new EnvironmentExp(null);

        //verif declarations
        declVariables.verifyListDeclVariable(compiler, envexp, null);

        // verif instructions
        insts.verifyListInst(compiler, envexp, null, null);
    }

    @Override
    protected void codeGenMain(DecacCompiler compiler) {
        // A FAIRE: traiter les déclarations de variables.
        // Update : fait par MEA 

        // On crée la table des méthodes pour la superClasse Object.
        // Je sais pas si ça doit être fait la 
/*      GPRegister R0 = GPRegister.getR(0);
        compiler.addInstruction(new LOAD(new NullOperand(),R0));
        compiler.addInstruction(new STORE(R0,new RegisterOffset(1, Register.GB)));
        compiler.addInstruction(new LOAD(new LabelOperand(new Label("code.Object.equals")),R0));
        compiler.addInstruction(new STORE(R0,new RegisterOffset(2, Register.GB)));
        */
        
        compiler.addComment("Beginning of main declarations:");
        declVariables.codeGenListDeclVar(compiler);
        
        compiler.addComment("Beginning of main instructions:");
        insts.codeGenListInst(compiler);
    }
    
    @Override
    public void decompile(IndentPrintStream s) {
        s.println("{");
        s.indent();
        declVariables.decompile(s);
        insts.decompile(s);
        s.unindent();
        s.println("}");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        declVariables.iter(f);
        insts.iter(f);
    }
 
    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        declVariables.prettyPrint(s, prefix, false);
        insts.prettyPrint(s, prefix, true);
    }

    /* Getter pour les liste des variables déclarées (MEA) */
    public ListDeclVar getDeclVar() {
        return declVariables ;
    }

    //extension
    @Override
    public boolean foldConstants(DecacCompiler compiler) {
        boolean changed = false;
        
        changed |= declVariables.foldConstants(compiler);
        changed |= insts.foldConstants(compiler);

        return changed;
    }

    @Override
    protected boolean eliminateDeadCode(DecacCompiler compiler){

        return insts.eliminateDeadCode(compiler);
       
    }

    @Override
    protected boolean eliminateUnusedVars(DecacCompiler compiler, Set<Symbol> reads) {
        boolean changed = false;

        // récupère les init à garder comme instructions
        List<AbstractExpr> sideEffects = new ArrayList<>();
        changed |= declVariables.eliminateUnusedVars(compiler, reads, sideEffects);

        if (!sideEffects.isEmpty()) {
            // on insère au début des insts du main
            List<AbstractInst> mod = insts.getModifiableList();
            for (int i = sideEffects.size() - 1; i >= 0; i--) {
                mod.add(0, sideEffects.get(i)); 
            }
            changed = true;
        }

        
        //changed |= insts.eliminateUnusedVars(compiler, reads);

        return changed;
    }

    @Override
    protected void collectReadVars(Set<Symbol> reads) {
        declVariables.collectReadVars(reads); // 
        insts.collectReadVars(reads);
    }

    @Override
    protected void collectWrittenVars(Set<Symbol> writes) {
        insts.collectWrittenVars(writes);
    }

    @Override
    protected boolean eliminateDeadStores(DecacCompiler compiler) {
        return insts.eliminateDeadStores(compiler);
    }
}
