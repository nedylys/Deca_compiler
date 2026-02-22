package fr.ensimag.deca.codegen;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tree.AbstractExpr;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.ImmediateFloat;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.BGE;
import fr.ensimag.ima.pseudocode.instructions.BGT;
import fr.ensimag.ima.pseudocode.instructions.BLE;
import fr.ensimag.ima.pseudocode.instructions.BLT;
import fr.ensimag.ima.pseudocode.instructions.BNE;
import fr.ensimag.ima.pseudocode.instructions.CMP;


/** 
 * Helper de génération de code pour les comparaisons booléennes.
 * 
 * Cette classe factorise le schéma général de génération de code
 * des expressions de comparaison.
 * 
 * Elle est utilisée par toutes les classes représentant des opérateurs
 * de compraisons ({@code ==, !=, <, <=, >, >=}).
 * 
 * Principe : 
 * 
 * - Les opérandes sont évaluées conformément à la convention : de gauche à droite
 * 
 * - Le résultat de la comparaison n'est pas métérialisé dans un registre.
 * 
 * - La comparaison se termine par un branchement conditionnel.
 */
public class CompareBoolHelper {
    
    /** 
     * Opérateurs de comparaison *
    */
    public enum CmpOp {
        /** égalité == */
        EQ,
        /** non égalité != */
        NE, 
        /** strictement inférieur < */
        LT, 
        /** inférieur ou égal <= */
        LE, 
        /** strictement supérieur > */
        GT, 
        /** supérieur ou égal >= */
        GE
    }

    /**
     * Génère le code correspondant à une comparaison suivie 
     * d'un branchement conditionnel.
     * 
     * Schéma général : 
     * 
     * - Evaluation de l'opérande gauche dans un registre {@code rL}.
     * 
     * - Comparaison avec l'opérande droite via {@link CMP}.
     * 
     * - Branchement conditionnel vers l'étiquette {@code E}.
     * 
     * Le paramètre {@code b} permet d'indiquer si le branchement doit 
     * être effectué lorsque la comparaison est vraie ({@code b == true}) 
     * ou lorsqu'elle est fausse ({@code b == false}). 
     * Cela permet d'éviter des inversions de conditions inutiles
     * dans le code généré.
     * 
     * Délègue, en plus, à une variante spécifique lorsque la génération
     * se fait à l'intérieur d'une méthode, afin de respecter les conventions
     * de gestion des registres et du spill.
     * 
     * @param compiler compilateur 
     * @param leftOp opérande gauche de la comparaison.
     * @param rightOp opérande droite de la comparaison.
     * @param b booléan indiquant le sens du branchement : 
     *          - si {@code true} alors branchement si la comparaison est vraie
     *          - si {@code false} alors branchement si la comparaison est fausse
     * @param E étiquettecible du branchement.
     * @param op opérateur de ocmparaison
     */

    public static void genCodeCmpOp(DecacCompiler compiler, AbstractExpr leftOp,
                                    AbstractExpr rightOp, boolean b, Label E, CmpOp op) {

        
        
        
        if (compiler.getRegManager().getInsideMthd()){
            genCodeCmpOpMthd(compiler, leftOp, rightOp, b, E, op);
            return;
        }
        
        // on évalue là l'op gche dans rL
        GPRegister rL = compiler.getRegManager().allocRegAnyway(compiler) ;
        leftOp.codeGenExpr(compiler, rL);

        // si on compare avec un immédiat
        DVal dR = BinaryArithOpHelper.getEventualDVal(rightOp, compiler) ;

        if (dR instanceof ImmediateInteger || dR instanceof ImmediateFloat) {
            compiler.addInstruction(new CMP(dR, rL));
            executeBranch(compiler, b, E, op);
            compiler.getRegManager().freeRegAnyway(compiler, rL);
            return;
        }

        // sinon, on évalue rightOp dans un registre
        GPRegister rR = compiler.getRegManager().allocRegAnyway(compiler) ;
        rightOp.codeGenExpr(compiler, rR);
        compiler.addInstruction(new CMP(rR, rL));
        executeBranch(compiler, b, E, op);
        compiler.getRegManager().freeRegAnyway(compiler, rL);
        compiler.getRegManager().freeRegAnyway(compiler, rR);
    }

    /**
     * Variante de {@link #genCodeCmpOp} utilisée lorsque la génération de code
     * concerne l'intérieur d'une méthode.
     * 
     * @param compiler compilateur
     * @param leftOp opérande gauche
     * @param rightOp opérande droite 
     * @param b sens du branchement
     * @param E étiquette cible du branchement
     * @param op opérateur de comparaison
     */
    public static void genCodeCmpOpMthd(DecacCompiler compiler, AbstractExpr leftOp,
                                    AbstractExpr rightOp, boolean b, Label E, CmpOp op) {
        
        // on évalue là l'op gche dans rL
        GPRegister rL = compiler.getRegManager().allocRegAnyway(compiler) ;
        leftOp.codeGenExpr(compiler, rL);

        // si on compare avec un immédiat
        DVal dR = BinaryArithOpHelper.getEventualDVal(rightOp, compiler) ;

        if (dR instanceof ImmediateInteger || dR instanceof ImmediateFloat) {
            compiler.addInstruction(new CMP(dR, rL));
            executeBranch(compiler, b, E, op);
            compiler.getRegManager().freeRegAnyway(compiler, rL);
            return;
        }

        // sinon, on évalue rightOp dans un registre
        GPRegister rR = compiler.getRegManager().allocRegAnyway(compiler) ;
        rightOp.codeGenExpr(compiler, rR);
        compiler.addInstruction(new CMP(rR, rL));
        executeBranch(compiler, b, E, op);
        compiler.getRegManager().freeRegAnyway(compiler, rL);
        compiler.getRegManager().freeRegAnyway(compiler, rR);
    }


    /** 
     * Emet l'instruction de branchement conditionnel correspondant à l'opérateur
     * de comparaison et au sens voulu.
     * 
     * L'instruction assembleur est conditionnée par {@code op} et {@code b}
     * 
     * @param compiler compilateur
     * @param b sens du branchement
     * @param E étiquette cible
     * @param op opérateur de comparaison
     */
    public static void executeBranch(DecacCompiler compiler, boolean b, Label E, CmpOp op) {
        switch (op) {
            case EQ :
                compiler.addInstruction(b ? new BEQ(E) : new BNE(E));
                break;
            case NE :
                compiler.addInstruction(b ? new BNE(E) : new BEQ(E));
                break ;
            case LT :
                compiler.addInstruction(b ? new BLT(E) : new BGE(E));
                break ;
            case LE :
                compiler.addInstruction(b ? new BLE(E) : new BGT(E));
                break ;
            case GT :
                compiler.addInstruction(b ? new BGT(E): new BLE(E));
                break ;
            case GE :
                compiler.addInstruction(b ? new BGE(E) : new BLT(E));
                break ;
        }
    }
}
