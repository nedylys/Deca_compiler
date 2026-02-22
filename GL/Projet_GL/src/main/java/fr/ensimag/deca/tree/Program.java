package fr.ensimag.deca.tree;
import java.io.PrintStream;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.ErrorManager;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Line;
import fr.ensimag.ima.pseudocode.instructions.ADDSP;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.HALT;
import fr.ensimag.ima.pseudocode.instructions.RTS;
import fr.ensimag.ima.pseudocode.instructions.TSTO;

/**
 * Deca complete program (class definition plus main block)
 *
 * @author gl43
 * @date 01/01/2026
 */
public class Program extends AbstractProgram {
    private static final Logger LOG = Logger.getLogger(Program.class);
    
    public Program(ListDeclClass classes, AbstractMain main) {
        Validate.notNull(classes);
        Validate.notNull(main);
        this.classes = classes;
        this.main = main;
        if (classes.getList().size() == 0){
            this.isOOP = false;
        }
        else{
            this.isOOP = true;
        }
    }


    @Override
    public boolean getIsOOP(){
        return isOOP;
    }
    
    public ListDeclClass getClasses() {
        return classes;
    }
    public AbstractMain getMain() {
        return main;
    }
    private ListDeclClass classes;
    private AbstractMain main;
    @Override
    public void verifyProgram(DecacCompiler compiler) throws ContextualError {
        LOG.debug("verify program: start");
        // verif des de la partie droite de la règle
        classes.verifyListClass(compiler);
        classes.verifyListClassMembers(compiler);
        classes.verifyListClassBody(compiler);
        main.verifyMain(compiler);
        /* classes.verifyListClass(compiler); */
        LOG.debug("verify program: end ");
    }
    @Override
    public void codeGenProgram(DecacCompiler compiler) {
        // A FAIRE: compléter ce squelette très rudimentaire de code
        // complété par MEA 
        /* test du débordement de la pile */

        // d1 : espace pour les tables de méthodes
        // d2 : espace pour les variables globales
        // d3 : taille maximale de la pile
        
        int d1 = 2; // Par défaut le pointeur null et Object.equals
        
        for (AbstractDeclClass c : classes.getList()){
            d1 += c.getName().getClassDefinition().getNumberOfMethods() + 1;
        }

        int d2 = 0;
        if (main instanceof Main){
           Main realMain = (Main) main;
           d2 = realMain.getDeclVar().size() + d1; 
        }
        
        Label fullStack = ErrorManager.getErrorLabel(ErrorManager.ErrorType.STACK_OVERFLOW) ;

        //classes.buildEtiquetteTable();
        //classes.buildTableMethd(compiler);

        

        classes.codeGenClass(compiler);

        
        compiler.addComment("Main program");
        main.codeGenMain(compiler);


        /* Prologue (en tête) */
  
        int d3 = compiler.getStackManager().getMaxNbTemp() + d2; 
        if (!compiler.isNoCheck() && d3 != 0) {
            // correction suite à la soutenance 
            compiler.addFirst(new ADDSP(d2));
            compiler.addFirst(new BOV(fullStack));
            compiler.addFirst(new TSTO(d3), "Taille maximale de la pile");
        }
        else {
            compiler.addFirst(new ADDSP(d2));
        }


        
        compiler.addFirst(new Line("test de la taille de la pile"));
        compiler.addInstruction(new HALT());
        compiler.addComment("end main program");

        // correction suite à la soutenance 
        compiler.addLabel(new Label("init.Object"));
        compiler.addInstruction(new RTS());
        
        if (!compiler.isNoCheck()) {
            compiler.useError(ErrorManager.ErrorType.STACK_OVERFLOW);
        }
        classes.codeGenClassMthd(compiler);
        ErrorManager.genCodeErrors(compiler, compiler.getUsedErrors());
    }
    @Override
    public void decompile(IndentPrintStream s) {
        getClasses().decompile(s);
        getMain().decompile(s);
    }
    
    @Override
    protected void iterChildren(TreeFunction f) {
        classes.iter(f);
        main.iter(f);
    }
    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        classes.prettyPrint(s, prefix, false);
        main.prettyPrint(s, prefix, true);
    }

    //extension 
    @Override 
    public boolean foldConstants(DecacCompiler compiler){ 
        boolean changed = false;
        changed |= classes.foldConstants(compiler);
        changed |= main.foldConstants(compiler);
        return changed; 
    }

    @Override
    public boolean eliminateDeadCode(DecacCompiler compiler){
        boolean changed = false;

        changed |= main.eliminateDeadCode(compiler);

        //Set<Definition> readsMain = new HashSet<>();
        //main.collectReadVars(readsMain);

        //changed |= main.eliminateUnusedVars(compiler,readsMain);
        
        changed |= classes.eliminateDeadCode(compiler);

        
        return changed;
    }

    @Override
    public void collectReadVars(Set<Symbol> readsMain, List<Set<Symbol>> readsClasses) {
        classes.collectReadVars(readsClasses);  // ignore pour l'instant
        main.collectReadVars(readsMain);
    }

    @Override
    public boolean eliminateUnusedVars(DecacCompiler compiler, Set<Symbol> usedMain) {
        boolean changed = false;
        changed |= main.eliminateUnusedVars(compiler, usedMain);
        changed |= classes.eliminateUnusedVars(compiler); // calc local par méthode
        return changed;
    }

    @Override
    public void collectWrittenVars(Set<Symbol> writesMain) {
        main.collectWrittenVars(writesMain);
    }


    @Override
    public boolean eliminateDeadStores(DecacCompiler compiler) {
        boolean changed = false;
        changed |= main.eliminateDeadStores(compiler);
        changed |= classes.eliminateDeadStores(compiler);
        return changed;
    }
}
