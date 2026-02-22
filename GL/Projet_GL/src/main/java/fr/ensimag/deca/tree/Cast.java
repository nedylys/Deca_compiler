package fr.ensimag.deca.tree;

import java.io.PrintStream;
import java.util.Set;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.ErrorManager;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ClassType;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.FLOAT;
import fr.ensimag.ima.pseudocode.instructions.INT;





public class Cast extends AbstractExpr{
    
    private AbstractIdentifier typeCast;
    private AbstractExpr expr;
    
    public Cast(AbstractIdentifier typeCast,AbstractExpr expr){
        Validate.notNull(typeCast);
        Validate.notNull(expr);
        this.typeCast = typeCast;
        this.expr = expr;
    }

    public AbstractIdentifier getTypeCast(){
        return typeCast;
    }

    public AbstractExpr getExpr(){
        return expr;
    }

    @Override
    public void decompile(IndentPrintStream s) { // je sais pas si c'est bon 
        s.print("( ");
        typeCast.decompile(s);
        s.print(") ");
        s.print("( ");
        expr.decompile(s);
        s.print(") ");
    }
    
    
    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        //throw new UnsupportedOperationException("not yet implemented");
        Type castingType = typeCast.verifyType(compiler);
        Type exprType = expr.verifyExpr(compiler, localEnv, currentClass);
        if (castingType.isVoid()){
            throw new ContextualError("Can not cast using a void Type", getLocation());
        }
        if(!(castingType.isCastCompatible(exprType))){
            throw new ContextualError("Incompatible cast, can not cast " + exprType + " to " + castingType, getLocation());
        }
        setType(castingType);
        return castingType;
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        typeCast.iter(f);
        expr.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        typeCast.prettyPrint(s, prefix, false);
        expr.prettyPrint(s, prefix, true);
    }

    @Override
    public void codeGenExpr(DecacCompiler compiler, GPRegister targetReg) {
        
        Type targetType = typeCast.getType();
        Type exprType = expr.getType();

        // Évaluer l'expression dans targetReg
        expr.codeGenExpr(compiler, targetReg);

        // conversion vers le mm type
        if (exprType.sameType(targetType)) {
            return;
        }

        // int vers float
        if (exprType.isInt() && targetType.isFloat()) {
            compiler.addInstruction(new FLOAT(targetReg, targetReg));
            return;
        }

        // float vers int
        if (exprType.isFloat() && targetType.isInt()) {
            compiler.addInstruction(new INT(targetReg, targetReg));
            return;
        }

        // transtypage inter-classes
        if (exprType.isClass() && targetType.isClass()) {
            codeGenCastClass(compiler, targetReg, (ClassType) targetType);
        }
    }

    private void codeGenCastClass(DecacCompiler compiler, GPRegister targetReg, ClassType targetType) {
        
        int labelNum = compiler.getLabelCounter();
        Label castOk = new Label("cast_ok." + labelNum);

        
        // null peut être casté vers n'importe quelle classe
        compiler.addInstruction(new CMP(new NullOperand(), targetReg));
        compiler.addInstruction(new BEQ(castOk));


        // instanceof
        ClassDefinition targetClassDef = targetType.getDefinition();
        
        // Si instanceof est true => branche vers castOk
        InstanceOf.genCodeInstanceOfCheck(compiler, targetReg, targetClassDef, castOk, labelNum);

        // cast échoué
        compiler.useError(ErrorManager.ErrorType.CAST_ERROR);
        compiler.addInstruction(new BRA(ErrorManager.getErrorLabel(ErrorManager.ErrorType.CAST_ERROR)));

        // étiquette de succès de transtypage
        compiler.addLabel(castOk);
        // targetReg contient toujours l'adresse de l'objet
    }


    //extension
    @Override
    public AbstractExpr foldExpr(DecacCompiler compiler){
        AbstractExpr n_expr = expr.foldExpr(compiler);

        if(n_expr != expr){
            expr = n_expr;
        }
        return this;
    }

    public AbstractExpr getExpression(){
        return expr;
    }

    //ajout le 18 janv
    @Override
    protected void collectReadVars(Set<Symbol> reads) {
        expr.collectReadVars(reads);
    }

}