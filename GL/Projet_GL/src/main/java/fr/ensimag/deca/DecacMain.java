package fr.ensimag.deca;

import java.io.File;

import org.apache.log4j.Logger;

import fr.ensimag.deca.tree.LocationException;

/**
 * Main class for the command-line Deca compiler.
 *
 * @author gl43
 * @date 01/01/2026
 */
public class DecacMain {
    private static Logger LOG = Logger.getLogger(DecacMain.class);
    
    public static void main(String[] args) {
        // example log4j message.

        boolean error = false;
        final CompilerOptions options = new CompilerOptions();

        try {
            options.parseArgs(args);
        } catch (CLIException e) {
            System.err.println("Error during option parsing:\n"
                    + e.getMessage());
            //options.displayUsage();
            System.exit(1);
        }
        if (options.getPrintBanner()) {
            // On le permet juste pour executer le basic_decac
            /* throw new UnsupportedOperationException("decac -b not yet implemented"); */
            System.out.println("Deca compiler - équipe gl43"); // Nom de l'équipe
            System.exit(0); // exit avec succés
        }

        if (!options.getParse() && !options.getVerification()) {
            LOG.info("Decac compiler started");
        }



        if (options.getSourceFiles().isEmpty()) {
            // Il faut la remettre pour que decac sans argument affiche un message d'erreur mais je veux la négliger maintenant
            /* throw new UnsupportedOperationException("decac without argument not yet implemented"); */
        }
        if (options.getParallel()) {
            // A FAIRE : instancier DecacCompiler pour chaque fichier à
            // compiler, et lancer l'exécution des méthodes compile() de chaque
            // instance en parallèle. Il est conseillé d'utiliser
            // java.util.concurrent de la bibliothèque standard Java.
            // throw new UnsupportedOperationException("Parallel build not yet implemented");
            LOG.warn("-P ignored (parallel not implemented)");
        } else {
            for (File source : options.getSourceFiles()) {
                DecacCompiler compiler = new DecacCompiler(options, source);
                try {
                    // -d is managed through Logger.getRootLogger().setLevel() in CompilerOptions.java
                    if (options.getParse()) {
                        compiler.prettyPrint(System.out);
                        //System.exit(0);
                        break; 
                    } else if (options.getVerification()) {
                        compiler.verification();
                        //System.exit(0);
                        break ;

                    } else {
                        // if no option is given
                        compiler.compile();
                    }

                } catch (LocationException e) {
                    // LocationException, we must diplay the location of the error given by setLocation
                    e.display(System.err);
                    error = true;
                } catch (DecacFatalError e) {
                    // DcacFatalError, we must stop the programm with nothing to track as it is not a coding error
                    System.err.println(e.getMessage());
                    error = true;
                } catch (Exception e) {
                    // internal error, for debugging purposes we must dipslay track
                    e.printStackTrace();
                    error = true;
                }
        }
            }
            
        
        System.exit(error ? 1 : 0);
    }
}