package fr.ensimag.deca.optim;
import fr.ensimag.ima.pseudocode.IMAProgram;

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




//Instructions imports

import fr.ensimag.ima.pseudocode.instructions.CMP;

// Branchement et control
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

/*
*Copy propagation optimization pass on IMA code.
*
* This otimizer detects chain of variable-to-variable assignements
* and propagates original sources forward in order to eliminate
* redundant memory loads.
*
* Example:
*  b = a;
*  c = b;
*  d = c;
*
* becomes
*  d = a;
*
*
* The optimization is local and linear: no control-flow graph (CFG)
* is constructed. For correctness, propagation is conservatively
* stopped at control-flow barriers such as labels, branches,
* procedure calls and returns.
*
*
* This pass operates at the IMA assembly level and complements
* AST-level optimizations such as dead store elimination and
* constant folding
*
*/

public class CopyPropagationOptimizer {
    private boolean changed;

    public CopyPropagationOptimizer(){
        this.changed = false;
    }


    private boolean instructionIsControlOrBranche(Instruction instruction){
        if (instruction instanceof BRA || instruction instanceof BEQ || instruction instanceof BNE ||
        instruction instanceof BLT || instruction instanceof BGT || instruction instanceof BLE ||
        instruction instanceof BGE || instruction instanceof BSR || instruction instanceof RTS ||
        instruction instanceof HALT){
            return true;
        }

        return false;
    }


/*     private void changedRefernce(HashMap<DAddr, DAddr> whoAmI, DAddr modifiedV){
        boolean wasLinked = false;
        do{
            wasLinked = false;
            // on ne peut pas utiliser un iterator (keyset)s directement sur whoAmI dans un for-eac
            // ceci génerera une ConcurrentModificationException
            // donc il faut plutot mettre une copie dans le for each
            for (DAddr key : new HashMap<DAddr, DAddr>(whoAmI).keySet()){
                DAddr linked = whoAmI.get(key);
                if (linked != null && linked.equals(modifiedV)){
                    whoAmI.remove(key);
                    wasLinked = true;
                }
            }
        } while (wasLinked);
    }
 */

    /*
    * Applies copy propagation on an IMA program.
    *
    * The optimizer tracks:
    * 
    *   variable-to-variable equivalences (memory copies)
    *   register-to-variable associations
    * 
    * When a variable is detected to be a simple copy of another,
    * subsequent loads are rewritten to directly load the original
    * source variable
    * Propagation is reset at control-flow barriers (labels, branches,
    * calls, returns, halt) to preserve correctness without requiring
    * a full control-flow graph.
    * 
     */
    public void optimize(IMAProgram imaProg){
        LinkedList<AbstractLine> lines = imaProg.getLines();
        /*L'intéret de ce optimizer est de faire propager les variables pour supprimer 
        du code inutile , par exmple:
        b = a -> c = b -> d = c -> e = d -> print(d) revent à faire print(a) */
        /*Pour ce faire, il faut garantir qu'une logique permettant de se rappeller de
        ces lasons d'affectation existe */

        // On adopte alors comme collection des Hashmap tq h[b] = a = > a = b est une inst du code

        // b = a
        HashMap<DAddr, DAddr> whoAmI = new HashMap<>();
        HashMap<Integer, DAddr> whatRAmI = new HashMap<>();

        // Ri est le resgistre associé à a (LOAD b Ri)

        for (AbstractLine line : lines){
            if (!line.isLine()){
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
            
            if (instruction == null){
                continue;
            }
/*             if (instructionIsControlOrBranche(instruction)){
                continue;
            } */

            // IL s'avère qu'il suffit pas de ne bloquer que les instruction de control et branchement
            // mais tout ce qui contient un label + les instructions de control et branchement
            // car on peut arriver à L sans passer par un branchement par exemple

            if (instructionIsControlOrBranche(instruction)){
                // on ne risque plus rien un ce stad un branchement ou label change toute notre perspective
                whoAmI.clear();
                whatRAmI.clear();
                continue;
            }

            // LOAD a R1
            if(instruction instanceof LOAD){
                LOAD loadInst = (LOAD) instruction;
                Operand valueOp = loadInst.getOperand1();
                Operand registerOp = loadInst.getOperand2(); 
                GPRegister register = (GPRegister) registerOp;

                int registerNum = register.getNumber();

                if (!(valueOp instanceof DAddr)){
                    // On ne s'intérese qu'aux adresses (variables)
                    // donc si un registre est utilisé pour autre choses (immédiat) on ne considère plus son registre à ce moment
                    whatRAmI.remove(registerNum);
                    continue;
                }

                else{
                    DAddr variableB = (DAddr) valueOp;
                    DAddr variableA = whoAmI.get(variableB);

                    if (variableA != null){
                        // dans ce cas Ri est lié à variableA car variableB n'est qu'une propagation de cette dernière
                        lineL.setInstruction(new LOAD(variableA, register));
                        whatRAmI.put(registerNum, variableA); // et on met le registre à jour
                    }

                    else{
                        // si ce LOAD n'est pas encore en considération c'est qu'il y a pas de propagation de a et
                        // on se contente de nous rappeller de quel registre il s'agit
                        whatRAmI.put(registerNum, variableB);
                    }
                    continue;
                }
            }

            // IL faut mettre ceci entre le LOAD et STORE car voir si a a changé (son registre est écrasé)
            // la propagation n'est plus valide
            // Un registre devient invalide si il est écrasé par une opération de type DVal To Reg
            if ((instruction instanceof BinaryInstructionDValToReg) && !(instruction instanceof CMP) ) {
                BinaryInstructionDValToReg binary = (BinaryInstructionDValToReg) instruction;

                Operand registerOp = binary.getOperand2(); // destination (toujours un registre en IMA)
                GPRegister register = (GPRegister) registerOp;
                int registerNum = register.getNumber();
                whatRAmI.remove(registerNum);
            
            }

            //STORE R1 b
            if (instruction instanceof STORE){
                STORE storeInst = (STORE) instruction;
                Operand registerOp = storeInst.getOperand1();
                Operand valueOp = storeInst.getOperand2();
            
                GPRegister register = (GPRegister) registerOp;
                int registerNum = register.getNumber();

                DAddr variableB = (DAddr) valueOp;
                DAddr variableA = whatRAmI.get(registerNum);

                // Ok mais est ce que ce store va impliquer que b soit changé
                /*en effet b = x dans b = a -> c = b -> b = x, va casser la propagation car on a changé de réference pour b */
                // et comme b = a ici, si jamais il y avais z = b c'est cassé
                //changedRefernce(whoAmI, variableB);
                if (variableA != null){
                    // meme registre a été utilisé donc c'est propagé: 
                    whoAmI.put(variableB, variableA); 
                    // hors context : je suis en train d'entendre S.T.A.Y de Hans Zimmer si ça vous intéresse :)
                }

                
                // Il n'ya aucun registre dont t il y a un LOAD de a, donc elle n'est pas propgée
                // autrement c'est à dire qu'on ne sait pas d'où vient notre Rk 
                else{
                    whoAmI.remove(variableB);
                }
                continue;
            }



        }
}
        }
