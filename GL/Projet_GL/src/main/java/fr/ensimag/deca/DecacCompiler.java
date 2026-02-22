package fr.ensimag.deca;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.log4j.Logger;

import fr.ensimag.deca.codegen.ErrorManager.ErrorType;
import fr.ensimag.deca.codegen.RegisterManager;
import fr.ensimag.deca.codegen.StackManager;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentType;
import fr.ensimag.deca.optim.ConstantFolder;
import fr.ensimag.deca.optim.PeepHoleOptimizer;
import fr.ensimag.deca.optim.DeadCodeEliminator;
import fr.ensimag.deca.syntax.DecaLexer;
import fr.ensimag.deca.syntax.DecaParser;
import fr.ensimag.deca.tools.DecacInternalError;
import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.tree.AbstractProgram;
import fr.ensimag.deca.tree.LocationException;
import fr.ensimag.ima.pseudocode.AbstractLine;
import fr.ensimag.ima.pseudocode.IMAProgram;
import fr.ensimag.ima.pseudocode.Instruction;
//// Optimization imports +++ : MEA
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Line;
import fr.ensimag.deca.codegen.RegisterManager;
import fr.ensimag.deca.codegen.StackManager;
//// Optimization imports +++ : MEA
import fr.ensimag.deca.optim.ConstantFolder;
import fr.ensimag.deca.optim.ConstantPropagationOptimizer;
import fr.ensimag.deca.optim.CopyPropagationOptimizer;
import fr.ensimag.deca.optim.DeadStoreOptimizer;
import fr.ensimag.deca.optim.PeepHoleOptimizer;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.Register ;
import fr.ensimag.ima.pseudocode.RegisterOffset;

/**
 * Decac compiler instance.
 *
 * This class is to be instantiated once per source file to be compiled. It
 * contains the meta-data used for compiling (source file name, compilation
 * options) and the necessary utilities for compilation (symbol tables, abstract
 * representation of target file, ...).
 *
 * It contains several objects specialized for different tasks. Delegate methods
 * are used to simplify the code of the caller (e.g. call
 * compiler.addInstruction() instead of compiler.getProgram().addInstruction()).
 *
 * @author gl43
 * @date 01/01/2026
 */
public class DecacCompiler {
    private static final Logger LOG = Logger.getLogger(DecacCompiler.class);
    
    /**
     * Portable newline character.
     */
    private static final String nl = System.getProperty("line.separator", "\n");

    public DecacCompiler(CompilerOptions compilerOptions, File source) {
        super();
        if (compilerOptions == null) {
            this.compilerOptions = new CompilerOptions();
            this.compilerOptions.setNbOfRegisters(16); }
        else{
            this.compilerOptions = compilerOptions;
        }

        this.source = source;

        int x = this.compilerOptions.getNbOfRegisters() ;
        this.regManager = new RegisterManager(x - 1) ;

        this.stackManager = new StackManager() ;
    }

    /**
     * Source file associated with this compiler instance.
     */
    public File getSource() {
        return source;
    }

    /**
     * Compilation options (e.g. when to stop compilation, number of registers
     * to use, ...).
     */
    public CompilerOptions getCompilerOptions() {
        return compilerOptions;
    }

    /**
     * @see
     * fr.ensimag.ima.pseudocode.IMAProgram#add(fr.ensimag.ima.pseudocode.AbstractLine)
     */
    public void add(AbstractLine line) {
        program.add(line);
    }

    public void remove(AbstractLine line){
        program.remove(line);
    }

    /**
     * @see fr.ensimag.ima.pseudocode.IMAProgram#addComment(java.lang.String)
     */
    public void addComment(String comment) {
        program.addComment(comment);
    }

    /**
     * @see
     * fr.ensimag.ima.pseudocode.IMAProgram#addLabel(fr.ensimag.ima.pseudocode.Label)
     */
    public void addLabel(Label label) {
        program.addLabel(label);
    }

    /**
     * @see
     * fr.ensimag.ima.pseudocode.IMAProgram#addInstruction(fr.ensimag.ima.pseudocode.Instruction)
     */
    public void addInstruction(Instruction instruction) {
        program.addInstruction(instruction);
    }

    /**
     * @see
     * fr.ensimag.ima.pseudocode.IMAProgram#addInstruction(fr.ensimag.ima.pseudocode.Instruction,
     * java.lang.String)
     */
    public void addInstruction(Instruction instruction, String comment) {
        program.addInstruction(instruction, comment);
    }

    /* Rajouter les commentaires de la javadoc plus tard +++ : MEA */
    public void addFirst(Line l) {
        program.addFirst(l);
    }

    public void addFirst(Instruction i) {
        program.addFirst(i);
    }

    public void addFirst(Instruction i, String comment) {
        program.addFirst(i, comment);
    }

    /**
     * @see 
     * fr.ensimag.ima.pseudocode.IMAProgram#display()
     */
    public String displayIMAProgram() {
        return program.display();
    }
    
