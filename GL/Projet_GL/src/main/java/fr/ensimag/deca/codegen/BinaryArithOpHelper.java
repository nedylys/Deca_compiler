package fr.ensimag.deca.codegen;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.FieldDefinition;
import fr.ensimag.deca.tree.AbstractExpr;
import fr.ensimag.deca.tree.FloatLiteral;
import fr.ensimag.deca.tree.Identifier;
import fr.ensimag.deca.tree.IntLiteral;
import fr.ensimag.deca.tree.MethodCall;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.ImmediateFloat;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.instructions.ADD;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.DIV;
import fr.ensimag.ima.pseudocode.instructions.MUL;
import fr.ensimag.ima.pseudocode.instructions.QUO ;
import fr.ensimag.ima.pseudocode.instructions.REM;
import fr.ensimag.ima.pseudocode.instructions.SUB;

/** Cette classe implémente un helper pour factoriser
 * le pattern de génération de code pour les opérations 
 * arithmétiques binaires.
 * 
 * On impose pour simplifier la convention suivante :
 * l'opérande de gauche est évaluée dans {@code targetReg} et le 
 * résultat y est produit aussi. 
 * 
 * Optimisation : si l'opérande droite peut être représentée comme un {@link DVal}
 * (immédiat ou adresse), on génère directement {@code OP dvR, targetReg} sans allouer
 * de registre.
 * 
 */
public class BinaryArithOpHelper {

    /** Opérateurs arithmétiques binaires
     * 
     * {@link #ADD} : addition entière / flottante selon le typage validé à l'étape B 
     * {@link #SUB} : soustraction
     * {@link #MUL} : multiplication
     * {@link #DIV_INT} : division entière (via instruction {@code QUO})
     * {@link #DIV_FLOAT} : division flottante (via instruction {@code DIV})
     * {@link #MOD} : modulo (via instruction {@code REM})
     * 
     */
    public enum ArithOp {
        ADD, SUB, MUL, DIV_INT, DIV_FLOAT, MOD
    }

    /** Génère le code d'une opération arithmétique binaire en suivant la démarche suivante :
     * 
     * - évaluer l'opérande gauche dans {@code targetReg}
     * 
     * - si l'opérande droite est un {@link DVal} (ie immédiat/adresse), 
     * l'utiliser directement
     * 
     * - sinon, évaluer l'opérande droite dans un registre temporaire alloué, puis 
     * appliquer l'instruction
     * 
     * @param compiler compilateur : comprend l'état global de génération, options, managers
     * @param leftOp leftOp opérande gauche (son évaluation doit 
     * déposer sa valeur dans {@code targetReg})
     * @param rightOp opérande droite
     * @param targetReg registre cible : contient l'opérande gauche puis le résultat final
     * @param op opérateur arithmétique à appliquer
     * 
     */
    public static void genCodeBinaryArithOp(DecacCompiler compiler, AbstractExpr leftOp,
                    AbstractExpr rightOp, GPRegister targetReg, ArithOp op) {
        
        // si on est dans le corps d'une méthode, on utilise une variante adaptée
        if (compiler.getRegManager().getInsideMthd()){
            genMthdCodeBinaryArithOp(compiler, leftOp, rightOp, targetReg, op);
            return;
        }
        // eval de opG dans targetReg en premier
        leftOp.codeGenExpr(compiler, targetReg) ;

        // si opérande droite est un DVal, on coutourne sans passer par un registre
        DVal dvR = getEventualDVal(rightOp,compiler) ;
        if (dvR != null) {
            addArithOpInstruction(compiler, op, dvR, targetReg);
            return;
        }

        // sinon, eval de opD
        GPRegister rR = compiler.getRegManager().allocRegAnyway(compiler) ;
        rightOp.codeGenExpr(compiler, rR);

        // génération de code de l'opération
        addArithOpInstruction(compiler, op, rR, targetReg);

        compiler.getRegManager().freeRegAnyway(compiler, rR);
    }


    /** Variante de {@link #genCodeBinaryArithOp} utilisée lors de la génération
     * de code à l'intérieur d'une méthode.
     * 
     * Différence avec son homologue : si {@code rightOp} est un {@link MethodCall}, 
     * l'appel peut écraser certains registres temporaires (scratch). 
     * On sauvegarde donc {@code targetReg} sur la pile le temps d'évaluer 
     * l'opérande droite, puis on restaure.
     * 
     * @param compiler compilateur
     * @param leftOp opérande gauche évaluée dans {@code targetReg}
     * @param rightOp opérande droite
     * @param targetReg registre cible contenant opG puis le résultat
     * @param op opérateur arithmétique
     */
    public static void genMthdCodeBinaryArithOp(DecacCompiler compiler, AbstractExpr leftOp,
                    AbstractExpr rightOp, GPRegister targetReg, ArithOp op) {
    
        // eval de opG dans targetReg 
        leftOp.codeGenExpr(compiler, targetReg) ;

        // utilisation éventuelle d"un DVal 
        DVal dvR = getEventualDVal(rightOp,compiler) ;
        if (dvR != null) {
            addArithOpInstruction(compiler, op, dvR, targetReg);
            return;
        }

        // cas paritculier : appel de méthode à droite => protection de targetReg 
        // en le sauvegardant dans la pile
        if (rightOp instanceof MethodCall) {
            // registre scratch choisi différent de targetReg
            GPRegister rTmpRight = (targetReg.getNumber() == 0) ? GPRegister.R1 : GPRegister.R0;
        
            // sauvegarde de targetReg
            compiler.getStackManager().pushRegTemp(compiler, targetReg);
        
            // éval de opD via appel méthode
            rightOp.codeGenExpr(compiler, rTmpRight);
        
            // restauration de targetReg
            compiler.getStackManager().popRegTemp(compiler, targetReg);
        
            // application de l'operation
            addArithOpInstruction(compiler, op, rTmpRight, targetReg);
            return;
        }

        // cas général...
        GPRegister rR = compiler.getRegManager().allocRegAnyway(compiler) ;
        rightOp.codeGenExpr(compiler, rR);
        addArithOpInstruction(compiler, op, rR, targetReg);
        compiler.getRegManager().freeRegAnyway(compiler, rR);

    }





