package fr.ensimag.deca.codegen;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;

/** 
 * Gestionnaire des temporaires empilés pendant la génération de code
 * 
 * Il sert essentiellement à : 
 * 
 * - Sauvegarder des valeurs intermédiaires sur la pile via {@link PUSH} / {@link POP}
 * lorsque la pression sur les registres devient trop forte 
 * (ou lorsqu'un registre doit être protégé).
 * 
 * - Suivre le nombre courant de temporaires empilés ({@code currNbTemp}).
 * 
 * - Mémoriser le pix maximal de temporaires empilés ({@code maxNbTemp})
 * pour dimensionner la pile (utile pour l'instruction {@code TSTO}) et anticiper
 * un éventuel débordement.
 * 
 * Invariants : 
 * 
 * - {@code currNbTemp >= 0} toujours.
 * 
 * - {@code maxNbTemp >= currNbTemp} toujours.
 * 
 * - Tout {@link #popRegTemp} doit correspondre à un {@link #pushRegTemp} précédent.
 * 
 */
public class StackManager {
    /** 
     * Nombre courant de valeurs temporaires sauvegardées sur la pile.
     * 
     * Incrémente après chaque {@link #pushRegTemp} et décrémente 
     * après chaque {@link #popRegTemp}. */
    private int currNbTemp = 0 ;  

    /** 
     * Nombre maximal de temporaires simultanément présents sur la pile.
     * Ce maximum correspond au "pic" d'utilisation et sert au dimensionnement.
     */
    private int maxNbTemp = 0 ; 

    /** 
     * Empile un registre temporaire sur la pile via {@link PUSH} et met 
     * à jour les compteurs de temporaires.
     * 
     * Utilisée typiquement : 
     * 
     * - Quand tous les registres sont occupés.
     * 
     * - Pour protéger un registre autour d'un appel.
     * 
     * @param compiler compilateur (sert à ajouter l'instruction {@link PUSH})
     * @param r registre à empiler (doit être non null)
     * @throws IllegalArgumentException si {@code r == null}
     * 
     */
    public void pushRegTemp(DecacCompiler compiler, GPRegister r) {
        if (r == null) {
            throw new IllegalArgumentException("Cannot push a null register");
        }        
        compiler.addInstruction(new PUSH(r));
        currNbTemp ++ ; // on l'incrémente suite au push
        if (currNbTemp > maxNbTemp) {
            maxNbTemp = currNbTemp ;  // pour garder le moment ou le pic de la pile est atteint
        }
    }

    /** 
     * Dépile un regsitre temporaire depuis la pile via {@link POP} et met
     * à jour les compteurs de temporaires.
     * 
     * Suppose qu'un {@link #pushRegTemp} a été effectué auparavant.
     * Si ce n'est pas le cas, l'état interne serait incohérent : 
     * on alors lève une exception.
     * 
     * @param compiler compileur (sert à ajouter l'instrcution {@link POP})
     * @param r registre recevant la valeur dépilée (doit être non null)
     * @throws IllegalStateException si {@code r == null}
     * @throws IllegalStateException si aucun temporaire n'est actuellement empilé
     */
    public void popRegTemp(DecacCompiler compiler, GPRegister r) {
        if (r == null) {
            throw new IllegalArgumentException("Cannot pop a null register");
        }        
        if (currNbTemp == 0) {
            throw new IllegalStateException("POP without a previous PUSH") ;
        }

        compiler.addInstruction(new POP(r));

        currNbTemp -- ; // décrémentation suite au pop
    }

    /** 
     * Renvoie le pic maximalde temporaires empilés.
     * 
     * Cette valeur est surtout utilisée pour paramétrer {@code TSTO}.
     * 
     * @return nombre maximal de temporaires simultanément empilés.
     */
    public int getMaxNbTemp() {
        return maxNbTemp ;
    }

    /** 
     * Renvoie le nombre courant de temporaires empilés.
     * 
     * @return nombre courant de temporaires présents sur la pile.
     */
    public int getCurrNbTemp() {
        return currNbTemp ;
    }

    /**
     * Ajuste le compteur courant de temporaires de {@code k} unités et 
     * met à jour le maximum.
     * 
     * NB : l'appelant doit garantir que le résultat reste cohérent
     * (notamment {@code currNbTemp + k >= 0}). 
     * 
     * Cette méthode ne fait pas de vérification stricte afin de rester légère.
     * @param k variation à appliquer au nombre courant de temporaires 
     * (peut être positive ou négative)
     */
    public void addToCurrNbTemp(int k){
        currNbTemp += k;
        if (currNbTemp > maxNbTemp) {
            maxNbTemp = currNbTemp ;  // pour garder le moment ou le pic de la pile est atteint
        }
    }
}  

