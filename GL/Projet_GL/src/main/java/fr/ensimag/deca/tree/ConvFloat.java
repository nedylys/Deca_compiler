package fr.ensimag.deca.tree;

import java.util.Set;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.instructions.FLOAT;


/**
 * Conversion of an int into a float. Used for implicit conversions.
 * 
 * @author gl43
 * @date 01/01/2026
 */
public class ConvFloat extends AbstractUnaryExpr {
    public ConvFloat(AbstractExpr operand) {
        super(operand);
    }

/*     @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) {
        //throw new UnsupportedOperationException("not yet implemented");
        // On remarque que verifyExpr n'a pas de ContextualError à lancer:
        // en effet par construction de l'arbre (verifyExpr dans AbstractOParith on est certain
        // que l'opérande est de type int)
        // La décoration et vérification de l'opérande se fait dans AbstractOpArith
        setType(compiler.environmentType.FLOAT); // Décoration de type
        return compiler.environmentType.FLOAT;
    } */
    
    /*---------------------!!!!!!!!! Alerte !!!!!!!!!!-------------------------------- */
    /* ça peut arriver que à un certain moment on ait besoin d'ajouter un
    throw ContextualError("not yet implemented", getLocation()); à la définition
    de verifyExpr ci-dessus, mais à ce stade du projet on en a pas besoin
    */

    /* Bon je le fait en tout cas pour respecter l'architecture */

    // F j'ai implémenté cette méthode :
    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError{
        //getOperand().verifyExpr(compiler, localEnv, currentClass); // This causes a double verification of the operand which causes an error:
        /* Wanted 1 time:
             intexpr1.verifyExpr(...)
        But was 2 times:
            -> at fr.ensimag.deca.tree.AbstractOpIneq.verifyExpr(AbstractOpIneq.java:26)
            -> at fr.ensimag.deca.tree.ConvFloat.verifyExpr(ConvFloat.java:47)
        */
       // But for safety we can do :
/*         if (getOperand().getType() == null) {
            getOperand().verifyExpr(compiler, localEnv, currentClass);
        } */
        getOperand().verifyExpr(compiler, localEnv, currentClass);
        setType(compiler.environmentType.FLOAT); // Décoration de type
        return compiler.environmentType.FLOAT;
    }
    @Override
    protected String getOperatorName() {
        return "/* conv float */";
    }

    @Override
    public void codeGenExpr(DecacCompiler compiler, GPRegister targetReg) {
        // Evalue l'opérande(qui est un int), résultat dans targetReg
        getOperand().codeGenExpr(compiler, targetReg);
        // Convertit l'entier en flottant : targetReg = (float) targetReg
        compiler.addInstruction(new FLOAT(targetReg, targetReg));
    }

    //extension
     
    @Override
    public AbstractExpr foldExpr(DecacCompiler compiler) {
        AbstractExpr inner = getOperand().foldExpr(compiler);
        if (inner != getOperand()) setOperand(inner);

        if (inner instanceof IntLiteral) {
            FloatLiteral res = new FloatLiteral((float) ((IntLiteral) inner).getValue());
            res.setType(getType());
            res.setLocation(getLocation());
            return res;
        }
        return this;
    } 

    //ajout le 18 janv
    @Override
    protected void collectReadVars(Set<Symbol> reads) {
        getOperand().collectReadVars(reads);
    }

}
