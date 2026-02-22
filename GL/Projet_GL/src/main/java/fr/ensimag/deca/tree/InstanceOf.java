package fr.ensimag.deca.tree;

import java.io.PrintStream;
import java.util.Set;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ClassType;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.LEA;
import fr.ensimag.ima.pseudocode.instructions.LOAD;

public class InstanceOf extends AbstractExpr {

    private AbstractExpr expr;
    private AbstractIdentifier typeInstance;

    public InstanceOf(AbstractExpr expr, AbstractIdentifier typeInstance) {
        Validate.notNull(expr);
        Validate.notNull(typeInstance);
        this.expr = expr;
        this.typeInstance = typeInstance;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        expr.decompile(s);
        s.print(" instanceof ");
        typeInstance.decompile(s);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        Type exprType = expr.verifyExpr(compiler, localEnv, currentClass);
        Type instanceType = typeInstance.verifyType(compiler);

        if (!(exprType.isClass() || exprType.isNull())) {
            throw new ContextualError("Instanceof could only be applied to a class or nullType, but a " + exprType + " was given.", getLocation());
        }

        if (!(instanceType.isClass())) {
            throw new ContextualError("Instanceof expected a class type but a " + instanceType + " was given.", getLocation());
        }

        setType(compiler.environmentType.BOOLEAN);
        return compiler.environmentType.BOOLEAN;
    }

    public AbstractExpr getExpr() {
        return expr;
    }

    public AbstractIdentifier getTypeInstance() {
        return typeInstance;
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        expr.iter(f);
        typeInstance.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        expr.prettyPrint(s, prefix, false);
        typeInstance.prettyPrint(s, prefix, true);
    }

    @Override
    public void codeGenExpr(DecacCompiler compiler, GPRegister targetReg) {
        
        // eval dans targetReg
        expr.codeGenExpr(compiler, targetReg);

        // def de la classe
        ClassType targetType = (ClassType) typeInstance.getType();
        ClassDefinition targetClassDef = targetType.getDefinition();

        // création des labels
        int labelNum = compiler.getLabelCounter();
        Label instanceTrue = new Label("instanceof_true." + labelNum);
        Label instanceFalse = new Label("instanceof_false." + labelNum);
        Label instanceEnd = new Label("instanceof_end." + labelNum);

        // objet null
        compiler.addInstruction(new CMP(new NullOperand(), targetReg));
        compiler.addInstruction(new BEQ(instanceFalse));


        // Si instanceof est VRAI --> branche vers instanceTrue
        // sinon continue après (tombe dans BRA instanceFalse)
        
        genCodeInstanceOfCheck(compiler, targetReg, targetClassDef, instanceTrue, labelNum);
        
        // Si on arrive là, instanceof est faux
        compiler.addInstruction(new BRA(instanceFalse));

        // arrivée à true
        compiler.addLabel(instanceTrue);
        compiler.addInstruction(new LOAD(1, targetReg));  // targetReg = 1 (true)
        compiler.addInstruction(new BRA(instanceEnd));

        
        // false 
        compiler.addLabel(instanceFalse);
        compiler.addInstruction(new LOAD(0, targetReg));  // targetReg = 0 (false)

        // fin
        compiler.addLabel(instanceEnd);
    }


    public static void genCodeInstanceOfCheck(DecacCompiler compiler,
                                               GPRegister objectReg,
                                               ClassDefinition targetClass,
                                               Label successLabel,
                                               int labelNum) {
        
        // labels pour la boucle
        Label loopStart = new Label("instanceof_loop." + labelNum);
        Label loopEnd = new Label("instanceof_end_loop." + labelNum);

        // allocation d'un registre pour parcourir la table
        GPRegister tableReg = compiler.getRegManager().allocRegAnyway(compiler);
        

        // charger la table des méthodes
        compiler.addInstruction(new LOAD(new RegisterOffset(0, objectReg), tableReg));


        // charger l'@ de la table de la classe cible
        DAddr targetAddr = targetClass.getMethodTabAddr();
        compiler.addInstruction(new LEA(targetAddr, GPRegister.R0));

        // boucler pour vérifier concordance
        compiler.addLabel(loopStart);

        // comparer la table courante avec la table cible
        compiler.addInstruction(new CMP(GPRegister.R0, tableReg));
        compiler.addInstruction(new BEQ(successLabel));  

        // remonter à la super class
        compiler.addInstruction(new LOAD(new RegisterOffset(0, tableReg), tableReg));

        // on a atteint null (Object)
        compiler.addInstruction(new CMP(new NullOperand(), tableReg));
        compiler.addInstruction(new BEQ(loopEnd)); // eched

        // boucler...
        compiler.addInstruction(new BRA(loopStart));

        // fin de la boucle : echec
        compiler.addLabel(loopEnd);

        compiler.getRegManager().freeRegAnyway(compiler, tableReg);
    }


    @Override
    protected void codeGenBool(DecacCompiler compiler, boolean b, Label E) {
        GPRegister reg = compiler.getRegManager().allocRegAnyway(compiler) ;

        expr.codeGenExpr(compiler, reg);

        int numLabel = compiler.getLabelCounter() ;
        Label isInstanceOf = new Label("instanceof_bool_true." + numLabel) ;
        Label endInstanceOf = new Label("instanceof_bool_end." + numLabel) ;

        compiler.addInstruction(new CMP(new NullOperand(), reg));
        if (b) {
            compiler.addInstruction(new BEQ(endInstanceOf));
        }
        else {
            compiler.addInstruction(new BEQ(E));
        }

        ClassType targetType = (ClassType) typeInstance.getType() ;
        ClassDefinition targetClassDef = targetType.getDefinition() ;

        genCodeInstanceOfCheck(compiler, reg, targetClassDef, isInstanceOf, numLabel);

        if (!b) {
            compiler.addInstruction(new BRA(E));
        }
        else {
            compiler.addInstruction(new BRA(endInstanceOf));
        }

        compiler.addLabel(isInstanceOf);
        if (b) {
            compiler.addInstruction(new BRA(E));
        }

        compiler.addLabel(endInstanceOf);
        
        compiler.getRegManager().freeRegAnyway(compiler, reg);
    }

    //ajout le 18 janv
    @Override
    protected void collectReadVars(Set<Symbol> reads) {
        expr.collectReadVars(reads);
    }

}