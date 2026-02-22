package fr.ensimag.deca.tree;

import java.util.Set;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.ErrorManager;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.Definition;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.FieldDefinition;
import fr.ensimag.deca.context.ParamDefinition;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.VariableDefinition;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.STORE;

/**
 * Assignment, i.e. lvalue = expr.
 *
 * @author gl43
 * @date 01/01/2026
 */
public class Assign extends AbstractBinaryExpr {

    @Override
    public AbstractLValue getLeftOperand() {
        // The cast succeeds by construction, as the leftOperand has been set
        // as an AbstractLValue by the constructor.
        return (AbstractLValue)super.getLeftOperand();
    }

    public Assign(AbstractLValue leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    // F j'ai implémenté cette méthode :
    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        // throw new UnsupportedOperationException("not yet implemented");


        // (3.28)
        Type leftOperand_Type = this.getLeftOperand().verifyExpr(compiler, localEnv, currentClass); // les verifyExpr retournent tous les types
        //Type leftOperand_Type = getLeftOperand().verifyLValue(compiler, localEnv, currentClass);
        AbstractExpr rightOperand = this.getRightOperand().verifyRValue(compiler, localEnv, currentClass, leftOperand_Type);
        setRightOperand(rightOperand);

        
/*         if (leftOperand_Type.isFloat() && rightOperand_Type.isInt()) {
            // autoriser l'affectation d'un int à un float en effectuant une conversion
            // implicite de l'entier en flottant
            // On peut créer une nouvelle expression qui représente cette conversion
            ConvFloat conv_right = new ConvFloat(getRightOperand());
            setRightOperand(conv_right);
            rightOperand_Type = compiler.environmentType.FLOAT; // Met à jour le type du côté droit après conversion
        }
 */
/*         // Rule right operand must be assign compatible with left operand
        if (!leftOperand_Type.assignCompatible(rightOperand_Type)) {
            throw new ContextualError("Incompatible types: cannot assign " + rightOperand_Type.getName() + " to " + leftOperand_Type.getName(), this.getLocation());
        } */

        // AbstractLValue lv = getLeftOperand();
        // if (lv instanceof Identifier){
        //     Identifier id = (Identifier) getLeftOperand();
        //     if (id.getDefinition() instanceof VariableDefinition){
        //         ((VariableDefinition) id.getVariableDefinition()).setInitialized(true);
        //     }
        // }
        
        setType(leftOperand_Type); // Décoration de type : pour une affectation on a : r := lval.’ = ’.e dans le poly
        return leftOperand_Type;
    }


    @Override
    protected String getOperatorName() {
        return "=";
    }

/*     @Override
    public void codeGenExpr(DecacCompiler compiler, GPRegister targetReg) {
        //Evaluate the expression on the right
        
        getRightOperand().codeGenExpr(compiler, targetReg);

        //Store the value in the adress on the left
        if (this.getLeftOperand() instanceof Selection){
            Selection fieldSelect = (Selection) this.getLeftOperand();
            DAddr addr = fieldSelect.getAdress(compiler); // Avec Objet le contenu
                                                                  // de la pile devient lui 
                                                                  // même un pointeur
            compiler.addInstruction(new STORE(targetReg,addr));
        } else{
            compiler.addInstruction(
            new STORE(
                targetReg, 
                getLeftOperand().getAdress(compiler)
            )
        );
        }
    } */


    @Override
    public void codeGenExpr(DecacCompiler compiler, GPRegister targetReg) {

        getRightOperand().codeGenExpr(compiler, targetReg);

        AbstractLValue lv = getLeftOperand();

/*         if (lv instanceof Selection sel) {
            int indexF = sel.getField().getFieldDefinition().getIndex();

            GPRegister rObj = compiler.getRegManager().allocRegAnyway(compiler);
            sel.getObjectExpr().codeGenExpr(compiler, rObj);

            if (!compiler.isNoCheck()) {
                compiler.addInstruction(new CMP(new NullOperand(), rObj));
                Label err = ErrorManager.useAndGetLabel(compiler, ErrorManager.ErrorType.DEREFERENCEMENT_NULL);
                compiler.addInstruction(new BEQ(err));
            }

            compiler.addInstruction(new STORE(targetReg, new RegisterOffset(indexF, rObj)));
            compiler.getRegManager().freeRegAnyway(compiler, rObj);
            return;
        } */

        if (lv instanceof Selection sel) {
            int indexF = sel.getField().getFieldDefinition().getIndex();
        
            GPRegister rObj = compiler.getRegManager().allocRegAnyway(compiler);
            sel.getObjectExpr().codeGenExpr(compiler, rObj);
        
            if (!compiler.isNoCheck()) {
                compiler.addInstruction(new CMP(new NullOperand(), rObj));
                Label err = ErrorManager.useAndGetLabel(compiler, ErrorManager.ErrorType.DEREFERENCEMENT_NULL);
                compiler.addInstruction(new BEQ(err));
            }
        
            compiler.getStackManager().pushRegTemp(compiler, rObj);
        
            getRightOperand().codeGenExpr(compiler, targetReg);
        
            compiler.getStackManager().popRegTemp(compiler, rObj);
        
            compiler.addInstruction(new STORE(targetReg, new RegisterOffset(indexF, rObj)));
            compiler.getRegManager().freeRegAnyway(compiler, rObj);
            return;
        }
        

        if (lv instanceof Identifier id && id.getDefinition() instanceof FieldDefinition) {
            // this.f = rhs  (champ implicite)
            int indexF = id.getFieldDefinition().getIndex();

            GPRegister rThis = compiler.getRegManager().allocRegAnyway(compiler);
            compiler.addInstruction(new LOAD(
                    new RegisterOffset(-2, GPRegister.LB), rThis));

            compiler.addInstruction(new STORE(targetReg, new RegisterOffset(indexF, rThis)));
            compiler.getRegManager().freeRegAnyway(compiler, rThis);
            return;
        }

        // var ou param
        DAddr addr = lv.getAdress(compiler);
        compiler.addInstruction(new STORE(targetReg, addr));
    }


    //extension
    @Override
    public AbstractExpr foldExpr(DecacCompiler compiler){
        //boolean changed = false;
        AbstractExpr right = getRightOperand().foldExpr(compiler);
        if (right != getRightOperand()) setRightOperand(right);
        return this;
    }

    @Override
    public void collectReadVars(Set<Symbol> reads) {
        // RHS lu
        getRightOperand().collectReadVars(reads);

        // LHS : si ce n'est pas un simple Identifier, il peut contenir des lectures (obj, index, etc.)
        if (!(getLeftOperand() instanceof Identifier)) {
            getLeftOperand().collectReadVars(reads);
        }
    }

        @Override
        public void collectWrittenVars(Set<Symbol> writes) {
            if (getLeftOperand() instanceof Identifier) {
                Identifier id = (Identifier) getLeftOperand();
                Definition def = id.getDefinition();
                if (def instanceof VariableDefinition && !(def instanceof ParamDefinition)) {
                    writes.add(id.getName());
                }
            }
        }
}
