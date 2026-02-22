package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.VariableDefinition;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;

/*les imports ajoutés*/
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.deca.context.FieldDefinition;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;

import fr.ensimag.ima.pseudocode.DAddr;

/**Again i am trying to follow the strcture fo DeclVar.java */
/**
 * @author gl43
 * @date 01/01/2026
 */

public class DeclField extends AbstractDeclField {
    final private Visibility visibility;
    final private AbstractIdentifier type;
    final private AbstractIdentifier fieldName;
    final private AbstractInitialization initialization;

    public DeclField(AbstractIdentifier type, AbstractIdentifier fieldName, AbstractInitialization initialization, Visibility visibility){
        // Unlike DeclVar there is no sign of localENv in here as we don't dive into the body of methods nor the programm in pass 2
        Validate.notNull(visibility);
        Validate.notNull(type);
        Validate.notNull(fieldName);
        Validate.notNull(initialization);

        this.visibility = visibility;
        this.type = type;
        this.fieldName = fieldName;
        this.initialization = initialization;
    }

    public AbstractIdentifier getFieldName(){
        return fieldName;
    }

    @Override
    protected void verifyDeclField(DecacCompiler compiler, ClassDefinition currentClass) throws ContextualError {

            Type ttype = type.verifyType(compiler);
            // I am gonna try to follow all conditions in the instructions book (pages 80 & 81)

            // First condition : field cannot be of type void aka type ↓env_types ↑type  + condition type ≠ void 
            if (ttype.isVoid()) {
                throw new ContextualError("Field cannot be declared with type void ", type.getLocation()
                );
            }

            // Second condition : field name must not hide a method name of the superClass
            //(class(__, env_exp_super), __) ≜ env_types(super)
            // + env_exp_super(name) est défini ⇒ env_exp_super(name) = field

             //(class(__, env_exp_super), __) ≜ env_types(super)
            ClassDefinition superClass = currentClass.getSuperClass();
            if (superClass != null){
                ExpDefinition methodWithname = superClass.getMembers().get(fieldName.getName());
            // + env_exp_super(name) est défini ⇒ env_exp_super(name) = field
                if (methodWithname != null && methodWithname.isMethod()){
                    throw new ContextualError("Name already used for a method in super class: you can not hide a method with a field", fieldName.getLocation());
                }
            }
            FieldDefinition def = new FieldDefinition(ttype, fieldName.getLocation(), visibility, currentClass, currentClass.incNumberOfFields());
            Symbol symbol = fieldName.getName();



            try {
                // we declare the field in the members env
                currentClass.getMembers().declare(symbol, def);
            } catch (EnvironmentExp.DoubleDefException e) {
                throw new ContextualError("Field already declared in this class: " + symbol.getName(), fieldName.getLocation()
                );
            }

            fieldName.setDefinition(def);
            fieldName.setType(ttype);

            // We won't verify the initialization of fields for now as it is not required in pass 2 (in fact it might call verifyExpr which is pass 3)
            // initialization.verifyInitialization(compiler, ttype, currentClass.getMembers(), currentClass);
        }

    // The following method is supposed to verify initialization of the field in the 3rd pass
    @Override
    protected void verifyInitializationOfField(DecacCompiler compiler, ClassDefinition currentClass) throws ContextualError{
        // We alwyase create a new environment when we verify initialization as we souldn't be peeking (and accidentaly) modifying getMemebers)
        // + it is kinda the translation of : ↑{name 7 → (field(visib, class), type)}
        EnvironmentExp localEnv = new EnvironmentExp(currentClass.getMembers());
        Type fieldType = fieldName.getDefinition().getType();
        initialization.verifyInitialization(compiler, fieldType, localEnv, currentClass);
        
    }

    @Override
    public void decompile(IndentPrintStream s) {
        //throw new UnsupportedOperationException("not yet implemented");
        if (visibility != Visibility.PUBLIC) {
            s.print("protected ");
        }

        type.decompile(s);
        s.print(" ");

        fieldName.decompile(s);

        if (!(initialization instanceof NoInitialization)) {
            s.print(" = ");
            initialization.decompile(s);
        }

        s.println(";");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        type.iter(f);
        fieldName.iter(f);
        initialization.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        fieldName.prettyPrint(s, prefix, false);
        initialization.prettyPrint(s, prefix, true);
    }

    public void initField(DecacCompiler compiler){
        FieldDefinition fieldDef = fieldName.getFieldDefinition() ;
        GPRegister rR = compiler.getRegManager().allocReg();
        if (rR == null){
            rR = GPRegister.R0;
            compiler.getStackManager().pushRegTemp(compiler, rR);
        }
        int indexF = this.fieldName.getFieldDefinition().getIndex();
        compiler.addInstruction(new LOAD(new RegisterOffset(-2, GPRegister.LB),rR));
        DAddr adddrField = new RegisterOffset(indexF, rR);
        fieldDef.setOperand(adddrField);
        initialization.codeGenInitializationField(compiler,adddrField,type.getType()); 
        compiler.getRegManager().freeReg(rR); 
    }

    public AbstractIdentifier getType(){
        return type;
    }

}
