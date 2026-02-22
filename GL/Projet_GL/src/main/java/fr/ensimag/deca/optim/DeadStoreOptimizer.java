package fr.ensimag.deca.optim;

import fr.ensimag.ima.pseudocode.*;
import fr.ensimag.ima.pseudocode.instructions.*;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

public class DeadStoreOptimizer {


    private boolean instructionIsControlOrBranche(Instruction instruction){
        if (instruction instanceof BRA || instruction instanceof BEQ || instruction instanceof BNE ||
        instruction instanceof BLT || instruction instanceof BGT || instruction instanceof BLE ||
        instruction instanceof BGE || instruction instanceof BSR || instruction instanceof RTS ||
        instruction instanceof HALT){
            return true;
        }

        return false;
    }

    private boolean isTerminal(Instruction instruction) {
        if (instruction == null) return false;
        if (instruction instanceof HALT) return true;
        if (instruction instanceof RTS) return true;
        return instruction.getClass().getSimpleName().equals("ERROR");
    }

    /**
 * Dead Store Elimination (DSE) optimizer for IMA programs.
 *
 * <p>
 * This optimization pass removes useless STORE instructions whose written
 * memory locations are never read afterwards.
 * The analysis is performed at the assembly level on an {@link IMAProgram}.
 * </p>
 *
 * <h2>General principle</h2>
 * <ul>
 *   <li>The program is first divided into linear basic blocks.</li>
 *   <li>A block ends when a control or branch instruction is encountered,
 *       or when a label starts a new block.</li>
 *   <li>The optimization is then applied independently on each block.</li>
 * </ul>
 *
 * <h2>Liveness analysis</h2>
 * <p>
 * For each block, a backward liveness analysis is performed on memory
 * addresses ({@link DAddr}):
 * </p>
 * <ul>
 *   <li>A memory address becomes <b>live</b> when it is read (LOAD or used
 *       as an operand).</li>
 *   <li>A STORE instruction is considered <b>dead</b> if its target address
 *       is not live at this point.</li>
 *   <li>If a STORE is kept, its target address is removed from the live set
 *       since it overwrites previous values.
 *
 * Safety restrictions
 *   The optimization is only applied to blocks whose last instruction
 *       is terminal (HALT, RTS, or ERROR).
 *   This restriction avoids incorrect optimizations due to the absence
 *       of a complete control flow graph (CFG).
 *   Only local reasoning is used; no inter-block propagation is assumed.
 * 
 *
 * Limitations:
 *   No global control flow graph is built.
 *   Function calls and non-terminal blocks are conservatively ignored.
 *   Heap or global memory stores are not safely optimized without
 *       further analysis.
 * 
 *
 * This optimization improves generated code by reducing unnecessary memory
 * writes while preserving semantic correctness.
 *
 */
    public void optimize(IMAProgram imaProg) {
        
        LinkedList<AbstractLine> lines = imaProg.getLines();

        // 1) On calcule les blocs SANS modifier la liste
        List<int[]> blocks = new java.util.ArrayList<>();

        // Les STORES sont très sensibles donc on va procéder bloc par bloc
        int start = 0; // ici commence le bloc
        int idx = 0; // et avec ceci on le parcours

        for (AbstractLine line : lines) {
            if (!line.isLine()) {
                // s'il s'agit pas dune ligne on l'ignore
                idx++; // mais il faut avancer comme mếme
                continue;
            }

            Line lineL = (Line) line;
            Instruction instruction = lineL.getInstruction();

            if (lineL.getLabel() != null && idx != start) {
                // rencontrer une ligne admettant un label signifie qu'il s'agit d'un nouvrau bloc
                blocks.add(new int[]{start, idx - 1}); // ainsi le bloc précédent contient les lignes allant de start à idx -1
                start = idx; // on recommance notre bloque
            }

            if (instruction != null && instructionIsControlOrBranche(instruction)) {
                blocks.add(new int[]{start, idx}); // de meme un branchement ferme un bloc (car après lui le parcours n'est plus linéaire)
                start = idx + 1; // un nouveau bloque qui n'a rien à avoir avec le monde avant le branchement ou le control
            }
            idx++;
        }

        if (start < lines.size()) {
            // il s'agit de mettre le denier bloque (si on n'a pas rencontré de derniers branchement ou control)
            blocks.add(new int[]{start, lines.size() - 1});
        }

        // 2) On applique la DSE bloc par bloc (maintenant on peut modifier)
        // on parcours à l'envers pour déterminer si c'est vivant ou pas
        for (int i = blocks.size() - 1; i >= 0; i--) {
            int[] b = blocks.get(i);
            runDseOnBlock(lines, b[0], b[1]);
        }
    }


