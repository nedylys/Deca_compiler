package fr.ensimag.deca.tree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.Definition;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.FieldDefinition;
import fr.ensimag.deca.context.ParamDefinition;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.VariableDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.ERROR;
import fr.ensimag.ima.pseudocode.instructions.WNL;
import fr.ensimag.ima.pseudocode.instructions.WSTR;
/**
 * 
 * @author gl43
 * @date 01/01/2026
 */
public class ListInst extends TreeList<AbstractInst> {

    /**
     * Implements non-terminal "list_inst" of [SyntaxeContextuelle] in pass 3
     * @param compiler contains "env_types" attribute
     * @param localEnv corresponds to "env_exp" attribute
     * @param currentClass 
     *          corresponds to "class" attribute (null in the main bloc).
     * @param returnType
     *          corresponds to "return" attribute (void in the main bloc).
     */    
    public void verifyListInst(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType)
            throws ContextualError {
        //throw new UnsupportedOperationException("not yet implemented");
        for (AbstractInst instruction : this.getList()) {
            instruction.verifyInst(compiler, localEnv, currentClass, returnType);
        }
    }

    public void codeGenListInst(DecacCompiler compiler) {
        for (AbstractInst i : getList()) {
            i.codeGenInst(compiler);
        }
    }

    public void codeGenListInstMethod(DecacCompiler compiler,Label finMthd){
        for (AbstractInst i : getList()) {
            if (i instanceof Return){
                i.codeGenInst(compiler);
                compiler.addInstruction(new BRA(finMthd));
                compiler.addLabel(new Label("etiq_fin"+compiler.getLabelCounter()));
                compiler.addInstruction(new WSTR("Erreur : sortie de la methode " + finMthd.toString() + "sans return"));
                compiler.addInstruction(new WNL());
                compiler.addInstruction(new ERROR());
                return;
            } 
            i.codeGenInst(compiler);
        }
    }
    
    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractInst i : getList()) {
            i.decompileInst(s);
            s.println();
        }
    }

    //extension
    public boolean foldConstants(DecacCompiler compiler) {
        boolean changed = false;

        for (int i = 0; i < size(); i++) {
            AbstractInst inst = getModifiableList().get(i);  // getModifiableList().get(i);

            // Si l'instruction est en réalité une expression (ex: Assign; Plus; appel; etc.)
            if (inst instanceof AbstractExpr) {
                AbstractExpr oldE = (AbstractExpr) inst;
                AbstractExpr newE = oldE.foldExpr(compiler);

                if (newE != oldE) {
                    set(i, newE);        //
                    changed = true;
                }
            } else {
                //
                changed |= inst.foldConstants(compiler);
            }
        }
        return changed;
    }

    protected boolean eliminateDeadCode(DecacCompiler compiler) {
        boolean changed = false;
        List<AbstractInst> insts = getModifiableList();

        for (int i = 0; i < insts.size(); i++) {
            AbstractInst inst = insts.get(i);

            // return => tout ce qui suit est mort
            if (inst instanceof Return) {
                int oldSize = insts.size();
                insts.subList(i + 1, oldSize).clear();
                if (insts.size() != oldSize) changed = true;
                break;
            }

            // while(false) => supprime la boucle sans analyser le body
            if (inst instanceof While) {
                While w = (While) inst;
                AbstractExpr cond = w.getCondition();
                if (cond instanceof BooleanLiteral && !((BooleanLiteral) cond).getValue()) {
                    insts.remove(i);
                    changed = true;
                    i--;
                    continue;
                }
            }

            // if(true/false) => remplace par then/else
            if (inst instanceof IfThenElse) {
                IfThenElse ite = (IfThenElse) inst;
                AbstractExpr cond = ite.getCondition();

                if (cond instanceof BooleanLiteral) {
                    boolean v = ((BooleanLiteral) cond).getValue();

                    List<AbstractInst> branch =
                            v ? new ArrayList<>(ite.getThenBranch().getList())
                            : (ite.getElseBranch() == null ? new ArrayList<>()
                                                            : new ArrayList<>(ite.getElseBranch().getList()));

                    insts.remove(i);
                    insts.addAll(i, branch);
                    changed = true;
                    i--;
                    continue;
                }
            }

            // sinon : récursif sur l'instruction
            changed |= inst.eliminateDeadCode(compiler);
        }

        return changed;
    }

    protected void collectReadVars(Set<Symbol> reads) {
        for (AbstractInst inst : getList()) {
            inst.collectReadVars(reads);
        }
    }

        protected void collectWrittenVars(Set<Symbol> writes) {
            for (AbstractInst inst : getList()) {
                inst.collectWrittenVars(writes);
            }
        }

   
    // protected boolean eliminateUnusedVars(DecacCompiler compiler, Set<Symbol> reads) {
    //     boolean changed = false;
    //     for (AbstractInst inst : getList()) {
    //         changed |= inst.eliminateUnusedVars(compiler, reads);
    //     }
    //     return changed;
    // }

    protected boolean eliminateDeadStores(DecacCompiler compiler) {
        boolean changed = false;
        List<AbstractInst> insts = getModifiableList();
        Set<Symbol> live = new HashSet<>();

        Set<Symbol> writesTmp = new HashSet<>();

        for (int i = insts.size() - 1; i >= 0; i--) {
            AbstractInst inst = insts.get(i);

            // Cas spécial Assign
            if (inst instanceof Assign) {
                Assign as = (Assign) inst;

                // if (!(as.getLeftOperand() instanceof Identifier)) {
                //     // affectation vers champ / sélection / tableau
                //     inst.collectReadVars(live);
                //     continue;
                // }

                if (as.getLeftOperand() instanceof Identifier) {
                    Identifier lhs = (Identifier) as.getLeftOperand();
                    Definition def = lhs.getDefinition();

                    if (def instanceof VariableDefinition && !(def instanceof ParamDefinition)) {
                        Symbol x = lhs.getName();
                        AbstractExpr rhs = as.getRightOperand();

                        if (!live.contains(x)) {
                            if (isPure(rhs)) {
                                insts.remove(i);
                                changed = true;
                                continue;
                            }
                        } else {
                            live.remove(x);
                            rhs.collectReadVars(live);
                            continue;
                        }
                    }
                }
            }

            // Cas général : live = (live - writes) U reads
            writesTmp.clear();
            inst.collectWrittenVars(writesTmp);
            live.removeAll(writesTmp);
            inst.collectReadVars(live);
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





}
