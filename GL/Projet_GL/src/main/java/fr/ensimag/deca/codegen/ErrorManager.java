package fr.ensimag.deca.codegen;

import java.util.Set;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.ImmediateString;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.ERROR;
import fr.ensimag.ima.pseudocode.instructions.WNL;
import fr.ensimag.ima.pseudocode.instructions.WSTR ;

/**
 * Gestionnaire centralisé des erreurs d'exécution (runtime) pour l'étape
 * de génération de code.
 * 
 * Rôle :
 *  
 * - Associer à chaque erreur runtime un {@link Label} 
 * (point d'entrée du bloc erreur)
 * 
 * - Associer à chaque erreur runtime un message {@link ImmediateString}
 * 
 * - Générer le code des blocs d'erreur
 * 
 * - Ne générer que les blocs effectivement utilisés (via {@link #useAndGetLabel})
 * 
 * Interaction avec l'option {@code -n} : 
 * 
 * - si {@code -n} est activée, aucun bloc d'erreur n'est généré, ni BOV 
 * 
 * - sinon, le programme branche éventuellement vers l'étiquette d'erreur indiquée
 * 

 */
public class ErrorManager {

    /** Types d'erreurs runtime possibles */
    public enum ErrorType {
        /** Débordement de la pile */
        STACK_OVERFLOW,
        /** Débordement lors d'une opération arithmétique */
        OVERFLOW,
        /** Division / Modulo par 0 */
        DIVISION_BY_ZERO,
        /** Erreur d'entrée / sortie */
        IO_ERROR,
        /** Dépassement de tas */
        TAS_PLEIN,
        /** Déréférencement de null (accès champ / méthode sur null */
        DEREFERENCEMENT_NULL,
        /** Cast échoué car impossible */
        CAST_ERROR
    }

    /**
     * Retourne l'étiquette correspondant à un type d'erreur
     * 
     * Cette étiquette sert de cible de branchement
     * 
     * @param type le type d'erreur runtime
     * @return étiquette unique représentant un bloc d'erreur
     * @throws AssertionError si {@code type} n'est pas géré
     */
    public static Label getErrorLabel(ErrorType type) {
        switch (type) {
            case STACK_OVERFLOW :
                return new Label("stack_overflow_error") ;
            case OVERFLOW :
                return new Label("overflow_error") ;
            case DIVISION_BY_ZERO :
                return new Label("division_by_zero_error") ;
            case IO_ERROR :
                return new Label("io_error");
            case TAS_PLEIN :
                return new Label("tas_plein");
            case DEREFERENCEMENT_NULL :
                return new Label("dereferencement_null");
            case CAST_ERROR :
                return new Label("cast_error");
            default:
                // normalement jamais on tombera ici
                throw new AssertionError("Unknown error type : " + type) ;
        }
    }

    /** Retourne le message affiché lorsqu'une erreur runtime survient.
     * 
     * Le message est un {@link ImmediateString} afin de pouvoir être passé directement à
     * l'instruction {@link WSTR}
     * 
     * @param type type d'erreur runtime
     * @return message d'erreur correspondant
     * @throws AssertionError si {@code type} n'est pas géré
    */
    public static ImmediateString getErrorMessage(ErrorType type) {
        switch (type) {
            case STACK_OVERFLOW :
                return new ImmediateString("Error : Stack Overflow") ;
            case OVERFLOW :
                return new ImmediateString("Error : Overflow during arithmetic operation") ;
            case DIVISION_BY_ZERO :
                return new ImmediateString("Error : Division by zero is forbidden") ;
            case IO_ERROR :
                return new ImmediateString("Error : Input / Output Error ") ;
            case TAS_PLEIN:
                return new ImmediateString("Error : Heap OverFlow") ;
            case DEREFERENCEMENT_NULL:
                return new ImmediateString("Error : Dereferencing null") ;
            case CAST_ERROR :
                return new ImmediateString("Error : Impossible Cast");
            default:
                // n'arrive jamais normalement
                throw new AssertionError("Unknown error type : " + type);
        }
    } 
    /**
     * Génère le bloc assembleur d'une erreur runtime
     * 
     * - Pose le {@link Label} de l'erreur.
     * 
     * - Affiche le message ({@link WSTR}).
     * 
     * - Saute une ligne ({@link WNL}).
     * 
     * - Termine le programme en erreur ({@link ERROR}).
     * 
     * Cette méthode ne filtre pas selon {@code -n} : dans tous les cas. 
     * Le filtrage global est assuré par {@link #genCodeErrors}.
     * 
     * @param compiler compilateur (sert à ajouter label / instructions)
     * @param type type d'erreur à matérialiser en bloc assembleur
     */
    public static void genCodeErrorBlock(DecacCompiler compiler, ErrorType type) {
        compiler.addLabel(getErrorLabel(type));
        compiler.addInstruction(new WSTR(getErrorMessage(type)));
        compiler.addInstruction(new WNL());
        compiler.addInstruction(new ERROR());
    }


    /**
     * Déclare qu'une erreur donnée peut être déclenchée dans le programme en cours
     * et retourne l'étiquette correspondante.
     *  
     * But : 
     * 
     * - Permettre au code généré d'utiliser immédiatement la cible 
     * de branchement (label).
     * 
     * - Enregistrer l'erreur côté compilateur pour ne générer, en fin de programme,
     * que les blocs d'erreur effectivement nécessaires.
     * @param compiler compilateur (pour stocker l'ensemble des erreurs 
     * pouvant être effectivement générées)
     * @param type type d'erreur potentiellement déclenchée
     * @return l'étiquette du bloc d'erreur (cible de branchement)
     */
    public static Label useAndGetLabel(DecacCompiler compiler, ErrorType type) {
        compiler.useError(type);
        return getErrorLabel(type);
    }
    

    /**
     * Génère, en fin de programme, uniquement les blocs d'erreurs runtime
     * réellement utilisés pendant la compilation.
     * 
     * Si l'option {@code -n} est active à la compilation, aucun bloc n'est généré
     * car le code ne doit pas contenir de vérifications runtime.
     * 
     * @param compiler compilateur
     * @param types ensemble des erreurs à générer 
     * (typiquement accumulé via {@link DecacCompiler#useError}
     * 
     */
    public static void genCodeErrors(DecacCompiler compiler, Set<ErrorType> types) {
        if (compiler.isNoCheck()) {
            return;
        }
        for (ErrorType type : types) {
            genCodeErrorBlock(compiler, type);
        }
    }
}