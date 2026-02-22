package fr.ensimag.deca.optim;
import fr.ensimag.ima.pseudocode.IMAProgram;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.AbstractLine;
import fr.ensimag.ima.pseudocode.BinaryInstructionDValToReg;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;

import java.util.LinkedList;
import java.util.ListIterator;
import fr.ensimag.ima.pseudocode.Line;
import fr.ensimag.ima.pseudocode.Instruction;
import fr.ensimag.ima.pseudocode.Operand;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.instructions.WINT;
import fr.ensimag.ima.pseudocode.instructions.WFLOAT;
import fr.ensimag.ima.pseudocode.instructions.WFLOATX;


//Instructions imports
import fr.ensimag.ima.pseudocode.instructions.ADD;
import fr.ensimag.ima.pseudocode.instructions.SUB;
import fr.ensimag.ima.pseudocode.instructions.MUL;
import fr.ensimag.ima.pseudocode.instructions.DIV;
import fr.ensimag.ima.pseudocode.instructions.BOV;

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
import fr.ensimag.ima.pseudocode.UnaryInstruction;
import fr.ensimag.ima.pseudocode.instructions.LEA;



public class PeepHoleOptimizer {
    /* Méthode pour l'optimisation AddZero
        élimine les instructions d'addition avec zéro 
    */
    private boolean check;
    private boolean changed;

    public PeepHoleOptimizer(boolean check){
        // Constructeur par défaut
        this.check = check;
        this.changed = false;
    }
    /**
     * Peephole optimizer for IMA programs.
     *
     * 
     * This optimization pass performs local, pattern-based optimizations on
     * short sequences of IMA instructions (peephole optimization).
     * The goal is to simplify arithmetic expressions, remove redundant
     * instructions, and reduce unnecessary memory or register operations.
     * 
     *
     * General principle
     * 
     *   The optimizer scans the instruction list sequentially.
     *   Small instruction windows are analyzed to detect simplifiable patterns.
     *   Optimizations are applied iteratively until a fixed point is reached
     *       (no further changes).
     * 
     *
     * Implemented optimizations
     * 
     *   Arithmetic simplifications:
     *     
     *       {@code ADD #0 Ri}, {@code SUB #0 Ri} removal
     *       {@code MUL #1 Ri} and {@code DIV #1 Ri} removal
     *       {@code MUL #0 Ri} replaced by {@code LOAD #0 Ri}
     *     
     *   
     *   Redundant LOAD elimination:
     *     
     *       {@code LOAD X Ri ; LOAD X Ri}
     *       {@code LOAD Ri Ri}
     *       Propagation of values across registers when safe
     *     
     *   
     *   LOAD–ADD simplification:
     *     
     *       {@code LOAD #0 Ri ; ADD X Ri} replaced by {@code LOAD X Ri}
     *     
     *   
     *   Redundant STORE elimination:
     *     
     *       {@code STORE R1 a ; STORE R2 a} → first STORE removed
     *     
     *  
     *   Removal of useless {@code BOV} instructions after constant operations
     *       (when runtime checks are enabled). 
     * 
     *
     * Safety rules
     * 
     *   Optimizations stop at control-flow barriers (branches, calls, returns).
     *   No transformation is applied across labels.
     *   Register liveness is conservatively checked before removing or merging
     *       instructions.
     * 
     *
     * Limitations
     * 
     *   This pass is purely local and does not rely on a control flow graph.
     *   No inter-block or inter-procedural analysis is performed.
     *   Optimizations are conservative to preserve semantic correctness.
     *
     *
     * This peephole optimizer improves code quality by reducing instruction count
     * and simplifying arithmetic computations while keeping the generated code safe.
     *
     */
    public void optimize(IMAProgram imaProg){
        /*Implémentation de l'optimisation Peephole & Simplification arithmétique
         Parcourir les instructions de imaProg
         Repérer les instructions simplifiables
         et les éliminer*/
        LinkedList<AbstractLine> lines = imaProg.getLines();
        /*  On ne peut pas parcourir la liste en utilisant un for-each car on doit
        modifier la liste en cours de parcours alors que un list.remove dans une boucle for-each
        provoque une ConcurrentModificationException
        */

        do {
            changed = false; // si non boucle while infinie
            ListIterator<AbstractLine> iterator = lines.listIterator();

            while(iterator.hasNext()){
                //System.out.println("Optimizing Arithmetic Instructions...");
                AbstractLine line = iterator.next();
                if (!line.isLine()){    
                    // On ne fait rien pour les InlinePortion
                    continue;
                }

                Line lineL = (Line) line; // Cast AbstractLine to Line
                Instruction instruction = lineL.getInstruction();

                if (instruction == null) {
                    // On ne fait rien pour les lignes sans instruction
                    continue;
                }

                // Appliquer les optimisations arithmétiques
                if (instruction instanceof ADD || instruction instanceof SUB || instruction instanceof MUL || instruction instanceof DIV){
                    arithOptimize(iterator, lineL, instruction);
                }

                // Appliquer l'optimisation LOAD #0 Ri suivie de ADD/SUB Ri Rj
                else if (instruction instanceof LOAD){
                    loadOptimize(iterator, lineL, instruction);
                }

/*                 else if (instruction instanceof STORE){
                    storeLoadOptimize(iterator, lineL, instruction);
                } Ici j'évite de appeller l'optimisation STORE directement à partir de optimize car
                 ça peut affecter des LOAD par exemple:
                    LOAD #3, Ri
                    STORE Ri, X
                    LOAD X, Rj si on fait cette optimisation on risque de casser le prog si X est utilisé après
                    */

                else if (instruction instanceof STORE) {
                    storeRedundantOptimize(iterator, lineL, instruction);
                }


                /* else if (instruction instanceof BRA){
                    jumpOptimize(iterator, lineL, instruction);
                }
 */
        } 
            }while(changed);

    }

