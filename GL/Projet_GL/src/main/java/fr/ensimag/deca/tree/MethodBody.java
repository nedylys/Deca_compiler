package fr.ensimag.deca.tree;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.ErrorManager;
import fr.ensimag.deca.codegen.RegisterManager;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Line;
import fr.ensimag.ima.pseudocode.instructions.ADDSP;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.SUBSP;
import fr.ensimag.ima.pseudocode.instructions.TSTO ; 

/* I tried to follow the structure provided in AbstractMain.java */
/**
 * Field declaration
 *
 * @author gl43
 * @date 25/12/2026
 */


public class MethodBody extends AbstractMethodBody{
    //MethodBody 
    // bloc ↓env_types ↓env_exp ↓env_exp_params ↓class ↓return

    // MethodBody[ LIST_DECL_VAR LIST_INST]
    final private ListDeclVar vars;
    final private ListInst insts;

    public MethodBody(ListDeclVar vars, ListInst insts){
        this.vars = vars;
        this.insts = insts;
    }


    @Override
    protected void verifyMethodBody(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass, Type type) throws ContextualError{
        // Same code as Main (MethodBody is an imitation of main)

        vars.verifyListDeclVariable(compiler, localEnv, currentClass);
        insts.verifyListInst(compiler, localEnv, currentClass, type);
    }

        public void decompile(IndentPrintStream s) {
        // Same code as Main (MethodBody is an imitation of main)
        s.println("{");
        s.indent();
        vars.decompile(s);
        insts.decompile(s);
        s.unindent();
        s.println("}");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        // Same code as Main (MethodBody is an imitation of main)
        vars.iter(f);
        insts.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // Same code as Main (MethodBody is an imitation of main)
        vars.prettyPrint(s, prefix, false);
        insts.prettyPrint(s, prefix, true);
    }

    //extension
    @Override
    public boolean foldConstants(DecacCompiler compiler){

         return insts.foldConstants(compiler);
    }
    

    public void codeGenClass(DecacCompiler compiler,Label nomFinMthd){
        compiler.setCurrentMethodEndLabel(nomFinMthd);
        Line lineDebutProg = new Line(new TSTO(0)); // Le nombre de registre tmp
                                                      // sera calculée plus tard
        
        compiler.add(lineDebutProg);

        // correction suite à la soutenance
        compiler.addInstruction(new BOV(ErrorManager.useAndGetLabel(compiler, ErrorManager.ErrorType.STACK_OVERFLOW)));                                        


        int d1 = 0;    // d1 c'est le nombre de places prises dans la pile par la mthd.

        int ancientCurrNbTemp = compiler.getStackManager().getCurrNbTemp();

        RegisterManager regManager = compiler.getRegManager();

        regManager.setIndexFirstFreeReg(2); // signaler ça à Hamza
        
        regManager.activateMthd();

        Deque<GPRegister> activeFreeReg = regManager.getFreeReg();

        regManager.resetfreeRegs();

        ArrayList<Line> linesofPush = new ArrayList<>();

        if (vars.size() > 0){
            compiler.addInstruction(new ADDSP(vars.size()));
        }
        
        d1 += vars.size();
        
        vars.codeGenListDeclVarMthd(compiler);

        // correction suite à la soutenance
        for (int i = 0; i < 14; i++){
            Line notRealLine = new Line( new PUSH(GPRegister.R0));
            compiler.add(notRealLine);
            linesofPush.add(notRealLine); // On va les changes après avoir 
                                         // obtenu les regs utilisés.
         }
        
        insts.codeGenListInstMethod(compiler, nomFinMthd);

        compiler.addLabel(nomFinMthd);

        TreeSet<GPRegister> regsPushed = regManager.regStack();

        int nbrofRegPush = regsPushed.size();

        d1 += nbrofRegPush;
        
        for (int i = nbrofRegPush; i > 0;i--){
            compiler.addInstruction(new POP(GPRegister.getR(i + 1)));
        }

        // correction suite à la soutenance
        if (vars.size() > 0){
            compiler.addInstruction(new SUBSP(vars.size()));
        }
        

        for (int i = 0;i < 14; i++ ){
            Line l = linesofPush.get(i);
            if (nbrofRegPush > 0 ){
                l.setInstruction(new PUSH(GPRegister.getR(i+2)));
                nbrofRegPush--;
            } else{
                compiler.remove(l);
            }
        }

        int newCurrNbTemp = compiler.getStackManager().getCurrNbTemp();

        d1 += newCurrNbTemp - ancientCurrNbTemp; // Si R0 et R1 ont été push
                                                // pendant la méthode 

        if (d1  > 0){
            lineDebutProg.setInstruction(new TSTO(d1));
        } else{
            compiler.remove(lineDebutProg);
        }

        regManager.setFreeReg(activeFreeReg);

        regManager.setIndexFirstFreeReg(2);
        compiler.setCurrentMethodEndLabel(null);
    }    

    @Override
    protected boolean eliminateDeadCode(DecacCompiler compiler){
        return insts.eliminateDeadCode(compiler);
    }

    @Override
    protected void collectReadVars(Set<Symbol> reads) {
        vars.collectReadVars(reads);
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

    @Override
    protected boolean eliminateUnusedVars(DecacCompiler compiler, Set<Symbol> used) {
        boolean changed = false;

        List<AbstractExpr> sideEffects = new ArrayList<>();
        changed |= vars.eliminateUnusedVars(compiler, used, sideEffects);

        if (!sideEffects.isEmpty()) {
            List<AbstractInst> mod = insts.getModifiableList();
            for (int i = sideEffects.size() - 1; i >= 0; i--) {
                mod.add(0, sideEffects.get(i)); 
            }
            changed = true;
        }

        return changed;
    }

}
