package fr.ensimag.deca.context;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class TestTypeCompatibility {

    @Test
    public void intToFloatCompatible() {
        Type intT = new IntType(null);
        Type floatT = new FloatType(null);

        assertTrue(floatT.assignCompatible(intT));
    }

    @Test
    public void floatToIntNotCompatible() {
        Type intT = new IntType(null);
        Type floatT = new FloatType(null);

        assertFalse(intT.assignCompatible(floatT));
    }
}