    private void arithOptimize(ListIterator<AbstractLine> iterator, Line lineL, Instruction instruction){
        if (instruction instanceof ADD || instruction instanceof SUB){
            addSubCase(iterator, lineL, instruction);
        }

        else if (instruction instanceof MUL){
            mulCase(iterator, lineL, instruction);
        }
        
        else if (instruction instanceof DIV){
            divCase(iterator, lineL, instruction);
        }
        
    }


    private boolean loadRiRiOptimize(ListIterator<AbstractLine> iterator, Line lineL, Instruction instruction, Operand valueOp, GPRegister register){
        if (valueOp instanceof GPRegister){
            GPRegister registerValue = (GPRegister) valueOp;
            if (register.getNumber() == registerValue.getNumber()){
                iterator.remove();
                changed = true;
                return true;
            }

        }
        return false;
    }

    private boolean loadRiRjOptimize( ListIterator<AbstractLine> iterator, Line lineL, Line nextLineL, Operand valueOp, GPRegister register, int consumedInst) {

        Instruction nextInstruction = nextLineL.getInstruction();

        /* Il s'agit de remplacer LOAD X, Ri -> LOAD Ri, Rj par LOAD X, Rj
        Je le met après le store pour optimiser s'il ya aussi le cas
        LOAD X, Ri -> .... -> LOAD Ri, Rj */
        if (!(nextInstruction instanceof LOAD)) {
            return false;
        }

        LOAD nextLoad = (LOAD) nextInstruction;

        Operand nextValueOp = nextLoad.getOperand1();
        GPRegister nextRegister = (GPRegister) nextLoad.getOperand2();

        if (!(nextValueOp instanceof GPRegister)) {
            /* for (int i = 0; i < consumedInst; i++) iterator.previous(); */
            return false;
        }

        GPRegister nextSrc = (GPRegister) nextValueOp;

        if (nextSrc.getNumber() != register.getNumber()) {
            /* for (int i = 0; i < consumedInst; i++) iterator.previous(); */
            return false;
        }

        /*Vérifier qu'il n' y a pas d'instruction intérmidiare qui lit ou écrit dans Ri */
        int back = 0;
        while (iterator.hasPrevious() && back < consumedInst) {
            AbstractLine prev = iterator.previous();
            back++;

            if (prev == lineL) {
                continue;
            }
            if (!prev.isLine()) {
                continue;
            }

            Instruction inst = ((Line) prev).getInstruction();
            if (inst != null && instructionTouchesRegister(inst, register)) {
                // rollback iterator
                for (int i = 0; i < back; i++) {
                    iterator.next();
                }
                return false;
            }
        }

        // rollback complet
        for (int i = 0; i < back; i++) {
            iterator.next();
        }

        // Remplacement : LOAD X, Rj
        DVal dst = (DVal) valueOp;
        nextLineL.setInstruction(new LOAD(dst, nextRegister));

        // Suppression du LOAD initial
        while (iterator.hasPrevious()) {
            AbstractLine prev = iterator.previous();
            if (prev == lineL) {
                iterator.remove();
                break;
            }
        }

        changed = true;
        return true;
    }



