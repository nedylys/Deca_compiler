package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;

/**
 * Arithmetic binary operations (+, -, /, ...)
 * 
 * @author gl43
 * @date 01/01/2026
 */
public abstract class AbstractOpArith extends AbstractBinaryExpr {

    public AbstractOpArith(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    // F j'ai implémenté cette méthode :
    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        //throw new UnsupportedOperationException("not yet implemented");
        Type leftOperand_Type = this.getLeftOperand().verifyExpr(compiler, localEnv, currentClass); // les verifyExpr retournent tous les types
        Type rightOperand_Type = this.getRightOperand().verifyExpr(compiler, localEnv, currentClass);
        this.getLeftOperand().verifyRValue(compiler, localEnv, currentClass, leftOperand_Type);
        this.getRightOperand().verifyRValue(compiler, localEnv, currentClass, rightOperand_Type);
        // Règle : On ne peut faire des opérations arithmétiques qu'entre des int & float
        if (leftOperand_Type.isInt()){
            if (rightOperand_Type.isInt()){
                setType(leftOperand_Type); // Décoration de type
                return leftOperand_Type;
            }
            else if (rightOperand_Type.isFloat()){
                // Ajout du noeud ConvFloat (conversion du type int -> float)
                ConvFloat convLeft = new ConvFloat(this.getLeftOperand());
                convLeft.verifyExpr(compiler, localEnv, currentClass);
                convLeft.setLocation(this.getLocation());
                
                this.setLeftOperand(convLeft); // On remplace l'opérande gauche par sa
                setType(rightOperand_Type); // Décoration de type
                return rightOperand_Type;
            }
            else {
                // Type invalide pour l'opérande droit
                throw new ContextualError("Type invalide pour opérande droit dans opération arithmétique, veuillez vous assurer que ça soit " + rightOperand_Type , this.getLocation());
            }
        }

        else if (leftOperand_Type.isFloat()){
            if (rightOperand_Type.isFloat()){
                setType(leftOperand_Type); // Décoration de type
                return leftOperand_Type;
            }
            else if (rightOperand_Type.isInt()){
                // Ajout du noeud ConvFloat (conversion du type int -> float)
                ConvFloat convRight = new ConvFloat(this.getRightOperand());
                convRight.setType(compiler.environmentType.FLOAT);
                this.setRightOperand(convRight); // On remplace l'opérande droit par sa conversion
                setType(leftOperand_Type); // Décoration de type
                return leftOperand_Type;
            }
            else {
                // Type invalide pour l'opérande droit
                throw new ContextualError("Type invalide pour opérande droit dans opération arithmétique, veuillez vous assurer que ça soit " + rightOperand_Type, this.getLocation());
            }
        }
        else {
            // Type invalide pour l'opérande gauche
            throw new ContextualError("Type invalide pour opérande gauche dans opération arithmétique, veuillez vous assurer que ça soit soit int soit float ", this.getLocation());
        }

        
    }

    //extension
    @Override
    public AbstractExpr foldExpr(DecacCompiler compiler) {
        AbstractExpr left = getLeftOperand().foldExpr(compiler);
        AbstractExpr right = getRightOperand().foldExpr(compiler);

        if (left != getLeftOperand()) setLeftOperand(left);
        if (right != getRightOperand()) setRightOperand(right);

        // int op int
        if (left instanceof IntLiteral && right instanceof IntLiteral) {
            int a = ((IntLiteral) left).getValue();
            int b = ((IntLiteral) right).getValue();

            // Pour Divide, on veut pouvoir refuser b==0
            if (!canFoldInt(a, b)) return this;

            IntLiteral res = new IntLiteral(evalInt(a, b));
            //res.setType(getType());
             if (getType() != null) {
                res.setType(getType());
            }
            res.setLocation(getLocation());
            return res;
        }

        // float op float
        if (left instanceof FloatLiteral && right instanceof FloatLiteral) {
            float a = ((FloatLiteral) left).getValue();
            float b = ((FloatLiteral) right).getValue();

            if (!canFoldFloat(a, b)) return this;

            FloatLiteral res = new FloatLiteral(evalFloat(a, b));
            //res.setType(getType()); 16 janv
             if (getType() != null) {
                res.setType(getType());
            }
            res.setLocation(getLocation());
            return res;
        }

        return this;
    }

    /** Permet à Divide/Modulo de refuser certains folds (ex: division par 0). */
    protected boolean canFoldInt(int a, int b) { return true; }
    protected boolean canFoldFloat(float a, float b) { return true; }

    protected abstract int evalInt(int a, int b);
    protected abstract float evalFloat(float a, float b);

}
