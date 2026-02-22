package fr.ensimag.deca.context;

import fr.ensimag.deca.tree.Location;

/**
 * Definition of a variable.
 *
 * @author gl43
 * @date 01/01/2026
 */
public class VariableDefinition extends ExpDefinition {

    private boolean initialized = false;
    public VariableDefinition(Type type, Location location) {
        super(type, location);
    }

    @Override
    public String getNature() {
        return "variable";
    }

    @Override
    public boolean isExpression() {
        return true;
    }

    public boolean isInitialized(){
        return initialized;
    }

    public void setInitialized(boolean initialized){
        this.initialized = initialized;
    }
}