    private boolean loadZeroAddOptimize(ListIterator<AbstractLine> iterator, Line lineL, Line nextLineL, Instruction nextInstruction, GPRegister register, int consumedInst) {

        /*Si on rencontre un ADD */

        ADD addIsnt = (ADD) nextInstruction;
        // On récupère le registre de destination de l'ADD
        Operand registerDestOp = addIsnt.getOperand2();
        GPRegister registerDest = (GPRegister) registerDestOp;

        // On récupère les numéros des registres pour la comparaison
        int registerNum = register.getNumber();
        int registerDestNum = registerDest.getNumber();

        if (registerNum != registerDestNum){
            //for (int i = 0; i < consumedInst; i++) iterator.previous();
            return false;
        }

        /* On récupère l'opérande 1 de l'ADD */
        Operand src = addIsnt.getOperand1();

        /* Cas ADD Rj Ri : je sais pas ce cas arrive : expérimentalement ça n'a jamais arrivé après un LOAD STORE*/
        /*if ((src instanceof GPRegister) && (registerNum == registerDestNum)){
        // On a LOAD #0 Ri suivi de ADD Rj Ri
        // On peut factoriser les deux instructions
        GPRegister registerSrc = (GPRegister) src;
        nextLineL.setInstruction(new LOAD(registerSrc, registerDest));

        // Revenir vers le LOAD pour le supprimer
        iterator.previous();  // ADD
        iterator.previous(); // LOAD
        iterator.remove(); // Supprimer l'instruction LOAD #0 Ri

        iterator.next(); // Revenir à l'instruction LOAD
        removeBOV(iterator); // Supprimer le BOV

    } */

        // soit un registre soit un immédiat ou une adresse : en gros un DVal
        DVal srcD = (DVal) src;
        nextLineL.setInstruction(new LOAD(srcD, registerDest));

        changed = true;
        removeBOV(iterator); // supprimer le BOV inutile s'il y en a

        // maintenant que ADD est remplacé on supprime le LOAD
        while(iterator.hasPrevious()){
            AbstractLine previousLine = iterator.previous();
            if (previousLine == lineL){
                iterator.remove();
                return true;
            }
        }

        return true;
    }


    private boolean loadRedundantOptimize(ListIterator<AbstractLine> iterator, Line lineL, Line nextLineL, Operand valueOp, GPRegister register,  int consumedInst) {

        Instruction nextInstruction = nextLineL.getInstruction();


        LOAD nextLoad = (LOAD) nextInstruction;

        Operand nextValueOp = nextLoad.getOperand1();

        GPRegister nextRegister = (GPRegister) nextLoad.getOperand2();
        if ((nextValueOp instanceof GPRegister)) {
            /* for (int i = 0; i < consumedInst; i++) iterator.previous(); */
            return false;
        }

        if (valueOp instanceof GPRegister){
            return false;
        }

        if (!nextValueOp.equals(valueOp)){
            return false;
        }
        // il faut qu'on ait le meme registre LOAD X, Ri ... LOAD X, Ri
        if (nextRegister.getNumber() != register.getNumber()) {
            /* for (int i = 0; i < consumedInst; i++) iterator.previous(); */
            return false;
        }

        /*Vérifier qu'il n' y a pas d'instruction intérmidiare qui lit ou écrit dans Ri */
        int back = 0;
        while (iterator.hasPrevious() && back < consumedInst) {
            AbstractLine prev = iterator.previous();
            back++;

            if (prev == lineL) {
                continue;
            }
            if (!prev.isLine()) {
                continue;
            }

            Instruction inst = ((Line) prev).getInstruction();
            if (inst != null && instructionTouchesRegister(inst, register)) {
                // rollback iterator
                for (int i = 0; i < back; i++) {
                    iterator.next();
                }
                return false;
            }
        }

        boolean removed = false;
        // Suppression du LOAD initial
        while (iterator.hasPrevious()) {
            AbstractLine prev = iterator.previous();
            if (prev == lineL) {
                removed = true;
                iterator.remove();
                break;
            }
        }

        if (!removed){
            return false;
        }


        changed = true;
        return true;
    }