    private final CompilerOptions compilerOptions;
    private final File source;
    /**
     * The main program. Every instruction generated will eventually end up here.
     */
    private final IMAProgram program = new IMAProgram();

    /**
     * cette partie (de compteur) a été utilisée dans la classe AND.java et Or.java pour la génération de code (Labels) - Etape Sans objet : 03/01 par AT MEA
     */
    // Compteur pour générer des étiquettes uniques
    private int labelCounter = 0;

    public int getLabelCounter() {
        return labelCounter++;
    }
 

    /** The global environment for types (and the symbolTable) */
    public final SymbolTable symbolTable = new SymbolTable();
    public final EnvironmentType environmentType = new EnvironmentType(this); // Public donc pas besoin de getter
    // public final SymbolTable symbolTable = new SymbolTable();

    public Symbol createSymbol(String name) {
        //return null; // A FAIRE: remplacer par la ligne en commentaire ci-dessous
        return symbolTable.create(name);
    }

    /**
     * Run the compiler (parse source file, generate code)
     *
     * @return true on error
     */
    public boolean compile() {
        // String sourceFile = source.getAbsolutePath();
        // String destFile = null;
        // A FAIRE: calculer le nom du fichier .ass à partir du nom du
        // A FAIRE: fichier .deca.

        PrintStream err = System.err;
        PrintStream out = System.out;
        boolean error =  false;
        //partie ajoutée
/*         for (File source : compilerOptions.getSourceFiles()) { */
        String sourceName = source.getAbsolutePath();

        // construire le nom du fichier .ass
        String destName;
        if (sourceName.endsWith(".deca")) {
            destName = sourceName.substring(0, sourceName.length() - 5) + ".ass";
        } else {
            destName = sourceName + ".ass";
        }

        LOG.debug("Compiling file " + sourceName + " to assembly file " + destName);
        try {
            error |= doCompile(sourceName, destName, out, err);
        } catch (LocationException e) {
            e.display(err);
            error = true;
        } catch (DecacFatalError e) {
            err.println(e.getMessage());
            error = true;
        } catch (StackOverflowError e) {
            LOG.debug("stack overflow", e);
            err.println("Stack overflow while compiling file " + sourceName + ".");
            error = true;
        } catch (Exception e) {
            LOG.fatal("Exception raised while compiling file " + sourceName
                    + ":", e);
            err.println("Internal compiler error while compiling file " + sourceName + ", sorry.");
            error = true;
        } catch (AssertionError e) {
            LOG.fatal("Assertion failed while compiling file " + sourceName
                    + ":", e);
            err.println("Internal compiler error while compiling file " + sourceName + ", sorry.");
            error = true;
        //}
        }

        // fin partie ajoutée
        // LOG.debug("Compiling file " + sourceFile + " to assembly file " + destFile);
        // try {
        //     return doCompile(sourceFile, destFile, out, err);
        // } catch (LocationException e) {
        //     e.display(err);
        //     return true;
        // } catch (DecacFatalError e) {
        //     err.println(e.getMessage());
        //     return true;
        // } catch (StackOverflowError e) {
        //     LOG.debug("stack overflow", e);
        //     err.println("Stack overflow while compiling file " + sourceFile + ".");
        //     return true;
        // } catch (Exception e) {
        //     LOG.fatal("Exception raised while compiling file " + sourceFile
        //             + ":", e);
        //     err.println("Internal compiler error while compiling file " + sourceFile + ", sorry.");
        //     return true;
        // } catch (AssertionError e) {
        //     LOG.fatal("Assertion failed while compiling file " + sourceFile
        //             + ":", e);
        //     err.println("Internal compiler error while compiling file " + sourceFile + ", sorry.");
        //     return true;
        // }
        return error;
    }

