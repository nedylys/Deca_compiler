package fr.ensimag.deca.context;

import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.tree.Location;

/**
 * Deca Type (internal representation of the compiler)
 *
 * @author gl43
 * @date 01/01/2026
 */

public abstract class Type {


    /**
     * True if this and otherType represent the same type (in the case of
     * classes, this means they represent the same class).
     */
    public abstract boolean sameType(Type otherType);

    private final Symbol name;

    public Type(Symbol name) {
        this.name = name;
    }

    public Symbol getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName().toString();
    }

    public boolean isClass() {
        return false;
    }

    public boolean isInt() {
        return false;
    }

    public boolean isFloat() {
        return false;
    }

    public boolean isBoolean() {
        return false;
    }

    public boolean isVoid() {
        return false;
    }

    public boolean isString() {
        return false;
    }

    public boolean isNull() {
        return false;
    }

    public boolean isClassOrNull() {
        return false;
    }

    public boolean isSubTypeOf(Type otherType){
        // I need this method to check the subTypes in DeclMethod.java
        return this.sameType(otherType);
    }

    public boolean assignCompatible(Type otherRight){
        // rule 3.28 ...

        //if B extends A then a B type is an A type hince A l = new B() is valid
        if (otherRight.isSubTypeOf(this)){
            return true;
        }

        if(this.isFloat() && otherRight.isInt()){
            return true;
        }

        if (this.isClass() && otherRight.isNull()){
            return true;
        }

        return false;

    }

    /**
     * Returns the same object, as type ClassType, if possible. Throws
     * ContextualError(errorMessage, l) otherwise.
     *
     * Can be seen as a cast, but throws an explicit contextual error when the
     * cast fails.
     */
    public ClassType asClassType(String errorMessage, Location l)
            throws ContextualError {
        throw new ContextualError(errorMessage, l);
    }

    public boolean isCastCompatible(Type otherType) {
        //return false;
        if(this.sameType(otherType)){
            return true;
        }
        if(this.isInt() && otherType.isFloat() || this.isFloat() && otherType.isInt()){
            return true;
        }

        else if (this.isClass() && otherType.isNull()){
            return true;
        }

        else if (this.isClass() && otherType.isClass()){
            return this.isSubTypeOf(otherType) || otherType.isSubTypeOf(this);
        }
        return false;
    }

}
