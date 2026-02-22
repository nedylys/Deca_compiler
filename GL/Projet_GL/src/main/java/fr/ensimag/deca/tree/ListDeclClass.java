package fr.ensimag.deca.tree;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.LabelOperand;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.RTS;
import fr.ensimag.ima.pseudocode.instructions.STORE;
/**
 *
 * @author gl43
 * @date 01/01/2026
 */
public class ListDeclClass extends TreeList<AbstractDeclClass> {
    private static final Logger LOG = Logger.getLogger(ListDeclClass.class);
    public int size = this.getList().size();

    
    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractDeclClass c : getList()) {
            c.decompile(s);
            s.println();
        }
    }

    /**
     * Pass 1 of [SyntaxeContextuelle]
     */
    //Première passe implémentée
    void verifyListClass(DecacCompiler compiler) throws ContextualError {
        LOG.debug("verify listClass: start");
        for(AbstractDeclClass c : this.getList()){
            c.verifyClass(compiler);
        }
        //throw new UnsupportedOperationException("not yet implemented");
        LOG.debug("verify listClass: end");
    }

    /**
     * Pass 2 of [SyntaxeContextuelle]
     */
    public void verifyListClassMembers(DecacCompiler compiler) throws ContextualError {
        //throw new UnsupportedOperationException("not yet implemented");
        for(AbstractDeclClass c : this.getList()){
            c.verifyClassMembers(compiler);
        }
    }
    
    /**
     * Pass 3 of [SyntaxeContextuelle]
     */
    public void verifyListClassBody(DecacCompiler compiler) throws ContextualError {
        //throw new UnsupportedOperationException("not yet implemented");
        for(AbstractDeclClass c : this.getList()){
            c.verifyClassBody(compiler);
        }
    }
    
    public void codeGenClassMthd(DecacCompiler compiler){
        codeObjectEq(compiler);
        for (AbstractDeclClass declClass : this.getList()){
            declClass.codeGenClassMthd(compiler);
        }
    } 

    /* 
    public void buildEtiquetteTable(){
        TreeSet<LabelMethod> tableEtiquetteSuperC;
        TreeSet<LabelMethod> tableEtiquetteObject = new TreeSet<>();
        LabelMethod objectEq = new LabelMethod("code.Object.equals", 1);
        tableEtiquetteObject.add(objectEq);
        List<AbstractDeclClass> copyList = this.getList();
        for (AbstractDeclClass abstractC : this.getList()){
            DeclClass c = (DeclClass) abstractC;
            String nomSuperC = c.getSuperClass().getName().getName();
            System.out.println("nomSuperC = " + nomSuperC);
            for (AbstractDeclClass abstractC2 : copyList){
                DeclClass c2 = (DeclClass) abstractC;
                String nomc2 = c2.getName().getName().getName();
                System.out.println("nomC = " + nomc2);
                if (nomc2.equals(nomSuperC)){
                    tableEtiquetteSuperC = c2.getTableEtiquette();
                    c.buildEtiquetteList(tableEtiquetteSuperC);
                    break;
                }
            }
            c.buildEtiquetteList(tableEtiquetteObject);
                                                           // On recupere les étiquettes
                                                          // du la superClasse
        }
    } */

    
    
    public void codeObjectEq(DecacCompiler compiler){
        GPRegister R0 = GPRegister.getR(0);
        GPRegister R1 = GPRegister.getR(1);
        
        Label eq = new Label("equal");
        Label notEq = new Label("not_equal");

        compiler.addLabel(new Label("code.Object.equals"));

        compiler.addInstruction(new LOAD(new RegisterOffset(-2, GPRegister.LB),R0));
        compiler.addInstruction(new LOAD(new RegisterOffset(-3, GPRegister.LB),R1));
        compiler.addInstruction(new CMP(R0,R1));
        compiler.addInstruction(new BEQ(eq));
        compiler.addLabel(notEq);
        compiler.addInstruction(new LOAD(0,R0));
        compiler.addInstruction(new RTS());
        compiler.addLabel(eq);
        compiler.addInstruction(new LOAD(1, R0));
        compiler.addInstruction(new RTS());
    }

    /* 
    public void buildTableMethd(DecacCompiler compiler){
        GPRegister R0 = GPRegister.getR(0);
        
        
        // On construit la Table des méthodes de Object
        compiler.addComment("Construction des tables des methodes");

        int dGB = compiler.allocGB();
        compiler.addInstruction(new LOAD(new NullOperand(),R0));
        compiler.addInstruction(new STORE(R0,new RegisterOffset(dGB, Register.GB)));

        dGB = compiler.allocGB();
        compiler.addInstruction(new LOAD(new LabelOperand(new Label("code.Object.equals")),R0));
        compiler.addInstruction(new STORE(R0,new RegisterOffset(dGB, Register.GB)));
        
        int lastClassdGB; // Sert à relier la table à sa table de superClasse
        
        if (this.getList().size() > 0){
            DeclClass premiereC = (DeclClass) this.getList().get(0); // On est sur que la première classe
            premiereC.getSuperClass().getClassDefinition().setdGB(1); // a pour superclasse Object
        }                                                                  
        
        for (AbstractDeclClass abstractC : this.getList()){
            DeclClass c = (DeclClass) abstractC;
            TreeSet<LabelMethod> tableEtiquettes = c.getTableEtiquette();
            lastClassdGB = c.getSuperClass().getClassDefinition().getdGB(); 
            compiler.addInstruction(new LEA(new RegisterOffset(lastClassdGB, Register.GB),R0)); 
            dGB = compiler.allocGB();
            compiler.addInstruction(new STORE(R0,new RegisterOffset(dGB, Register.GB)));
            c.getName().getClassDefinition().setdGB(dGB);
            for (Label label : tableEtiquettes){
                dGB = compiler.allocGB();
                compiler.addInstruction(new LOAD(new LabelOperand(label),R0));
                compiler.addInstruction(new STORE(R0,new RegisterOffset(dGB, Register.GB)));
            }
        }
    } */
    

    //extension
    public boolean foldConstants(DecacCompiler compiler){
        boolean changed = false;

        for (AbstractDeclClass declClass : getList()){
            changed |= declClass.foldConstants(compiler);
        } 
        return changed;
    }

    protected boolean eliminateDeadCode(DecacCompiler compiler){
        boolean changed = false;

        for (AbstractDeclClass declClass : getList()){
            changed |= declClass.eliminateDeadCode(compiler);
        } 
        return changed;
    }

    protected void collectReadVars(List<Set<Symbol>> readsClasses){
        readsClasses.clear();
        for (AbstractDeclClass declClass : getList()) {
            Set<Symbol> s = new HashSet<>();
            declClass.collectReadVars(s);
            readsClasses.add(s);
        }
    }

    protected boolean eliminateDeadStores(DecacCompiler compiler) {
        boolean changed = false;
        for (AbstractDeclClass c : getList()) {
            changed |= c.eliminateDeadStores(compiler);
        }
        return changed;
    }

    protected boolean eliminateUnusedVars(DecacCompiler compiler) {
        boolean changed = false;
        for (AbstractDeclClass c : getList()) {
            changed |= c.eliminateUnusedVars(compiler);
        }
        return changed;
    }




    // nouvellement intégré
    // passe 1 : table des méthodes de Object
    public void codeGenObject(DecacCompiler compiler) {
        ClassDefinition defObj = compiler.environmentType.OBJECT.getDefinition() ;

        Label[] etiqObj = defObj.getTabEtiq() ;
        if (etiqObj == null || etiqObj.length == 0 || etiqObj[0] == null) {
            throw new IllegalStateException("Object.tabEtiq not initialized (expected code.Object.equals)");
        }

        int baseOffsetObj = compiler.allocGBBlock(defObj.getNumberOfMethods() + 1);
        DAddr baseAddrObj = compiler.gbAddr(baseOffsetObj);
        defObj.setMethodTabAddr(baseAddrObj);


        GPRegister rTemp = GPRegister.R0;

        compiler.addInstruction(new LOAD(new NullOperand(), rTemp));
        compiler.addInstruction(new STORE(rTemp, compiler.gbAddr(baseOffsetObj)));

        compiler.addInstruction(new LOAD(new LabelOperand(etiqObj[0]), rTemp));
        compiler.addInstruction(new STORE(rTemp, compiler.gbAddr(baseOffsetObj + 1))) ;
    }

    public void codeGenClass(DecacCompiler compiler){
        compiler.addComment("Construction de la table des méthodes de Object");
        codeGenObject(compiler);
        for (AbstractDeclClass declClass : this.getList()){
            declClass.codeGenClass(compiler);

            // our débugguer
            DeclClass dc = (DeclClass) declClass;
            ClassDefinition def = dc.name.getClassDefinition();
            //System.out.println("[ADDR] " + dc.name.getName().getName()
            //    + " -> " + def.getMethodTabAddr());
        }
    }

}
