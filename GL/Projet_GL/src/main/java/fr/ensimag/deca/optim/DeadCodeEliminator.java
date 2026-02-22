package fr.ensimag.deca.optim;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tree.AbstractProgram;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

/**
 * Global dead code elimination pass on the Deca abstract syntax tree (AST).
 *
 * This pass coordinates several AST-level optimizations in order to remove
 * useless code before code generation:
 *
 *   <li>dead control-flow code (unreachable instructions)</li>
 *   <li>dead stores (assignments whose values are never read)</li>
 *   <li>unused variables (declared but never used variables)</li>
 *
 * The optimization is applied iteratively until a fixpoint is reached
 * (i.e. no further modification is detected), or a safety limit is hit
 * to prevent non-termination in case of buggy rewrites.
 *
 * This pass operates on the AST and is complementary to low-level
 * IMA optimizations such as dead store elimination at assembly level.
 */
public class DeadCodeEliminator {

    /**
     * Creates a dead code eliminator.
     *
     * This class is stateless and only provides a coordination method
     * for dead code elimination passes.
     */
    public DeadCodeEliminator() {
        // no state
    }

    /**
     * Applies dead code elimination on a whole program AST.
     *
     * The following transformations are applied iteratively:
     *   elimination of unreachable code (dead control-flow)
     *   elimination of dead stores (unused assignments)
     *   elimination of unused variables
     *
     * Between passes, read and written variables are recomputed in order
     * to ensure correctness and allow interactions between optimizations.
     *
     * A guard limits the number of iterations to avoid infinite loops
     * in case the optimizations do not converge.
     *
     * @param compiler the Deca compiler instance
     * @param prog the program AST to optimize
     * @return true if the AST was modified at least once
     */
    public boolean apply(DecacCompiler compiler, AbstractProgram prog) {
        boolean changedAny = false;
        boolean changed;
        int guard = 0;

        do {
            // changed = false;

            // Set<Symbol> readsMain = new HashSet<>();
            // List<Set<Symbol>> readsClasses = new ArrayList<>();
            // prog.collectReadVars(readsMain, readsClasses);

            // changed |= prog.eliminateDeadCode(compiler);
            // changed |= prog.eliminateUnusedVars(compiler, readsMain);
            changed = false;

            changed |= prog.eliminateDeadCode(compiler);
            changed |= prog.eliminateDeadStores(compiler);

            Set<Symbol> readsMain = new HashSet<>();
            List<Set<Symbol>> readsClasses = new ArrayList<>();
            prog.collectReadVars(readsMain, readsClasses);

            Set<Symbol> writesMain = new HashSet<>();
            prog.collectWrittenVars(writesMain);
            Set<Symbol> usedMain = new HashSet<>(readsMain);
            usedMain.addAll(writesMain);

            changed |= prog.eliminateUnusedVars(compiler, usedMain);
            changedAny |= changed;
            guard++;
        } while (changed && guard < 30);

        return changedAny;
    }
}