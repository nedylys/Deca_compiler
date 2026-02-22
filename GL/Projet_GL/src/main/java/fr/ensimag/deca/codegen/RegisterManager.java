package fr.ensimag.deca.codegen;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.TreeSet;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.GPRegister;


/**
 * Gestionnaire des registres lors de la génération de code
 * 
 * Cette classe est responsable de : 
 * 
 * - Allocation et la libération des registres {@code R2} à {@code RMax}.
 * 
 * - Spill de registres sur la pile lorsque tous les registres sont occupés.
 * 
 * - Distinction de comportement entre le code hors méthode et le code généré
 * à l'intérieur d'une méthode.
 * 
 * Conventions : 
 * 
 * - {@code R0} et {@code R1} sont considérés comme registres scratch
 * (écrasables, non gérés par la pile des registres libres).
 * 
 * - Les registres réellement gérés commencent à {@code R2}.
 * 
 * - Les registres sont alloués selon une approche LIFO (pile).
 * 
 * La structure de données choisie est {@link Deque}, utilisée comme une pile,
 * conformément aux recommandations de l'API Java (remplacement de {@code Stack}).
 */

public class RegisterManager {

    /** Pile des registres actuellement libres.
    * Elle contient uniquement les registres {@code R2 .. Rmax} qui ne sont
    * pas utilisés à l'instant courant.
    */
    private Deque<GPRegister> freeRegisters = new ArrayDeque<>() ;

    /**
    * Index du premier registre géré par le gestionnaire.
    * {@code R0} et {@code R1} sont réservés (scratch).
    */
    private static int IDX_FIRST_FREE_REGISTER = 2 ; 
    
    /**
    * Index du dernier registre disponible ({@code R15} par défaut,
    * ou {@code R(X-1)} avec l'option {@code -r X}).
    */
    private int idxLastFreeReg ;             
    
    /**
     * Ensemble des registres effectivement utilisés à l'intérieur d'une méthode.
     * 
     * Il sert à tracer quels registres doivent être sauvegardés / restaurés
     * dans le prologue / épilogue d'une méthode.
     */
    private final TreeSet<GPRegister> registersSTtack = new TreeSet<>();

    /**
     * Indique si la génération de code se fait actuellement dans une méthode.
     */
    private boolean insideMthd = false;
                                                       

    /**
     * Constructeur du gestionnaire de registres.
     * Initialise la pile des registres libres {@code RMax ... R2}.
     * 
     * @param idxRMax index du dernier registre utilisable (doit être
     * dans l'intervalle {@code [2, 15]})
     * 
     * @throws IllegalArgumentException si {@code idxRMax} est hors bornes.
     */
    public RegisterManager(int idxRMax) {
        if (idxRMax < 2 || idxRMax > 15) {
            throw new IllegalArgumentException("idxRMax must be in [2..15]");
        }        
        this.idxLastFreeReg = idxRMax ;
        this.idxLastPushedReg = idxLastFreeReg ;
        // System.out.println(idxRMax);
        for (int i = idxLastFreeReg ; i >= IDX_FIRST_FREE_REGISTER ; i--) {
            freeRegisters.addFirst(GPRegister.getR(i)); // équivaut à push
        }
    }


    /** 
     * Indique s'il reste au moins un registre libre.
     * 
     * @return {@code true} si un registre peut être alloué sans spill
     */
    public boolean hasFreeReg() {
        return !freeRegisters.isEmpty() ;
    }

    /**
     * Alloue un registre libre si possible.
     * 
     * Le registre retourné est retiré de {@link #freeRegisters}.
     * 
     * Si la génération se fait dans une méthode, le registre est enregistré dans
     * {@link #registersSTtack} pour pouvoir être sauvegardé / restauré lors de l'appel.
     * 
     * @return un registre libre, ou {@code null} s'il n'y en a plus.
     */
    public GPRegister allocReg() {
        if (this.hasFreeReg()) {
            GPRegister r = freeRegisters.removeFirst() ; // équivaut à pop 
            int index = r.getNumber();    
            if (insideMthd && index != 1){
                registersSTtack.add(r); // On compte pas R1 c'est juste pour pouvoir
                                        // L'utiliser au sein de la méthode.
            }   
            return r;                                     
        }
        return null ;
    }

  
    /**
     * Libère un registre précédemment alloué.
     * 
     * Le registre est remis dans la pile des registres libres uniquement s'il 
     * appartient dans l'intervalle géré.
     * 
     * @param r registre à libérer (ignoré si {@code null}).
    */
    public void freeReg(GPRegister r) {
        if (r == null) {
            return;
        }
        int idxReg = r.getNumber() ;
        if (idxReg <= idxLastFreeReg && idxReg >= IDX_FIRST_FREE_REGISTER) {
            freeRegisters.addFirst(r); // équivaut à push
        } 
    }

