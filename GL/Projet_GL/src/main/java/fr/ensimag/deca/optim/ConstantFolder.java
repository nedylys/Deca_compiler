package fr.ensimag.deca.optim;

import org.apache.log4j.Logger;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tree.AbstractProgram;

/**
 * Constant folding pass: evaluates constant-only expressions at compile time.
 *
 * This class orchestrates the foldConstants() traversal implemented in the AST.
 */
public final class ConstantFolder {
    private static final Logger LOG = Logger.getLogger(ConstantFolder.class);

    /** Safety cap to avoid infinite loops in case of buggy rewrites. */
    private static final int MAX_PASSES = 20;

    public ConstantFolder() {
        // utility class
    }

    /**
     * Applies constant folding on the whole program AST.
     *
     * @return true if the AST changed at least once.
     */
    public boolean apply(DecacCompiler compiler, AbstractProgram prog) {
        boolean changedAtLeastOnce = false;
        boolean changed = true;
        int pass = 0;

        while (changed && pass < MAX_PASSES) {
            pass++;
            changed = prog.foldConstants(compiler);
            changedAtLeastOnce |= changed;
        }

        if (pass >= MAX_PASSES) {
            LOG.warn("Constant folding reached MAX_PASSES=" + MAX_PASSES
                    + " (possible non-converging rewrites)");
        } else if (changedAtLeastOnce) {
            LOG.debug("Constant folding converged in " + pass + " pass(es)");
        }

        return changedAtLeastOnce;
    }
}