    /**
     * Internal function that does the job of compiling (i.e. calling lexer,
     * verification and code generation).
     *
     * @param sourceName name of the source (deca) file
     * @param destName name of the destination (assembly) file
     * @param out stream to use for standard output (output of decac -p)
     * @param err stream to use to display compilation errors
     *
     * @return true on error
     */
    private boolean doCompile(String sourceName, String destName,
            PrintStream out, PrintStream err)
            throws DecacFatalError, LocationException {
        AbstractProgram prog = doLexingAndParsing(sourceName, err);

        if (prog == null) {
            LOG.info("Parsing failed");
            return true;
        }
        assert(prog.checkAllLocations());

        prog.verifyProgram(this);
        assert(prog.checkAllDecorations());

        
        if (compilerOptions.getOptimize()) {
            // ++++++++ j'ai commenté ceci en attendant que ça soit corrigé +++++++

            ConstantFolder constantfolder = new ConstantFolder();
            constantfolder.apply(this, prog);
            DeadCodeEliminator deadcodeeliminator =  new DeadCodeEliminator();
            deadcodeeliminator.apply(this,prog);
        } 
              

        addComment("start main program");
        prog.codeGenProgram(this);

         
       addComment("end main program");

       
        if (compilerOptions.getOptimize()) {
            //++++++++++++++++++ Optimizations ++++++++++++++++++

            // ++++++++++++++++++++++++++++++++++++++++++

            if (prog.getIsOOP() == false){
                // ++++++++++++ CopyPrpagation ++++++++++++++
                CopyPropagationOptimizer copyPropagationOptimizer = new CopyPropagationOptimizer();
                copyPropagationOptimizer.optimize(program);
                // +++++++++++++ ConstantPropagation +++++++++++++++
                ConstantPropagationOptimizer constantPropagationOptimizer = new ConstantPropagationOptimizer();
                constantPropagationOptimizer.optimize(program);}
            // ++++++++++++++++++++++++++++++++++++++++++
            // ++++++++++++ Peephole +++++++++++++++++
            PeepHoleOptimizer peepholeOptimizer = new PeepHoleOptimizer(!compilerOptions.getNoCheck());
            peepholeOptimizer.optimize(program);
            // ++++++++++++++++++++++++++++++++++++++
            // ++++++++++++ DeadStore +++++++++++++++
            if (prog.getIsOOP() == false){
                DeadStoreOptimizer deadStoreOptimizer = new DeadStoreOptimizer();
                deadStoreOptimizer.optimize(program);
                peepholeOptimizer.optimize(program);
            }
            

        } 
        

        LOG.debug("Generated assembly code:" + nl + program.display());
        LOG.info("Output file assembly file is: " + destName);

        FileOutputStream fstream = null;
        try {
            fstream = new FileOutputStream(destName);
        } catch (FileNotFoundException e) {
            throw new DecacFatalError("Failed to open output file: " + e.getLocalizedMessage());
        }


        LOG.info("Writing assembler file ...");

        program.display(new PrintStream(fstream));
        LOG.info("Compilation of " + sourceName + " successful.");
        return false;
    }

    /**
     * Build and call the lexer and parser to build the primitive abstract
     * syntax tree.
     *
     * @param sourceName Name of the file to parse
     * @param err Stream to send error messages to
     * @return the abstract syntax tree
     * @throws DecacFatalError When an error prevented opening the source file
     * @throws DecacInternalError When an inconsistency was detected in the
     * compiler.
     * @throws LocationException When a compilation error (incorrect program)
     * occurs.
     */
    protected AbstractProgram doLexingAndParsing(String sourceName, PrintStream err)
            throws DecacFatalError, DecacInternalError {
        DecaLexer lex;
        try {
            lex = new DecaLexer(CharStreams.fromFileName(sourceName));
        } catch (IOException ex) {
            throw new DecacFatalError("Failed to open input file: " + ex.getLocalizedMessage());
        }
        lex.setDecacCompiler(this);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        DecaParser parser = new DecaParser(tokens);
        parser.setDecacCompiler(this);
        return parser.parseProgramAndManageErrors(err);
    }

    /* GB counter : MEA */
    private int gbCounter = 1 ;     // 1ère case libre en GB
    public int allocGB() {
        return gbCounter++  ;
    }
    public int getNbGBVars() {
        return gbCounter - 1 ; 
    }

    // ++++++++++++++++  -p   ++++++++++++++++++++
    public void prettyPrint(PrintStream s)  throws DecacFatalError {
        AbstractProgram prog = doLexingAndParsing(source.getAbsolutePath(), System.err);
        if (prog == null) {
            throw new DecacFatalError("Parsing failed");
        }

        prog.decompile(s);
        s.println();

    }

    // ++++++++++++++++  -v   ++++++++++++++++++++
    public void verification() throws DecacFatalError, ContextualError {
        AbstractProgram prog = doLexingAndParsing(source.getAbsolutePath(), System.err);
        if (prog == null) {
            return;
        }
        assert(prog.checkAllLocations());
        prog.verifyProgram(this);
        assert(prog.checkAllDecorations());

    }
    
    private final RegisterManager regManager ;
    public RegisterManager getRegManager() {
        return regManager ;
    }

    private final StackManager stackManager ;
    public StackManager getStackManager() {
        return stackManager ;
    }

    private final Set<ErrorType> usedErrors = EnumSet.noneOf(ErrorType.class) ;

    public void useError(ErrorType type) {
        usedErrors.add(type);
    }

    public Set<ErrorType> getUsedErrors() {
        return usedErrors ;
    }

    public boolean isNoCheck() {
        return getCompilerOptions().getNoCheck();
    }
    


    // nouvellement intégré
    /* Alloue un bloc contigu de n cases en GB et renvoie
    l'offset de la première case */
    public int allocGBBlock(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("allocGBBlock : n has to b > 0") ;
        }
        int firstGBOffset = gbCounter ;
        gbCounter += n ;
        return firstGBOffset ;
    }

    /* Construit une adresse GB à partir d'un offsset */
    public DAddr gbAddr(int offset) {
        return new RegisterOffset(offset, Register.GB) ;
    }
    



    // pour tester un truc 
    private Label currentMethodEndLabel = null;

    public void setCurrentMethodEndLabel(Label l) {
        this.currentMethodEndLabel = l;
    }

    public Label getCurrentMethodEndLabel() {
        return currentMethodEndLabel;
    }
}