    private void loadOptimize(ListIterator<AbstractLine> iterator, Line lineL, Instruction instruction){
        LOAD loadInst = (LOAD) instruction;
        Operand valueOp = loadInst.getOperand1();

        Operand registerOp = loadInst.getOperand2(); 
        GPRegister register = (GPRegister) registerOp;
        
        if (loadRiRiOptimize(iterator, lineL, instruction, valueOp, register)){
            return;
        }

        int consumedInst = 0;

        while (iterator.hasNext()){
            AbstractLine nextLine = iterator.next();
            consumedInst++;

            if (!nextLine.isLine()){
                // On ne fait rien pour les InlinePortion
                continue;
            }

            Line nextLineL = (Line) nextLine;
            Instruction nextInstruction = nextLineL.getInstruction();

            if (nextLineL.getLabel() != null){
                for (int i = 0; i < consumedInst; i++) iterator.previous();
                return;
            }
            if (nextInstruction == null){
                // On ne fait rien pour les lignes sans instruction
                continue;
            }
            // Cas LOAD redondant : LOAD x, Ri ; LOAD x, Ri
            if (nextInstruction instanceof LOAD) {
                LOAD nextLoad = (LOAD) nextInstruction;

                if (valueOp.equals(nextLoad.getOperand1())
                    && register.getNumber() ==
                    ((GPRegister) nextLoad.getOperand2()).getNumber()) {

                    // supprimer le second LOAD
                    iterator.remove();
                    changed = true;
                    return;
                }
            }


            // Barrière de flot de contrôle
            if (instructionIsControlOrBranche(nextInstruction)) {
                for (int i = 0; i < consumedInst; i++) iterator.previous();
                return;
            }

            /* Si on rencontre un Store on le néglige, meme s'il lit à partir de Ri */
            if (nextInstruction instanceof STORE){
                STORE storeInst = (STORE) nextInstruction;
                Operand regStoreOp = storeInst.getOperand1();

                // Si STORE lit Ri, alors LOAD est UTILISÉ => on ne peut pas l’optimiser au delà.
                if (regStoreOp instanceof GPRegister && ((GPRegister) regStoreOp).getNumber() == register.getNumber()) {
                    for (int i = 0; i < consumedInst; i++) iterator.previous();
                    return;
                }

                // Sinon, STORE ne consomme pas Ri : on peut continuer à chercher
                storeLoadOptimize(iterator, nextLineL, nextInstruction);
                continue;
            }

            /* Il s'agit de remplacer LOAD X, Ri -> LOAD Ri, Rj par LOAD X, Rj
            Je le met après le store pour optimiser s'il ya aussi le cas
            LOAD X, Ri -> STORE -> LOAD Ri, Rj */
            if (nextInstruction instanceof LOAD){


                if (loadRedundantOptimize(iterator, lineL, nextLineL, valueOp, register, consumedInst)){
                    return;
                }
                // loadRiRjOptimize contient pas mal de vérif + que loadRedundant donc je la plasse après (vérif de Redundant incluses dans vérifs de loadRedundant)
                if (loadRiRjOptimize(iterator, lineL, nextLineL, valueOp, register, consumedInst)){
                    return;
                }
            }

            if (isImmediateZero(valueOp) && nextInstruction instanceof ADD){
                if (loadZeroAddOptimize(iterator, lineL, nextLineL, nextInstruction, register, consumedInst)){
                    return;
                }
            }

            // Premier truc non-ignorable rencontré: soit c'est l'ADD qu'on veut, soit on abandonne.
            for (int i = 0; i < consumedInst; i++) iterator.previous();
            return;
        }

        // si on sort car il reste plus de next il faut reculer
        for (int i = 0; i < consumedInst; i++) iterator.previous();
    }


    private void storeRedundantOptimize(ListIterator<AbstractLine> iterator, Line lineL, Instruction instruction) {

        if (!iterator.hasNext()){
            return;
        }

        AbstractLine nextLine = iterator.next();

        if (!nextLine.isLine()){
            iterator.previous();
            return;
        }

        Line nextLineL = (Line) nextLine;

        STORE storeInst = (STORE) instruction;
        DAddr addrStore = (DAddr) storeInst.getOperand2();


        


        Instruction nextInstruction = nextLineL.getInstruction();

        if (!(nextInstruction instanceof STORE)) {
            iterator.previous();
            return;
        }

        STORE nextStore = (STORE) nextInstruction;
        DAddr addrNext = (DAddr) nextStore.getOperand2();

        if (!addrStore.equals(addrNext)) {
            iterator.previous();
            return ;
        }

        /* Cas STORE R1, a
                STORE R2, a
        => le premier STORE est inutile */

        // on est positionné après le second STORE
        iterator.previous(); // revenir sur le second
        iterator.previous(); // revenir sur le premier
        iterator.remove();   // supprimer le premier
        iterator.next();     // revenir après le second


        changed = true;
    }

        