    /** 
     * Active le mode "génération de code dans une méthode".
     * Ce mode influe sur : 
     * 
     * - Le suivi des registres utilisés.
     * 
     * - La politique de spill.
     * 
     */
    public void activateMthd(){
        insideMthd = true;
    }

    /**
     * 
     * @return ensemble des registres utilisés dans la méthode courante.
     */
    public TreeSet<GPRegister> regStack(){
        return registersSTtack;
    }

    /**
     * @return pile actuelle des registres libres.
     */
    public Deque<GPRegister> getFreeReg(){
        return freeRegisters;
    }

    /**
     * Remplace la pile des registres libres.
     * @param freeReg nouvelle pile
     */
    public void setFreeReg(Deque<GPRegister> freeReg){
        this.freeRegisters = freeReg;
    }

    /**
     * Modifie dynamiquement l'indice du premier registre géré.
     * 
     * @param k nouvel indice minimal.
     */
    public void setIndexFirstFreeReg(int k){
        IDX_FIRST_FREE_REGISTER = k;
    }

    /**
     * 
     * @return {@code true} si la génération est actuellement dans une méthode.
     */
    public boolean getInsideMthd(){
        return insideMthd;
    }


    /**
     * Réinitialise complètement l'état du gestionnaire de registres.
     * 
     * Typiquement utilisé : 
     * 
     * - Au début d'un nouveau contexte.
     * 
     * - Après génération complète d'un bloc indépendant.
     */
    public void resetfreeRegs() {
        freeRegisters.clear();
        pushedRegs.clear();
        idxLastPushedReg = idxLastFreeReg;
        for (int i = idxLastFreeReg; i >= IDX_FIRST_FREE_REGISTER; i--) {
            freeRegisters.addFirst(GPRegister.getR(i));
        }
    }
        

    public GPRegister getFirstUsedReg() {
        return GPRegister.getR(IDX_FIRST_FREE_REGISTER) ;   // R2
    }


    /** 
     * Pile des registres ayant été spillés sur la pile. 
     *
    */
    private final Deque<GPRegister> pushedRegs = new ArrayDeque<>();

    /**
     * Index du dernier registre spillé.
     */
    private int idxLastPushedReg ; 

    
    /**
     * Alloue un registre coûte que coûte.
     * 
     * Si aucun registre libre n'est disponible : 
     * 
     * - Un registre déja utilisé est sélectionné.
     * 
     * - Sa valeur est sauvegardée sur la pile (ie spill).
     * 
     * - Le registre est réutilisé.
     * @param compiler compilateur (pour accéder au {@link StackManager}).
     * @return registre utilisable
     */
    public GPRegister allocRegAnyway(DecacCompiler compiler) {
        GPRegister rRet = this.allocReg() ;
        if (rRet != null) {
            return rRet ;
        }
        // on prend le dernier registre alloué
        rRet = GPRegister.getR(idxLastPushedReg) ;
        // on le push
        //compiler.addInstruction(new PUSH(rRet));
        compiler.getStackManager().pushRegTemp(compiler, rRet);
        pushedRegs.addFirst(rRet); // push
        // on réinitialise idxLastAllocatedReg
        idxLastPushedReg -- ;
        if (idxLastPushedReg < IDX_FIRST_FREE_REGISTER) {
            idxLastPushedReg = idxLastFreeReg;
        }
        return rRet ;
    }


    /**
     * Libère un registre alloué via {@link #allocRegAnyway}.
     * 
     * Si le registre correspond à un spill récent, sa valeur est restaurée
     * depuis la pile.
     * 
     * Sinon, il est remis dans la pile des registres libres.
     * @param compiler compilateur
     * @param rToFree registre à libérer
     */
    public void freeRegAnyway(DecacCompiler compiler, GPRegister rToFree) {
        if (rToFree == null) return;
    
        if (!pushedRegs.isEmpty() && pushedRegs.peekFirst().equals(rToFree)) {
            compiler.getStackManager().popRegTemp(compiler, rToFree);
            pushedRegs.removeFirst();
        } else {
            freeReg(rToFree);
        }
    }
}


