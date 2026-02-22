package fr.ensimag.deca.context;

import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.context.ExpDefinition;
import java.util.HashMap;



/**
 * Dictionary associating identifier's ExpDefinition to their names.
 * 
 * This is actually a linked list of dictionaries: each EnvironmentExp has a
 * pointer to a parentEnvironment, corresponding to superblock (eg superclass).
 * 
 * The dictionary at the head of this list thus corresponds to the "current" 
 * block (eg class).
 * 
 * Searching a definition (through method get) is done in the "current" 
 * dictionary and in the parentEnvironment if it fails. 
 * 
 * Insertion (through method declare) is always done in the "current" dictionary.
 * 
 * @author gl43
 * @date 01/01/2026
 */
public class EnvironmentExp {
    // A FAIRE : implémenter la structure de donnée représentant un
    // environnement (association nom -> définition, avec possibilité
    // d'empilement).

    private HashMap<Symbol, ExpDefinition> env = new HashMap<>();
    EnvironmentExp parentEnvironment;
    
    public EnvironmentExp(EnvironmentExp parentEnvironment) {
        this.parentEnvironment = parentEnvironment;
    }

    public static class DoubleDefException extends Exception {
        private static final long serialVersionUID = -2733379901827316441L;
    }

    /**
     * Return the definition of the symbol in the environment, or null if the
     * symbol is undefined.
     */


    // F j'ai implémenté ça
    public ExpDefinition get(Symbol key) {
        //throw new UnsupportedOperationException("not yet implemented");
        if (this.env.containsKey(key)){
            return this.env.get(key);
        }
        // mais il y a aussi les symbol du niveau parent qui sont aussi visible à this.env (et le parent de parent ...)
        else if (this.parentEnvironment != null){
            return this.parentEnvironment.get(key); 
        }
        else {
            return null;
        }
    }


    /**
     * Add the definition def associated to the symbol name in the environment.
     * 
     * Adding a symbol which is already defined in the environment,
     * - throws DoubleDefException if the symbol is in the "current" dictionary 
     * - or, hides the previous declaration otherwise.
     * 
     * @param name
     *            Name of the symbol to define
     * @param def
     *            Definition of the symbol
     * @throws DoubleDefException
     *             if the symbol is already defined at the "current" dictionary
     *
     */

    // F j'ai implémenté ça
    public void declare(Symbol name, ExpDefinition def) throws DoubleDefException {
        //throw new UnsupportedOperationException("not yet implemented");
        if (this.env.containsKey(name)){
            throw new DoubleDefException(); // ici on déclare le doublant dans le meme niveau pour que verifyXXX retourne la contextual error correspondante
        }
        else {
            this.env.put(name, def); // du coup il n y a pas de doublants
        }
    }

}