    private void storeLoadOptimize(ListIterator<AbstractLine> iterator, Line lineL, Instruction instruction){
        STORE storeInst = (STORE) instruction;
        if (iterator.hasNext()){
            AbstractLine nextLine = iterator.next();
            if (!nextLine.isLine()){
                // On ne fait rien pour les InlinePortion
                iterator.previous();
                return;
            }
            

            Line nextLineL = (Line) nextLine;
            Instruction nextInstruction = nextLineL.getInstruction();
            DAddr addrStore = (DAddr) storeInst.getOperand2();


            if (!(nextInstruction instanceof LOAD)){
                // On ne fait rien s'il s'agit pas de LOAD
                iterator.previous();
                return;
            } 

            LOAD loadInst = (LOAD) nextInstruction;
            // Registre du load
            Operand registerOp = loadInst.getOperand2(); 
            GPRegister registerLoad = (GPRegister) registerOp;
            // Registre du Store
            Operand registerOpStore = storeInst.getOperand1();
            GPRegister registerStore = (GPRegister) registerOpStore;


            if (!(loadInst.getOperand1() instanceof DAddr)){
                // On ne fait rien s'il s'agit pas d'une adresse
                iterator.previous();
                return;
            }

            DAddr addrLoad = (DAddr) loadInst.getOperand1();

            // Vérifier si il s'agit du meme registre: si c'est le cas ces 2 instructions sont inutiles
            if (registerLoad.getNumber() == registerStore.getNumber() && (addrLoad.equals(addrStore))){ 
                iterator.remove();
                iterator.previous();
                iterator.remove();
                iterator.next();
                changed = true;
                return;
            }
            
            iterator.previous();
        }}


    ////////////////////////////////////////////////// Méthodes utilitaires ///////////////////////////////////////////////
    private boolean isImmediateZero(Operand op){
        return(op instanceof ImmediateInteger && ((ImmediateInteger)op).getValue() == 0);
    }

    private boolean isImmediateOne(Operand op){
        return(op instanceof ImmediateInteger && ((ImmediateInteger)op).getValue() == 1);
    }


