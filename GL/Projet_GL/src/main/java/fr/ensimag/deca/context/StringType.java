package fr.ensimag.deca.context;

import fr.ensimag.deca.tools.DecacInternalError;
import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.ima.pseudocode.DVal;

/**
 *
 * @author Ensimag
 * @date 01/01/2026
 */
public class StringType extends Type {

    public StringType(SymbolTable.Symbol name) {
        super(name);
    }

    @Override
    public boolean isString() {
        return true;
    }

    // F j'ai implémenté cette méthode : avec tout les autres sameType
    @Override
    public boolean sameType(Type otherType) {
        //throw new UnsupportedOperationException("not yet implemented");
        return otherType.isString();
    }


}
