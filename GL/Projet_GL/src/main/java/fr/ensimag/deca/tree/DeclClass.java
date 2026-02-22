package fr.ensimag.deca.tree;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.ErrorManager;
import fr.ensimag.deca.codegen.RegisterManager;
import fr.ensimag.deca.context.BooleanType;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ClassType;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.context.FloatType;
import fr.ensimag.deca.context.IntType;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.TypeDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.ImmediateFloat;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.LabelOperand;
import fr.ensimag.ima.pseudocode.Line;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.BSR;
import fr.ensimag.ima.pseudocode.instructions.LEA;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH ;
import fr.ensimag.ima.pseudocode.instructions.RTS;
import fr.ensimag.ima.pseudocode.instructions.STORE ;
import fr.ensimag.ima.pseudocode.instructions.SUBSP ;
import fr.ensimag.ima.pseudocode.instructions.TSTO;

/**
 * Declaration of a class (<code>class name extends superClass {members}<code>).
 * 
 * @author gl43
 * @date 01/01/2026
 */
public class DeclClass extends AbstractDeclClass {

    // attributs ajoutés
    public final AbstractIdentifier name;
    public final AbstractIdentifier superClass;
    private final ListDeclField fields;
    private final ListDeclMethod methods;
     
    // On a choisit le TreeSet pour que le tri se fasse directement avec 

    //Constructeur ajouté
    public DeclClass(AbstractIdentifier name,AbstractIdentifier superClass, ListDeclField fields, ListDeclMethod methods){
        Validate.notNull(name);
        Validate.notNull(superClass);
        Validate.notNull(fields);
        Validate.notNull(methods);
        this.name = name;
        this.superClass = superClass;
        this.fields = fields;
        this.methods = methods;
    }
    //

    @Override
    public AbstractIdentifier getName(){
        return name;
    }

    public AbstractIdentifier getSuperClass(){
        return superClass;
    }

    // Modifiée
    @Override
    public void decompile(IndentPrintStream s) {
        
        s.print("class ");
        name.decompile(s);

        if (superClass != null) {
            s.print(" extends ");
            superClass.decompile(s);
        }

        s.println(" {");
        s.indent();

        fields.decompile(s);

        methods.decompile(s);

        s.unindent();
        s.println("}");
    }
    //

    //Méthode modifiée
    @Override
    protected void verifyClass(DecacCompiler compiler) throws ContextualError {

        SymbolTable.Symbol nomClasse = name.getName();

        // if(compiler.environmentType.defOfType(nomClasse) != null){
        //     throw new ContextualError("Class already defined "+nomClasse.getName(), name.getLocation());
        // }

        ClassDefinition superClassDef;

        SymbolTable.Symbol objectSymb = compiler.environmentType.OBJECT.getName();

        if (superClass.getName().equals(objectSymb.getName())) {
            superClassDef = compiler.environmentType.OBJECT.getDefinition();
        }

        else{

            SymbolTable.Symbol nomClasseMere = superClass.getName();
            TypeDefinition superTypeDEF = compiler.environmentType.defOfType(nomClasseMere);

            if (superTypeDEF == null){
                throw new ContextualError("Superclass does not exist: " + nomClasseMere.getName(),superClass.getLocation());
            }

            Type superType = superTypeDEF.getType();

            if(!superType.isClass()){
                throw new ContextualError( "Superclass is not a class ",superClass.getLocation());
            }

            superClassDef = ((ClassType) superType).getDefinition();
            superClass.setDefinition(superClassDef); //AJOUT LE 1 JANV
            superClass.setType(superType); //AJOUT LE 1 JANV

        }
        ClassType classType = new ClassType(nomClasse, name.getLocation(), superClassDef);
        //ClassDefinition classDef = new ClassDefinition(classType, name.getLocation(), superClassDef);
        try {
            compiler.environmentType.declareClass(nomClasse, classType.getDefinition());
        } catch (DoubleDefException e) {
            throw new ContextualError("Class already defined : " + nomClasse.getName(),
                name.getLocation()
            );
        }

        name.setDefinition(classType.getDefinition());
        name.setType(classType);
        //throw new UnsupportedOperationException("not yet implemented");
    }
    //

