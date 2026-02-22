package fr.ensimag.deca;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * User-specified options influencing the compilation.
 *
 * @author gl43
 * @date 01/01/2026
 */
public class CompilerOptions {
    public static final int QUIET = 0;
    public static final int INFO  = 1;
    public static final int DEBUG = 2;
    public static final int TRACE = 3;
    public int getDebug() {
        return debug;
    }

    public boolean getParallel() {
        return parallel;
    }

    public boolean getPrintBanner() {
        return printBanner;
    }

    public boolean getParse() {
        return parse;
    }
    
    public int getNbOfRegisters() {
        return nbOfRegisters;
    }

    public boolean getVerification() {
        return verification;
    }

    public boolean getWarnings() {
        return warnings;
    }

    public boolean getNoCheck() {
        return noCheck;
    }

    public boolean getOptimize() {
        return optimize;
    }


    public List<File> getSourceFiles() {
        return Collections.unmodifiableList(sourceFiles);
    }

    private boolean parseIsSet = false;
    private boolean verificationIsSet = false;
    private boolean parallelIsSet = false;
    private boolean warningsIsSet = false;
    private boolean bannerIsSet = false;
    private boolean nbOfRegistersIsSet = false;
    private boolean noCheckIsSet = false;
    private boolean noCheck = false;
    private boolean optimize = false;
    private boolean optimizeIsSet = false;




    private int debug = 0;
    private int nbOfRegisters = 16;
    private boolean parallel = false;
    private boolean printBanner = false;
    private boolean parse = false;
    private boolean verification = false;
    private boolean warnings = false;
    private List<File> sourceFiles = new ArrayList<File>();

    
    public void parseArgs(String[] args) throws CLIException {
        // A FAIRE : parcourir args pour positionner les options correctement.
        if (args.length == 0) {
            //throw new CLIException("No input file nor Option is provided");
            displayUsage();
            return;
        }
        for (int i = 0; i < args.length; i++) {

            String arg = args[i];
            if (arg.equals("-b")) {
               /*  Poly: L’option '-b' ne peut être utilisée que sans autre option, et sans fichier source. Dans ce cas, decac
termine après avoir affiché la bannière. */
                /*On va le permettre maintenant, juste pour pouvoir lancer  basic_decac*/
                if (bannerIsSet) {
                    throw new CLIException("Option -b cannot be used more than once");
                    }
                bannerIsSet = true;
                printBanner = true; // Banner c'est pour autheur, nom etc ..
                continue; // Otherwise we will get a CLIException
        }
            if (arg.equals("-p")) {
                if (parseIsSet) {
                    throw new CLIException("Option -p cannot be used more than once");
                    }
                parseIsSet = true;
                parse = true;
                continue;
            }

            if (arg.equals("-v")) {
                if (verificationIsSet) {
                    throw new CLIException("Option -v cannot be used more than once");
                    }
                verificationIsSet = true;
                verification = true;
                continue;
            }

            if (arg.equals("-r")){
                if (nbOfRegistersIsSet){
                    // -r 6 -r 7
                    throw new CLIException("-r cannot be used more than once");
                }
                

                if (i + 1 >= args.length){
                    throw new CLIException("-r Must be followed by an integer");
                }

                else{
                    try{
                        nbOfRegisters = Integer.parseInt(args[i + 1]);
                        i++;
                        nbOfRegistersIsSet = true;
                    }
                    
                    catch (NumberFormatException e) {
                        throw new CLIException("-r Must be followed by an integer");
                    }
                    
                    if (nbOfRegisters < 4 || nbOfRegisters > 16) {
                        // That is because R0 and R1 are already reserved and we need at least 2 more registers for various operations
                        throw new CLIException("Invalid number of registers, must be between 4 and 16");
                    }

                    continue;
                }

            }

            if (arg.matches("-d")){
                debug ++ ;
                continue;
            }
            
            if (arg.equals("-P")) {
                if (parallelIsSet) {
                    throw new CLIException("Option -P cannot be used more than once");
                    }
                parallelIsSet = true;
                parallel = true;
                continue;
            }

            if (arg.equals("-w")) {
                if (warningsIsSet) {
                    throw new CLIException("Option -w cannot be used more than once");
                    }
                warningsIsSet = true;
                warnings = true;
                continue;
            }

            if (arg.equals("-n")) {
                if (noCheckIsSet) {
                    throw new CLIException("Option -n cannot be used more than once");
                }
                noCheckIsSet = true;
                noCheck = true;
                continue;
            }

            if (arg.equals("-o")) {
                if (optimizeIsSet) {
                    throw new CLIException("Option -o cannot be used more than once");
                }
                optimizeIsSet = true;
                optimize = true;
                continue;
            }


            if (arg.startsWith("-")) {
                
                throw new CLIException("Options not supported yet: " + arg);
            } else {
                File f = new File(arg);
                if (!f.exists()) {
                    throw new CLIException("File not found: " + arg);
                }
                sourceFiles.add(f);
            }


    }
        if (printBanner) {
            if (!sourceFiles.isEmpty() ||parse || verification || parallel || warnings || debug > 0 ) {
                throw new CLIException("-b must be used alone, without a file or other options");
            }
            else {
                return;
            }
        }

        if (sourceFiles.isEmpty() /* && !printBanner */){
            // printBanner doesn't require a file
            throw new CLIException("No input file provided");
        }





        if (parse && verification) {
            throw new CLIException("Options -p and -v are incompatibles");
        }
        
        Logger logger = Logger.getRootLogger();
        // map command-line debug option to log4j's level.
        int d = Math.min(getDebug(), TRACE) ;
        switch (d) {
        case QUIET: break; // keep default
        case INFO:
            logger.setLevel(Level.INFO); break;
        case DEBUG:
            logger.setLevel(Level.DEBUG); break;
        case TRACE:
            logger.setLevel(Level.TRACE); break;
        /* default:
            logger.setLevel(Level.ALL); break; */
        }
        logger.info("Application-wide trace level set to " + logger.getLevel());

        boolean assertsEnabled = false;
        assert assertsEnabled = true; // Intentional side effect!!!
        if (assertsEnabled) {
            logger.info("Java assertions enabled");
        } else {
            logger.info("Java assertions disabled");
        }

        //throw new UnsupportedOperationException("not yet implemented");
        // for (String arg : args) {
        //     sourceFiles.add(new File(arg));
        // }

        
    }

    public void setNbOfRegisters(int nbOfRegisters) {
        this.nbOfRegisters = nbOfRegisters;
    }

    protected void displayUsage() {
        //throw new UnsupportedOperationException("not yet implemented");
        System.err.println(
            "Usage: decac [options] <source files>\n" +
            "Options:\n" +
            "  -b        Print banner\n" +
            "  -p        Stop after parsing and print decompiled program\n" +
            "  -v        Stop after contextual verification\n" +
            "  -n        Disable runtime checks\n" +
            "  -r X      Limit number of registers to X (4 <= X <= 16)\n" +
            "  -d        Enable debug traces\n" +
            "  -P        Parallel compilation (optional)\n"
        );
    }
}
