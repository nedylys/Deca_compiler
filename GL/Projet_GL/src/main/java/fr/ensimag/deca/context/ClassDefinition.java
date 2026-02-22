package fr.ensimag.deca.context;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.tree.Location;
import fr.ensimag.ima.pseudocode.DAddr ;
import fr.ensimag.ima.pseudocode.Label ;

/**
 * Definition of a class.
 *
 * @author gl43
 * @date 01/01/2026
 */
public class ClassDefinition extends TypeDefinition {

    // Il s'agit d'un noeud qui ne leve pas d'erreur contextuelle, car il est créé lors de la déclaration de la classe
    // et que les erreurs liées à l'héritage, les champs et les méthodes sont gérées ailleurs.
    
    public void setNumberOfFields(int numberOfFields) {
        this.numberOfFields = numberOfFields;
    }

    public int getNumberOfFields() {
        return numberOfFields;
    }

    public int incNumberOfFields() {
        // I've modified tis method so itcould return the new numberOffields like incNumberOfMethods
        this.numberOfFields++;
        return numberOfFields;
    }

    public int getNumberOfMethods() {
        return numberOfMethods;
    }

    public void setNumberOfMethods(int n) {
        Validate.isTrue(n >= 0);
        numberOfMethods = n;
    }
    
    public int incNumberOfMethods() {
        numberOfMethods++;
        return numberOfMethods;
    }

    private int numberOfFields = 0;
    private int numberOfMethods = 1;
    
    @Override
    public boolean isClass() {
        return true;
    }
    
    @Override
    public ClassType getType() {
        // Cast succeeds by construction because the type has been correctly set
        // in the constructor.
        return (ClassType) super.getType();
    };

    public ClassDefinition getSuperClass() {
        return superClass;
    }

    private final EnvironmentExp members;
    private final ClassDefinition superClass; 
    private int dGB; // C'est l'entier k(GB) ou se trouve le pointeur des méthodes de la classe.

    public EnvironmentExp getMembers() {
        return members;
    }

    public ClassDefinition(ClassType type, Location location, ClassDefinition superClass) {
        super(type, location);
        EnvironmentExp parent;
        if (superClass != null) {
            parent = superClass.getMembers();
        } else {
            parent = null;
        }
        members = new EnvironmentExp(parent);
        this.superClass = superClass;
    }

    // La méthode retourne le k de la pile ou on trouve le pointeur de la Table des
    // méthodes de la classe
    public void setdGB(int kGB){
        dGB = kGB;
    }

    public int getdGB(){
        return dGB;
    }


    // nouvellement intégré
    // tableau des étiquettes des méthodes
    private Label[] tabEtiq ; 

    // addresse du début de la table des méthodes
    private DAddr tabAddrMethod ;

    public Label[] getTabEtiq() {
        return tabEtiq ;
    }

    public void setTabEtiq(Label[] tabEtiq) {
        this.tabEtiq = tabEtiq ;
    }

    public void setMethodTabAddr(DAddr tabAddrMethod) {
        this.tabAddrMethod = tabAddrMethod ;
    }

    public DAddr getMethodTabAddr() {
        return tabAddrMethod ;
    }
    
}
