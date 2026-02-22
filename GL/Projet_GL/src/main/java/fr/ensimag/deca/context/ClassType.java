package fr.ensimag.deca.context;

import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.tree.Location;
import org.apache.commons.lang.Validate;

/**
 * Type defined by a class.
 *
 * @author gl43
 * @date 01/01/2026
 */
public class ClassType extends Type {
    
    protected ClassDefinition definition ;
    
    public ClassDefinition getDefinition() {
        return this.definition;
    }
            
    @Override
    public ClassType asClassType(String errorMessage, Location l) {
        return this;
    }

    @Override
    public boolean isClass() {
        return true;
    }

    @Override
    public boolean isClassOrNull() {
        return true;
    }

    /**
     * Standard creation of a type class.
     */
    public ClassType(Symbol className, Location location, ClassDefinition superClass) {
        super(className);
        this.definition = new ClassDefinition(this, location, superClass);
    }

    /**
     * Creates a type representing a class className.
     * (To be used by subclasses only)
     */
    protected ClassType(Symbol className) {
        super(className);
    }
    

    @Override
    public boolean sameType(Type otherType) {
        if (!otherType.isClass()) {
        return false;
    }
        return this.getDefinition() == ((ClassType) otherType).getDefinition();
    }

    /**
     * Return true if potentialSuperClass is a superclass of this class.
     */
    // Méthode modifiée
/*     public boolean isSubClassOf(ClassType potentialSuperClass) {
        //throw new UnsupportedOperationException("not yet implemented"); 
        
        return this.isSubTypeOf(potentialSuperClass);

    }
 */
    @Override
    public boolean isSubTypeOf(Type other) {
        if (this.sameType(other)) { // Same Class
            return true;
        }


        if (!other.isClass()) {
            // Imagine doing int func as an override to AbstractDeclVar func
            return false;
        }

        ClassDefinition currentClass = this.getDefinition().getSuperClass();

        ClassDefinition otherClass= ((ClassType) other).getDefinition();

        // Maybe our class is the daughter or granddaughter or grand granddaughter or grand grand granddaughter or grand grand grand granddaughter or grand grand grand grand granddaughter or grand grand grand grand grand granddaughter of otherClass (DNA test let's gooooo)
        // I'm sorry if you did read the whole comment above
        while (currentClass != null) {
            if (currentClass == otherClass) {
                // Okey so the class isn't adopted
                return true;
            }
            currentClass = currentClass.getSuperClass();
        }
        // Never mind it is, well as lo,ng as they love it as they would love there biological class-daughter
        return false;
    }



}