    private void runDseOnBlock(LinkedList<AbstractLine> lines, int start, int end) {
        if (start > end) {
            // Il s'agit d'un bloc vide : par exemple le bloc HALT va avoir start = 1 et end = 0 à cause de 
            //blocks.add(new int[]{start, idx - 1});
            //start = idx;
            return;
        }

        // Pour ne rien casser il faut récupérer la derniere instruction du bloc et 
        // ne s'implifier que ssi ce block et terminal càd finisse le programmeou une méthode
        // si non on est pas sur si ce Store sera utilisé autre part
        Instruction lastBlockInst = null;
        for (int i = end; i >= start; i--) {
            // On détéremine la derniere instruction de bloc pour vérifier si elle est terminal
            AbstractLine line = lines.get(i);
            
            if (!line.isLine()){ 
                continue;
            }

            Line lineL = (Line) line;

            lastBlockInst = lineL.getInstruction();
            if (lastBlockInst != null) {
                break;
            }
        }

        if (!isTerminal(lastBlockInst)) {
            return; // sans CFG, on ne sait pas si un store sera utilisé après ce block donc on évite le risuqe
        }

        Set<DAddr> live = new HashSet<>();

        // itérateur positionné "après end"
        ListIterator<AbstractLine> iterator = lines.listIterator(end + 1); // on itere jusqu'à la fin du bloc mais pas au delà de ça

        for (int idx = end; idx >= start; idx--) {
            AbstractLine line = iterator.previous(); // recule d'une ligne

            if (!line.isLine()){
                continue;
            }

            Line lineL = (Line) line;
            Instruction instruction = lineL.getInstruction();

            if (instruction == null) {
                continue;
            }

            // Barrière de flot : on peut entrer ici depuis un autre bloc
            // Sans CFG, on suppose toute la mémoire vivante

            /* if (lineL.getLabel() != null) {
                live.clear();
                continue;
            } */

            /* if (instruction instanceof BSR) {
                // encore  c'est un branchement mais cette fois ci c'est risqué car ça touchent aux adresses
                live.clear();
                return;
            } */

            // le reste de branchement/ control finissent les bloques et ne touchent pas au adresses 

            // STORE Rk, addr
             /* if (instruction instanceof STORE) {
                 STORE store = (STORE) instruction;
                 Operand valueOp = store.getOperand2();
                
                 DAddr storeAddr = (DAddr) valueOp;

                 if (!live.contains(storeAddr)) {
                     // store mort : il yen a aucune variable vivante 
                     it.remove(); // ne coute que 0(1) mieux que ma première implémentation qui étatit trop chère
                     continue;
                 } else {
                     // ce store définit addr pour les instructions précédentes donc tous les stores précédentes deviennet
                     // inutiles car ce store les écrasent
                     live.remove(storeAddr);
                     continue;
                 }
                } */

             if (instruction instanceof STORE) {
                STORE store = (STORE) instruction;
                Operand op2 = store.getOperand2();

                if (!(op2 instanceof DAddr)) {
                    continue; // prudence
                }
                DAddr storeAddr = (DAddr) op2;

                // Ne pas supprimer un store non-pile (champ/heap/global) sans analyse globale
                /* if (!isStackAddr(storeAddr)) {
                    //live.clear();
                    continue;
                }  */

                if (!live.contains(storeAddr)) {
                    iterator.remove();
                } else {
                    live.remove(storeAddr);
                }
                continue;
            } 


            // LOAD addr, Rk ceci nous diterator que cette variable est vivante (elle est utilisée quelque part)
            if (instruction instanceof LOAD) {
                LOAD load = (LOAD) instruction;
                Operand valueOp  = load.getOperand1();

                if (valueOp instanceof DAddr) {
                    DAddr loadAddr = (DAddr) valueOp;
                    live.add(loadAddr);
                }

                continue;
            }

            // Instruction binaire DVal -> Reg
            if (instruction instanceof BinaryInstructionDValToReg) {
                Operand valueOp = ((BinaryInstructionDValToReg) instruction).getOperand1();
                if (valueOp instanceof DAddr) {
                    // c'est risqué d'ignorer les opérations
                    DAddr binaryAddr = (DAddr) valueOp;

                    live.add(binaryAddr);
                }
                continue;
            }
        }
    }

    //ajout
    /* private boolean isStackAddr(DAddr a) {
        if (a instanceof RegisterOffset) {
            Register base = ((RegisterOffset) a).getRegister(); // 
            return base == Register.LB || base == Register.SP;
        }
        return false;
    } */
}