    private boolean instructionTouchesRegister(Instruction instruction, GPRegister reg) {
        
        /* Instructions binaires */
        if (instruction instanceof BinaryInstructionDValToReg) {
            BinaryInstructionDValToReg bin = (BinaryInstructionDValToReg) instruction;

            Operand valueOp = bin.getOperand1();
            Operand registerOp = bin.getOperand2(); // toujours un registre
            GPRegister register = (GPRegister) registerOp;

            int registerNum = register.getNumber();


            if (registerNum == reg.getNumber()) {
                return true; // écriture dans Ri
            }

            if (valueOp instanceof GPRegister) {
                GPRegister registerVal = (GPRegister) valueOp;
                if (registerVal.getNumber() == reg.getNumber()) {
                    return true; // lecture de Ri
            }
            }
        }
        /* instructionructions binaires */
        if (instruction instanceof UnaryInstruction) {
            UnaryInstruction bin = (UnaryInstruction) instruction;

            Operand valueOp = bin.getOperand();
            if (valueOp instanceof GPRegister){
                GPRegister register = (GPRegister) valueOp;
                int registerNum = register.getNumber();


                if (registerNum == reg.getNumber()) {
                    return true; // écriture dans Ri
                }

            }
        }

        // LOAD est aussi un BinaryinstructionructionDValToReg

        // STORE 
        if (instruction instanceof STORE) {
            STORE store = (STORE) instruction;
            Operand registerOp = store.getOperand1();
            GPRegister register = (GPRegister) registerOp;
            int registerNum = register.getNumber();

            if (register instanceof GPRegister && registerNum == reg.getNumber()){
                return true; // lecture Ri
            }

            Operand valueOp = store.getOperand2();
            if (valueOp instanceof RegisterOffset){
                RegisterOffset ro = (RegisterOffset) valueOp;
                GPRegister roRegister = (GPRegister) ro.getRegister();
                if (roRegister.getNumber() == reg.getNumber()){
                    return true;
                }
            }
        }

        // Instructions type WINT, WFLOAT, 
        /* Instructions nullaires : WINT, WFLOAT, WFLOATX */
        if (instruction instanceof WINT || instruction instanceof WFLOAT || instruction instanceof WFLOATX) {

            /* Convention IMA : ces instructions lisent implicitement R1 */
            if (reg.getNumber() == 1) {
                return true; // lecture implicite de R1, juste pour etre extra safe 
            }
        }

        if (instruction instanceof LEA) {
            LEA lea = (LEA) instruction;

            Operand op1 = lea.getOperand1(); // c'est une adresse
            Operand op2 = lea.getOperand2(); // c'est un GPRegistre

            GPRegister registerOp = (GPRegister) op2;

            if (registerOp.getNumber() == reg.getNumber()){
                return true;
            }

            if (op1 instanceof RegisterOffset){
                RegisterOffset ro = (RegisterOffset) op1;
                GPRegister roRegister = (GPRegister) ro.getRegister();
                if (roRegister.getNumber() == reg.getNumber()){
                    return true;
                }
            }
        }
        

        return false;
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


    /////////////////////////////////////////////// Cas de ArithOptimize  ///////////////////////////////////////////////
    private void addSubCase(ListIterator<AbstractLine> iterator, Line lineL, Instruction instruction){
        // Remplacer l'instruction d'addition par une instruction de copie
        BinaryInstructionDValToReg addIsnt = (BinaryInstructionDValToReg) instruction;
        Operand operand1 = addIsnt.getOperand1();

        if (isImmediateZero(operand1)){
            //System.out.println("Found ADD(SUB) #0 Ri, removing it.");
            iterator.remove();
            changed = true; // Supprimer l'instruction ADD(SUB) #0 Ri
            // Après avoir supprimé ADD(SUB) #0 Ri, on peut vérifier si l'instruction suivante est une BOV inutile
            removeBOV(iterator);
        }
    }


    private void mulCase(ListIterator<AbstractLine> iterator, Line lineL, Instruction instruction){
        // Remplacer l'instruction de multiplication par une instruction de copie
        MUL mulInst = (MUL) instruction;
        Operand operand1 = mulInst.getOperand1();

        if (operand1 instanceof ImmediateInteger ){
            int value = ((ImmediateInteger)operand1).getValue();
            if (value == 1){
                iterator.remove(); // Supprimer l'instruction MUL #1 Ri
                // Après avoir supprimé MUL #1 Ri, on peut vérifier si l'instruction suivante est une BOV inutile
                changed = true;
                removeBOV(iterator);
            }
            else if (value == 0){
                // Remplacer MUL #0 Ri par LOAD #0 Ri ceci va permettre de detecter les peephole de type Load #0 R1 -> ADD R2 R1
                Operand registerOp = mulInst.getOperand2();
                GPRegister register = (GPRegister) registerOp;

                Instruction loadZero = new LOAD(new ImmediateInteger(0), register);
                lineL.setInstruction(loadZero); // Remplacer l'instruction MUL #0 Ri par LOAD #0 Ri
                changed = true;
                // Après avoir remplacé par LOAD #0 Ri, on peut vérifier si l'instruction suivante est une BOV inutile
                removeBOV(iterator);
            }
        }
    }

    private void divCase(ListIterator<AbstractLine> iterator, Line lineL, Instruction instruction){
        // Remplacer l'instruction de division par une instruction de copie
        Operand operand1 = ((DIV)instruction).getOperand1();
        
        if (isImmediateOne(operand1)){
            iterator.remove(); // Supprimer l'instruction DIV #1 Ri
            // Après avoir supprimé DIV #1 Ri, on peut vérifier si l'instruction suivante est une BOV inutile
            changed = true;
            removeBOV(iterator);
    }
    }



    ///////////////////////////////////////// Suppression des BOV inutiles après LOAD #0 Ri /////////////////////////////////
    private void removeBOV(ListIterator<AbstractLine> iterator){
        if (!this.check){
            return; // Si les vérifications sont désactivées, ne rien faire
        }
        if (iterator.hasNext()){
            AbstractLine nextLine = iterator.next();
            if (nextLine.isLine()){
                Line nextLineL = (Line) nextLine;
                Instruction nextInstruction = nextLineL.getInstruction();
                // Vérifier si l'instruction suivante est une BOV
                if (nextInstruction instanceof BOV){
                    iterator.remove(); // Supprimer l'instruction BOV inutile après LOAD #0 Ri
                    changed = true;
                }
            }

            iterator.previous(); // Revenir à la position initiale
        }
    }

    
}
    