    @Override
    protected void verifyClassMembers(DecacCompiler compiler)
            throws ContextualError {
        //throw new UnsupportedOperationException("not yet implemented");

        ClassDefinition currentClass = name.getClassDefinition();
        
        ClassDefinition spClass = currentClass.getSuperClass() ;

        if(spClass != null) {
            currentClass.setNumberOfMethods(spClass.getNumberOfMethods());
            currentClass.setNumberOfFields(spClass.getNumberOfFields());
        }

        fields.verifyListDeclField(compiler, currentClass);

        methods.verifyListDeclMethod(compiler, currentClass);
    }
    
    @Override
    protected void verifyClassBody(DecacCompiler compiler) throws ContextualError {
        //throw new UnsupportedOperationException("not yet implemented");
        ClassDefinition currentClass = name.getClassDefinition();

        fields.verifyListFieldInitialization(compiler, currentClass);

        methods.verifyListMethodBody(compiler, currentClass);
    }
     

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        //throw new UnsupportedOperationException("Not yet supported");
        name.prettyPrint(s, prefix, false);

        if (superClass != null) {
            superClass.prettyPrint(s, prefix, false);
        }

        fields.prettyPrintChildren(s, prefix);

        methods.prettyPrintChildren(s, prefix);

    }

    @Override
    protected void iterChildren(TreeFunction f) {
        //throw new UnsupportedOperationException("Not yet supported");
        name.iter(f);
        superClass.iter(f);
        fields.iter(f);
        methods.iter(f);
    }


   @Override
    public void codeGenClassMthd(DecacCompiler compiler){
        initMethod(compiler); 
        for (AbstractDeclMethod method : methods.getList()){
            String name = method.getMethodName().getName().getName();
            String str = "code." + this.name.getName().getName() + "." + name;
            String strFin = "fin." + this.name.getName().getName() + "." + name;
            Label nomMthd = new Label(str);
            Label nomFinMthd = new Label(strFin);
            compiler.addLabel(nomMthd);
            method.codeGenClass(compiler,nomFinMthd);
            compiler.addInstruction(new RTS());
        }
        
    } 

    public void initMethod(DecacCompiler compiler){
        GPRegister R0 = GPRegister.getR(0);
        GPRegister R1 = GPRegister.getR(1);
        String nomC = "init." + this.name.getName().getName();
        String nomSuperC = "init." + this.superClass.getName().getName();
        
        compiler.addComment("Initialisation des champs de la classe "+this.name.getName().getName());
        compiler.addLabel(new Label(nomC));

        Line lineDebutProg = new Line(new TSTO(0)); // Le nombre de registre tmp
                                                      // sera calculée plus tard
        compiler.add(lineDebutProg);
        
        int d1 = 0;    // d1 c'est le nombre de places prises dans la pile par la mthd.

        int ancientCurrNbTemp = compiler.getStackManager().getCurrNbTemp();

        RegisterManager regManager = compiler.getRegManager();

        regManager.setIndexFirstFreeReg(2);
        
        regManager.activateMthd();

        Deque<GPRegister> activeFreeReg = regManager.getFreeReg();

        regManager.resetfreeRegs();

        ArrayList<Line> linesofPush = new ArrayList<>();

        for (int i = 0; i < 14; i++){
           Line notRealLine = new Line( new PUSH(GPRegister.R0));
           compiler.add(notRealLine);
           linesofPush.add(notRealLine); // On va les changes après avoir 
                                        // obtenu les regs utilisés.
        }

        if (!(superClass.getName().getName().equals("Object"))){ 
            d1 += 3;
            Label fullStack = ErrorManager.getErrorLabel(ErrorManager.ErrorType.STACK_OVERFLOW);
            compiler.addInstruction(new BOV(fullStack));
            GPRegister rR = compiler.getRegManager().allocReg();
            compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB),R1));
            for (AbstractDeclField f : fields.getList()){
               DeclField field = (DeclField) f;
               int indexF = field.getFieldName().getFieldDefinition().getIndex();
               if (field.getType().getType() instanceof IntType || 
               field.getType().getType() instanceof BooleanType ){
                    
                    compiler.addInstruction(new LOAD(new ImmediateInteger(0), R0)); // A optimiser On refait
                                                                // Le load a chaque fois
               } else if (field.getType().getType() instanceof FloatType ){
                    compiler.addInstruction(new LOAD( new ImmediateFloat(0), R0));
               } else {
                    compiler.addInstruction(new LOAD(new NullOperand(),R0));
               }
               compiler.addInstruction(new STORE(R0,new RegisterOffset(indexF, R1) ));
            }
            compiler.addInstruction(new PUSH(R1));
            compiler.addInstruction(new BSR(new Label(nomSuperC)));
            compiler.addInstruction(new SUBSP(1));
            compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB),R1));
            for (AbstractDeclField f : fields.getList()){
               DeclField field = (DeclField) f;
               field.initField(compiler);
            }
            compiler.addInstruction(new RTS());
        } else {
            for (AbstractDeclField f : fields.getList()){
               DeclField field = (DeclField) f;
               compiler.addComment("Entree dans la boucle des fields");
               field.initField(compiler);
            }
        }
        
        TreeSet<GPRegister> regsPushed = regManager.regStack();

        int nbrofRegPush = regsPushed.size();

        d1 += nbrofRegPush;
        
        for (int i = nbrofRegPush; i > 0;i--){
            compiler.addInstruction(new POP(GPRegister.getR(i + 1)));
        }

        for (int i = 0;i < 14; i++ ){
            Line l = linesofPush.get(i);
            if (nbrofRegPush > 0 ){
                l.setInstruction(new PUSH(GPRegister.getR(i+2)));
                nbrofRegPush--;
            } else{
                compiler.remove(l);
            }
        }

        int newCurrNbTemp = compiler.getStackManager().getCurrNbTemp();

        d1 += newCurrNbTemp - ancientCurrNbTemp; // Si R0 et R1 ont été push
                                                // pendant la méthode 

        if (d1  > 0){
            lineDebutProg.setInstruction(new TSTO(d1));
        } else{
            compiler.remove(lineDebutProg);
        }

        regManager.setFreeReg(activeFreeReg);

        regManager.setIndexFirstFreeReg(2);

        compiler.addInstruction(new RTS());
    }

    
    /* 
    public void buildEtiquetteList(TreeSet<LabelMethod> TableEtiquetteSuperC){
        for (AbstractDeclMethod method : methods.getList()){
            int index = method.getMethodName().getMethodDefinition().getIndex();
            String name = method.getMethodName().getName().getName();
            String str = "code." + this.name.getName().getName() + "." + name;
            LabelMethod labelMthd = new LabelMethod(str, index);
            tableEtiquettes.add(labelMthd);
        }
        for (LabelMethod labelMthd : TableEtiquetteSuperC){
            tableEtiquettes.add(labelMthd); // Si une méthod a été Overridée dans la
                                            // sous classe, la même méthode dans la SuperClasse
                                            // n'est pas ajouté dans le TreeSet car elles ont 
                                            // le même indexMethod
        }
    } */

    //extension
    @Override
    public boolean foldConstants(DecacCompiler compiler){
        boolean changed = false;

        //changed |= fields.foldConstants(compiler);
        changed |= methods.foldConstants(compiler);

        return changed;
    }

    @Override
    protected boolean eliminateDeadCode(DecacCompiler compiler) {
        return methods.eliminateDeadCode(compiler);
    }

    @Override
    protected boolean eliminateDeadStores(DecacCompiler compiler) {
        return methods.eliminateDeadStores(compiler);
    }

    @Override
    protected boolean eliminateUnusedVars(DecacCompiler compiler) {
        return methods.eliminateUnusedVars(compiler);
    }

    @Override
    protected void collectReadVars(Set<Symbol> reads) {
        methods.collectReadVars(reads);
    }

    @Override
    protected void collectWrittenVars(Set<Symbol> writes) {
        methods.collectWrittenVars(writes);
    }


    // nouvellement intégré
    // passe 1 : construction de la table des étiquettes
    protected void constructEtiqTable() {
        ClassDefinition defC = name.getClassDefinition() ;
        ClassDefinition defSC = defC.getSuperClass() ;
        String className = name.getName().getName() ;

        int nbMethodsC = defC.getNumberOfMethods() ;
        Label[] etiqsC = new Label[nbMethodsC] ;

        // copier la table d'étiquettes de la super classe si elle existe
        if (defSC != null && defSC.getTabEtiq() != null) {
            Label[] etiqsSC = defSC.getTabEtiq() ;
            System.arraycopy(etiqsSC, 0, etiqsC, 0, etiqsSC.length);
        }

        // puis là override et méthodes nouvelles : 
        // terminaison du remplissage du tableau des étiquettes
        for (AbstractDeclMethod abstM : methods.getList()) {
            DeclMethod m = (DeclMethod) abstM ;

            AbstractIdentifier idM = m.getMethodName() ;
            MethodDefinition methodDef = idM.getMethodDefinition() ;

            int idx = methodDef.getIndex() ;
            String methodName = idM.getName().getName() ;


            etiqsC[idx - 1] = new Label("code." + className + "." + methodName) ;
        }

        defC.setTabEtiq(etiqsC);

        // pour débugguer
        //System.out.println("TableEtiq de " + className);
        for (int i = 0; i < etiqsC.length; i++) {
            //System.out.println((i+1) + ": " + etiqsC[i]);
        }
    }

    // passe 1 : construction de la table des méthodes
    protected void constructMethodTable(DecacCompiler compiler) {
        ClassDefinition defC = name.getClassDefinition() ;
        ClassDefinition defSC = defC.getSuperClass() ;
        String className = name.getName().getName() ;
        int nbMethodsC = defC.getNumberOfMethods() ;
        Label[] etiqsC = defC.getTabEtiq() ;

        if (etiqsC == null) {
            throw new IllegalStateException("Tableau des étiquettes not built before table des méthodes for class : " + className) ;
        }

        int baseOffset = compiler.allocGBBlock(nbMethodsC + 1) ; // + 1 pour le pointeur vers la super class
        DAddr baseAdrr = compiler.gbAddr(baseOffset) ;
        defC.setMethodTabAddr(baseAdrr);

        GPRegister rTemp = GPRegister.R0 ;
        if (defSC != null && defSC.getMethodTabAddr() != null) {
            compiler.addInstruction(new LEA(defSC.getMethodTabAddr(), rTemp));
        }
        else {
            compiler.addInstruction(new LOAD(new NullOperand(), rTemp));
        }

        compiler.addInstruction(new STORE(rTemp, compiler.gbAddr(baseOffset)));

        for (int i = 1 ; i <= nbMethodsC ; i++) {
            Label etiq = etiqsC[i - 1] ;
            if (etiq == null) {
                throw new IllegalStateException("Missing label in method table for class " + className + " at index " + i) ;
            }
            compiler.addInstruction(new LOAD(new LabelOperand(etiq), rTemp)) ;
            compiler.addInstruction(new STORE(rTemp, compiler.gbAddr(baseOffset + i)));
        }
        
    }


    @Override
    public void codeGenClass(DecacCompiler compiler){
        compiler.addComment("Construction de la table des méthodes de " + name.getName().getName());
        constructEtiqTable(); 
        constructMethodTable(compiler);
    }
}