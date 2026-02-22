package fr.ensimag.deca.context;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;

//import java.lang.instrument.ClassDefinition;
import java.util.HashMap;
import java.util.Map;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.tools.DecacInternalError;
import fr.ensimag.deca.tree.Location;
import fr.ensimag.ima.pseudocode.Label ;

// A FAIRE: étendre cette classe pour traiter la partie "avec objet" de Déca
/**
 * Environment containing types. Initially contains predefined identifiers, more
 * classes can be added with declareClass().
 *
 * @author gl43
 * @date 01/01/2026
 */
public class EnvironmentType {
    public EnvironmentType(DecacCompiler compiler) {
        envTypes = new HashMap<Symbol, TypeDefinition>();
        // environnment prédefinie : ne contient que int, boolean, float, void, et Object
        Symbol intSymb = compiler.createSymbol("int");
        INT = new IntType(intSymb);
        envTypes.put(intSymb, new TypeDefinition(INT, Location.BUILTIN));

        Symbol floatSymb = compiler.createSymbol("float");
        FLOAT = new FloatType(floatSymb);
        envTypes.put(floatSymb, new TypeDefinition(FLOAT, Location.BUILTIN));

        Symbol voidSymb = compiler.createSymbol("void");
        VOID = new VoidType(voidSymb);
        envTypes.put(voidSymb, new TypeDefinition(VOID, Location.BUILTIN));

        Symbol booleanSymb = compiler.createSymbol("boolean");
        BOOLEAN = new BooleanType(booleanSymb);
        envTypes.put(booleanSymb, new TypeDefinition(BOOLEAN, Location.BUILTIN));

        Symbol stringSymb = compiler.createSymbol("string");
        STRING = new StringType(stringSymb);
        // not added to envTypes, it's not visible for the user.

        //Type Object ajouté
        Symbol objectSymb = compiler.createSymbol("Object");
        OBJECT = new ClassType(objectSymb,Location.BUILTIN,null);
        envTypes.put(objectSymb, OBJECT.getDefinition());

        // nouvellement intégré
        // Ajout méthode builtin: boolean equals(Object)
        ClassDefinition objDef = OBJECT.getDefinition();
        Symbol equalsSymb = compiler.createSymbol("equals");

        Signature sig = new Signature();
        
        sig.add(OBJECT); // param de type Object

        MethodDefinition equalsDef =
            new MethodDefinition(BOOLEAN, Location.BUILTIN, sig, 1);

        try {
            objDef.getMembers().declare(equalsSymb, equalsDef);
        } 
        catch (EnvironmentExp.DoubleDefException e) {
            throw new DecacInternalError("Object.equals already declared");
        }

        objDef.setNumberOfMethods(1);

        // tableau des étiquettes de Object
        Label[] objEtiq = new Label[1];
        objEtiq[0] = new Label("code.Object.equals");
        objDef.setTabEtiq(objEtiq);



        //Type Null ajouté
        Symbol nullSymb = compiler.createSymbol("null");
        NULL = new NullType(nullSymb);
        envTypes.put(nullSymb, new TypeDefinition(NULL, Location.BUILTIN));
        
    }

    private final Map<Symbol, TypeDefinition> envTypes;

    public TypeDefinition defOfType(Symbol s) {
        return envTypes.get(s);
    }

    // Méthode ajoutée
    public void declareClass(Symbol s, ClassDefinition def) throws DoubleDefException {  // à discuter à propos du type de l'erreur ici
        if(envTypes.containsKey(s)){
            throw new DoubleDefException();
        }
        envTypes.put(s,def);
    }
    //

    public final VoidType    VOID;
    public final IntType     INT;
    public final FloatType   FLOAT;
    public final StringType  STRING;
    public final BooleanType BOOLEAN;
    // Attributs ajoutés
    public final ClassType OBJECT;
    public final NullType NULL;
}
