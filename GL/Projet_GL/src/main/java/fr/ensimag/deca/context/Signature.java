package fr.ensimag.deca.context;

import java.util.ArrayList;
import java.util.List;

/**
 * Signature of a method (i.e. list of arguments)
 *
 * @author gl43
 * @date 01/01/2026
 */
public class Signature {
    List<Type> args = new ArrayList<Type>(); // package-pria=vate par défaut

    public void add(Type t) {
        args.add(t);
    }
    
    public Type paramNumber(int n) {
        return args.get(n);
    }
    
    public int size() {
        return args.size();
    }

    public Type get(int i){
        return args.get(i);
    }

    @Override
    public boolean equals(Object other){
        // I need This method in DeclMethod.java
        if (!(other instanceof Signature)){
            return false;
        }

        Signature othersig = (Signature) other;

        if (this.size() != othersig.size()){
            return false;
        }

        for (int i = 0; i < size(); i++) {
            if (!this.args.get(i).sameType(othersig.args.get(i))) {
                return false;
            }
        }
        return true;

    }
}