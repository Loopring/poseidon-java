package com.loopring.poseidon;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.math.BigInteger;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue()
    {
        assertTrue( true );
    }

    @Test
    public void checkHHashValue() {
        BigInteger a = BigInteger.ONE.add(BigInteger.ONE);
        BigInteger b = PoseidonParamsType.H(a.pow(256).subtract(BigInteger.ONE));
        for (byte v : b.toByteArray()) {
            System.out.printf("%02x", v);
        }
        System.out.println("\n------------------------------------");

        BigInteger inputH = new BigInteger(1, new byte[]{-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1});
        BigInteger outH = PoseidonParamsType.H(inputH);
        for (byte v : outH.toByteArray()) {
            System.out.printf("%02x", v);
        }
        System.out.println("\noutH = " + outH);
        System.out.println("------------------------------------");
    }

    @Test
    public void checkMultiHHashValue() {
        BigInteger a = BigInteger.ONE.add(BigInteger.ONE);
        for (int i = 0; i < 32; i++) {
            BigInteger outH = PoseidonParamsType.H(a.pow(i).subtract(BigInteger.ONE));
            System.out.println("a = " + i);
            System.out.print("0x");
            for (byte v : outH.toByteArray()) {
                System.out.printf("%02x", v);
            }
            System.out.println("\r");
        }
    }

    @Test
    public void checkPoseidonHashValue() {
        PoseidonParamsType params = PoseidonParamsType.CreatePoseidonParams(Field.SNARK_SCALAR_FIELD, 6, 8, 57,"poseidon",
                5, null, null, 126);
        BigInteger[] inputs = new BigInteger[]{new BigInteger(new byte[]{0})};
        BigInteger hash = Poseidon.hash(inputs, params, false, true);

        System.out.println("input = " + inputs + ", output hash = " + hash);
        System.out.println("len of hash = " + hash.toByteArray().length);
        for (byte v : hash.toByteArray()) {
            System.out.printf("%02x", v);
        }

        inputs = new BigInteger[]{new BigInteger(new byte[]{1})};
        hash = Poseidon.hash(inputs, params, false, true);

        System.out.println("input = " + inputs + ", output hash = " + hash);
        System.out.println("len of hash = " + hash.toByteArray().length);
        for (byte v : hash.toByteArray()) {
            System.out.printf("%02x", v);
        }

        inputs = new BigInteger[]{new BigInteger(new byte[]{1}), new BigInteger(new byte[]{2})};
        hash = Poseidon.hash(inputs, params, false, true);

        System.out.println("input = " + inputs + ", output hash = " + hash);
        System.out.println("len of hash = " + hash.toByteArray().length);
        for (byte v : hash.toByteArray()) {
            System.out.printf("%02x", v);
        }

        // change t
        params = PoseidonParamsType.CreatePoseidonParams(Field.SNARK_SCALAR_FIELD, 7, 8, 57,"poseidon",
                5, null, null, 126);
        inputs = new BigInteger[]{new BigInteger(new byte[]{1}), new BigInteger(new byte[]{2})};
        hash = Poseidon.hash(inputs, params, false, true);

        System.out.println("input = " + inputs + ", output hash = " + hash);
        System.out.println("len of hash = " + hash.toByteArray().length);
        for (byte v : hash.toByteArray()) {
            System.out.printf("%02x", v);
        }
    }

    @Test
    public void checkPoseidonContants() {
        PoseidonParamsType params = PoseidonParamsType.CreatePoseidonParams(Field.SNARK_SCALAR_FIELD, 6, 8, 57,"poseidon",
                5, null, null, 126);
        BigInteger[] c = params.poseidon_constants(params.p, params.seed+"_constants", params.nRoundsF + params.nRoundsP);
        for (BigInteger v : c) {
            System.out.println(v.toString(16));
        }
    }

    @Test
    public void checkPoseidonMatrix() {
        PoseidonParamsType params = PoseidonParamsType.CreatePoseidonParams(Field.SNARK_SCALAR_FIELD, 6, 8, 57,"poseidon",
                5, null, null, 126);
        BigInteger[][] c = params.poseidon_matrix(params.p, params.seed+"_matrix_0000", params.t);
        for (BigInteger[] v : c) {
            for (BigInteger vv : v) {
                System.out.println(vv.toString(16));
            }
        }
    }
}