    /** Ajoute l'instruction correspondant l'opération arithmétique {@code op}
     * sur {@code targetReg} (qui contient l'opérande gauche) avec 
     * {@code rR} comme opérande droite.
     * 
     * Insère aussi, lorsque l'option {@code -n} (no-check) n'est pas active,
     * des branches vers les routines d'erreur pour :
     * 
     * - Débordement arithmétisue : BOV overflow
     * - Division / Modulo par 0 : BOV division_by_zero_error
     * 
     * @param compiler compilateur pour émettre les instructions
     * @param op opérateur arithmétique
     * @param rR opérande droite (registre, immédiat ou adresse)
     * @param targetReg registre cible (contient opG + résultat ensuite)
     */
    private static void addArithOpInstruction(DecacCompiler compiler, ArithOp op, 
                                        DVal rR, GPRegister targetReg) {
        switch (op) {
            case ADD :
                compiler.addInstruction(new ADD(rR, targetReg));
                if (!compiler.isNoCheck()) {
                    compiler.addInstruction(new BOV(ErrorManager.useAndGetLabel(compiler, ErrorManager.ErrorType.OVERFLOW)));
                }
                break;
            case SUB :
                compiler.addInstruction(new SUB(rR, targetReg));
                if (!compiler.isNoCheck()) {
                    compiler.addInstruction(new BOV(ErrorManager.useAndGetLabel(compiler, ErrorManager.ErrorType.OVERFLOW)));
                }
                break ;
            case MUL :
                compiler.addInstruction(new MUL(rR, targetReg));
                if (!compiler.isNoCheck()) {
                    compiler.addInstruction(new BOV(ErrorManager.useAndGetLabel(compiler, ErrorManager.ErrorType.OVERFLOW)));
                }
                break ;
            case DIV_INT :
                compiler.addInstruction(new QUO(rR, targetReg));
                if (!compiler.isNoCheck()) {
                    //compiler.addInstruction(new CMP(0, rR));
                    compiler.addInstruction(new BOV(ErrorManager.useAndGetLabel(compiler, ErrorManager.ErrorType.DIVISION_BY_ZERO)));
                }
                break ;
            case DIV_FLOAT :
                compiler.addInstruction(new DIV(rR, targetReg));
                if (!compiler.isNoCheck()) {
                    compiler.addInstruction(new BOV(ErrorManager.useAndGetLabel(compiler, ErrorManager.ErrorType.OVERFLOW)));
                }
                break ;
            case MOD :
                compiler.addInstruction(new REM(rR, targetReg));
                if (!compiler.isNoCheck()) {
                    // compiler.addInstruction(new CMP(0, rR));
                    compiler.addInstruction(new BOV(ErrorManager.useAndGetLabel(compiler, ErrorManager.ErrorType.DIVISION_BY_ZERO)));
                }
                break ;
        }
    }

    /**  Tente de représenter l'expression {@code e} sous forme d'un 
     * {@link DVal} directement utilisable comme opérande d'une instruction 
     * 
     * Cas gérés : 
     * 
     * - {@link IntLiteral} --> {@link ImmediateInteger}
     * 
     * - {@link FloarLiteral} --> {@link ImmediateFLoat}
     * 
     * - {@link Identifier} --> (variable / paramètre) : adresse mémoire stable
     * 
     * Accès à un champs {@link FieldDefinition} exclu car son adresse n'est pas stable :
     * elle dépend de l'adresse d'un objet.
     * 
     * @param e expression éventuellement DVal 
     * @param compiler compilateur
     * @return un {@link DVal} si l'expression est adressable directement ou immédiate
     * sinon {@code null}
     * */
    public static DVal getEventualDVal(AbstractExpr e, DecacCompiler compiler) {
        if (e instanceof IntLiteral) {
            return new ImmediateInteger(((IntLiteral) e).getValue()) ;
        }
        else if (e instanceof FloatLiteral floatLiteral) {
            return new ImmediateFloat(floatLiteral.getValue()) ;
        }
        else if (e instanceof Identifier id) {
            // seulement var/param (adresse stable)
            if (!(id.getDefinition() instanceof FieldDefinition)) {
                return id.getAdress(compiler);
            }
            return null ;
        }
        else {
            return  null ;
        }
    }
}
