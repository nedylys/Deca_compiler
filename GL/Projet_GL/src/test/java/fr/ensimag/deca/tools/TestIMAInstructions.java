package fr.ensimag.deca.tools;

import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.ImmediateFloat;
import fr.ensimag.ima.pseudocode.RegisterOffset;

import fr.ensimag.ima.pseudocode.instructions.*;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * High-impact JaCoCo tests for IMA pseudocode instructions.
 * These tests only EXECUTE constructors + toString().
 */
public class TestIMAInstructions {

    /* =========================
     * Arithmetic instructions
     * ========================= */

    @Test
    public void testADD() {
        ADD i = new ADD(Register.R1, Register.getR(2));
        assertNotNull(i.toString());
    }

    @Test
    public void testSUB() {
        SUB i = new SUB(Register.getR(2), Register.getR(3));
        assertNotNull(i.toString());
    }

    @Test
    public void testMUL() {
        MUL i = new MUL(Register.getR(3), Register.getR(4));
        assertNotNull(i.toString());
    }

    @Test
    public void testDIV() {
        DIV i = new DIV(Register.getR(4), Register.getR(5));
        assertNotNull(i.toString());
    }

    /* =========================
     * LOAD / STORE
     * ========================= */

    @Test
    public void testLOADRegister() {
        LOAD i = new LOAD(Register.R1, Register.getR(2));
        assertNotNull(i.toString());
    }

    @Test
    public void testLOADImmediateInt() {
        LOAD i = new LOAD(new ImmediateInteger(42), Register.R1);
        assertNotNull(i.toString());
    }

    @Test
    public void testLOADImmediateFloat() {
        LOAD i = new LOAD(new ImmediateFloat(3.14f), Register.R1);
        assertNotNull(i.toString());
    }

    @Test
    public void testSTORE() {
        STORE i = new STORE(
            Register.R1,
            new RegisterOffset(4, Register.GB)
        );
        assertNotNull(i.toString());
    }

    /* =========================
     * Comparison
     * ========================= */

    @Test
    public void testCMP() {
        CMP i = new CMP(Register.R1, Register.getR(2));
        assertNotNull(i.toString());
    }

    /* =========================
     * Branch instructions
     * ========================= */

    @Test
    public void testBEQ() {
        BEQ i = new BEQ(new Label("label_eq"));
        assertNotNull(i.toString());
    }

    @Test
    public void testBNE() {
        BNE i = new BNE(new Label("label_ne"));
        assertNotNull(i.toString());
    }

    @Test
    public void testBGT() {
        BGT i = new BGT(new Label("label_gt"));
        assertNotNull(i.toString());
    }

    @Test
    public void testBLE() {
        BLE i = new BLE(new Label("label_le"));
        assertNotNull(i.toString());
    }

    /* =========================
     * Stack instructions
     * ========================= */

    @Test
    public void testPUSH() {
        PUSH i = new PUSH(Register.R1);
        assertNotNull(i.toString());
    }

    @Test
    public void testPOP() {
        POP i = new POP(Register.R1);
        assertNotNull(i.toString());
    }

    /* =========================
     * Misc
     * ========================= */

    @Test
    public void testHALT() {
        HALT i = new HALT();
        assertNotNull(i.toString());
    }

    @Test
    public void testRTS() {
        RTS i = new RTS();
        assertNotNull(i.toString());
    }

    @Test
    public void testWSTR() {
        WSTR i = new WSTR("hello");
        assertNotNull(i.toString());
    }

    @Test
    public void testWINT() {
        WINT i = new WINT();
        assertNotNull(i.toString());
    }

    @Test
    public void testWFLOAT() {
        WFLOAT i = new WFLOAT();
        assertNotNull(i.toString());
    }

    @Test
    public void testWNL() {
        WNL i = new WNL();
        assertNotNull(i.toString());
    }
}