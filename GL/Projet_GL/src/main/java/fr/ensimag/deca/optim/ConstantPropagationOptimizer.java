package fr.ensimag.deca.optim;

import fr.ensimag.ima.pseudocode.IMAProgram;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.AbstractLine;
import fr.ensimag.ima.pseudocode.BinaryInstructionDValToReg;
import fr.ensimag.ima.pseudocode.GPRegister;

import java.util.LinkedList;
import fr.ensimag.ima.pseudocode.Line;
import fr.ensimag.ima.pseudocode.Instruction;
import fr.ensimag.ima.pseudocode.Operand;

import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import fr.ensimag.ima.pseudocode.DAddr;

// Branchement et contrôle
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.BNE;
import fr.ensimag.ima.pseudocode.instructions.BLT;
import fr.ensimag.ima.pseudocode.instructions.BGT;
import fr.ensimag.ima.pseudocode.instructions.BLE;
import fr.ensimag.ima.pseudocode.instructions.BGE;
import fr.ensimag.ima.pseudocode.instructions.BSR;
import fr.ensimag.ima.pseudocode.instructions.RTS;
import fr.ensimag.ima.pseudocode.instructions.HALT;

import java.util.HashMap;
/**
 * Constant propagation optimization pass on IMA code.
 *
 * This optimizer tracks constant values stored in registers and memory
 * locations (addresses) and propagates them forward to replace loads
 * from memory by immediate constants whenever it is safe to do so.
 *
 * The analysis is purely local and linear: it does not build a full control-flow
 * graph (CFG). As a consequence, propagation is conservatively stopped at
 * control-flow barriers such as labels, branches, calls and returns.
 *
 * This pass operates after code generation, directly on the IMA instruction
 * list, and complements AST-level constant folding by exposing new
 * optimization opportunities at the assembly level.
 */

public class ConstantPropagationOptimizer {

    

    public ConstantPropagationOptimizer() {
        // Rien à faire
    }

    /* Barrière de flot de contrôle */
    private boolean instructionIsControlOrBranche(Instruction instruction) {
        return instruction instanceof BRA || instruction instanceof BEQ ||
               instruction instanceof BNE || instruction instanceof BLT ||
               instruction instanceof BGT || instruction instanceof BLE ||
               instruction instanceof BGE || instruction instanceof BSR ||
               instruction instanceof RTS || instruction instanceof HALT;
    }

    /**
     * Applies constant propagation on an IMA program.
     *
     * The optimizer scans instructions sequentially and maintains:
     *   a mapping from memory addresses to constant values
     *   a mapping from registers to constant values
     *
     * When a {@code LOAD} instruction reads from a memory address known
     * to hold a constant, it is replaced by a {@code LOAD immediate}
     * instruction.
     *
     * Propagation is conservatively reset at control-flow barriers
     * (labels, branches, calls, returns, halt) to preserve correctness
     * without requiring a full control-flow graph.
     *
     * @param imaProg the IMA program to optimize
     */

    public void optimize(IMAProgram imaProg) {

        LinkedList<AbstractLine> lines = imaProg.getLines();

        /*
        * L'objectif de cet optimiseur est de propager les constantes
        * afin de transformer par exemple : a = 3; -> b = a; -> c = b; en : c = 3;*/

        /*whoAmI : x -> 3   signifie que la variable x vaut toujours 3
         /* whatRAmI :  R1 -> 3  signifie que le registre R1 contient 3*/
        HashMap<DAddr, ImmediateInteger> whoAmI = new HashMap<>();
        HashMap<Integer, ImmediateInteger> whatRAmI = new HashMap<>();

        for (AbstractLine line : lines) {

            if (!line.isLine()) {
                continue;
            }

            Line lineL = (Line) line;
            Instruction instruction = lineL.getInstruction();

            // Barrière label même si pas d'instruction
            if (lineL.getLabel() != null) {
                whoAmI.clear();
                whatRAmI.clear();
                continue;
            }
        
            if (instruction == null) {
                continue;
            }

             // IL s'avère qu'il suffit pas de ne bloquer que les instruction de control et branchement
            // mais tout ce qui contient un label + les instructions de control et branchement
            // car on peut arriver à L sans passer par un branchement par exemple
            if (instructionIsControlOrBranche(instruction) || lineL.getLabel() != null) {
                whoAmI.clear();
                whatRAmI.clear();
                continue;
            }
            
             //LOAD #k, Ri
             // LOAD x,  Ri
             
            if (instruction instanceof LOAD) {

                LOAD loadInst = (LOAD) instruction;
                Operand valueOp = loadInst.getOperand1();
                GPRegister register = (GPRegister) loadInst.getOperand2();
                int registerNum = register.getNumber();

                // LOAD #k, Ri : contrairement à copypropagation icic on s'intéresse pas qu'aux adresses
                if (valueOp instanceof ImmediateInteger) {
                    // Ici une valeur étant constante on va la ixer pour la propager si possible
                    whatRAmI.put(registerNum, (ImmediateInteger) valueOp);
                    continue;
                }

                /* LOAD x, Ri */
                if (valueOp instanceof DAddr) {
                    // Maintenant il faut faire attention , on a préciser DAddr simple 
                    // meme si ça impliquent un RegistreOffset, car dans ce cas ça se complique 
                    // mais en expérimentant j'ai remarqué que jamais les RegOff de type k(Ri) sont
                    // optimisés, ceci est parceque les load .. off(GB/LB) sont faites une seule fois alors que
                    // Les load .. off(R) peuvent se faire plusierus fois, auquel cas java créer à chaque
                    // fois une nouvelle instance de RegOff et whoAmI ne la reconnait jamais, exple:
                    // le code java fait :
                    /* RegisterOffset regOff1 = new RegisterOffset(1, Ri);
                       RegisterOffset regOff2 = new RegisterOffset(1, Ri);
                       et donc meme si les 2 sont 1(Ri) whoAmI ne le connait pas et donc considere regOff2
                       comme un nouveau Reg et dit non je l'ai jamais vu avant.
                        */
                    DAddr variable = (DAddr) valueOp;
                    ImmediateInteger constant = whoAmI.get(variable);

                    if (constant != null) {
                        // Propagation de constante
                        lineL.setInstruction(new LOAD(constant, register));
                        whatRAmI.put(registerNum, constant);
                    } else {
                        // Valeur inconnue
                        whatRAmI.remove(registerNum);
                    }
                    continue;
                }

                whatRAmI.remove(registerNum);
                continue;
            }

            // IL faut mettre ceci entre le LOAD et STORE car voir si a a changé (son registre est écrasé)
            // la propagation n'est plus valide
            // Un registre devient invalide si il est écrasé par une opération de type DVal To Reg
            if ((instruction instanceof BinaryInstructionDValToReg)) {

                BinaryInstructionDValToReg bin =
                        (BinaryInstructionDValToReg) instruction;

                GPRegister register = (GPRegister) bin.getOperand2();
                whatRAmI.remove(register.getNumber());
                continue;
            }

          
            
            // STORE Ri, x
            if (instruction instanceof STORE) {

                STORE storeInst = (STORE) instruction;
                GPRegister register = (GPRegister) storeInst.getOperand1();
                DAddr variable = (DAddr) storeInst.getOperand2();
                int registerNum = register.getNumber();

                ImmediateInteger constant = whatRAmI.get(registerNum);

                if (constant != null) {
                    // La variable devient constante
                    whoAmI.put(variable, constant);
                } else {
                    // Valeur inconnue faut se débarasser d'elle
                    whoAmI.remove(variable);
                }
                continue;
            }
        }
    }
}
