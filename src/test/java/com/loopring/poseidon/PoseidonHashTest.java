package com.loopring.poseidon;

import static com.loopring.poseidon.PoseidonHash.Field.SNARK_SCALAR_FIELD;
import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

import java.math.BigInteger;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Case {
    String name;
    String expected;
    BigInteger[] numInputs;
    byte[]  bytesInputs;

    static PoseidonHash.PoseidonParamsType params = PoseidonHash.DefaultParams;
    static PoseidonHash poseidon = PoseidonHash.Digest.newInstance(params);

    static void setNewParams(PoseidonHash.PoseidonParamsType params) {
        Case.params = params;
        poseidon = PoseidonHash.Digest.newInstance(params);
    }

    Case(String name, String expected, BigInteger[] inputs) {
        this.name = name;
        this.expected = expected;
        this.numInputs = inputs;
    }

    Case(String name, String expected, byte[] inputs) {
        this.name = name;
        this.expected = expected;
        this.bytesInputs = inputs;
    }

    String testPoseidonHash(BigInteger inputs) {
        poseidon.reset();
        poseidon.add(inputs);
        return byteToHex(poseidon.digest());
    }

    String testPoseidonHash(BigInteger[] inputs) {
        poseidon.reset();
        poseidon.add(inputs);
        return byteToHex(poseidon.digest());
    }

    String[] testChainPoseidonHash(BigInteger[] inputs) {
        poseidon.reset();
        poseidon.add(inputs);
        BigInteger[] hashes = poseidon.digest(true);
        String[] chainHash = new String[hashes.length];
        for (int i = 0; i < hashes.length; i++) {
            chainHash[i] = hashes[i].toString(10);
        }
        return chainHash;
    }

    private String byteToHex(byte[] bytes) {
        String hexStr = "0x";
        for (byte b : bytes) {
            hexStr += String.format("%02x", b);
        }
        return hexStr;
    }
}

/**
 * Unit test for simple App.
 */
public class PoseidonHashTest
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void DefaultParamTest()
    {
        Case[] cases = new Case[]{
                new Case("0",
                        "0x021a76d5f2cdcf354ab66eff7b4dee40f02501545def7bb66b3502ae68e1b781",
                        new BigInteger[]{new BigInteger(new byte[]{0})}),
                new Case("1",
                        "0x050a05b5d53f6f01b1629db59138e94b0827e70cbf91b1f66255b90ca700450d",
                        new BigInteger[]{new BigInteger(new byte[]{1})}),
                new Case("2",
                        "0x1d08db71da28c62e2335bd068af0f92aef0e660fe1b9a597d2ad4bcb8c2619db",
                        new BigInteger[]{new BigInteger(new byte[]{2})}),
                new Case("3",
                        "0x28c656b9b00f105832304d31b2b143b2c4ccba739d9db04ea7c2d4baf8c567cd",
                        new BigInteger[]{new BigInteger(new byte[]{3})}),
                new Case("q-1",
                        "0x19516079b85b6ea905493fc1c8353201ac76eee62a1341f52fd71e85f8b491ab",
                        new BigInteger[]{PoseidonHash.DefaultParams.p.subtract(BigInteger.ONE)}),
                new Case("[0,1,2]",
                        "0x06c96f1a6f1d72ac9ae97e7456e82327022e97b4787a70c9ea10df62640644c6",
                        new BigInteger[]{BigInteger.valueOf(0), BigInteger.valueOf(1), BigInteger.valueOf(2)}),
                new Case("[0, 1, 2, 3, 4]",
                        "0x04d751fbf220324f45ba8cb53d9935b8af647e9d55eab24113fb4f9ee2835ee6",
                        new BigInteger[]{BigInteger.valueOf(0), BigInteger.valueOf(1), BigInteger.valueOf(2),
                                         BigInteger.valueOf(3), BigInteger.valueOf(4)}),
        };

        for (Case c : cases) {
            assertEquals(c.name, c.expected, c.testPoseidonHash(c.numInputs));
        }
    }

    @Test
    public void userParamTest()
    {
        PoseidonHash.PoseidonParamsType userParams = PoseidonHash.PoseidonParamsType.newInstance(
                SNARK_SCALAR_FIELD, 10, 8, 57, "poseidon",
                5, null, null, 126);
        Case.setNewParams(userParams);
        Case[] cases = new Case[]{
                new Case("0",
                        "0x27476133378d92972301ea9ace2f75bbf9e9fb9abbed4b24c5c90b218e164444",
                        new BigInteger[]{new BigInteger(new byte[]{0})}),
                new Case("1",
                        "0x1052f72d52e425bf02e1a62e9f89eba8ba522af8084f875ffeab6a2f69ba2614",
                        new BigInteger[]{new BigInteger(new byte[]{1})}),
                new Case("2",
                        "0x1364b2857bf5b8fbe6f62b71cf3b619daa1e20cbea53c0121a8bf2d98f82aecb",
                        new BigInteger[]{new BigInteger(new byte[]{2})}),
                new Case("3",
                        "0x15375c4785401643f109c7b5438bfa966d3efd06f8e5b0148b8a3e3fcba2f652",
                        new BigInteger[]{new BigInteger(new byte[]{3})}),
                new Case("q-1",
                        "0x0262b02ce75d0b2967ad39b7b4e025cdb7c7e1a02a82cda6d0ae90df72e08381",
                        new BigInteger[]{PoseidonHash.DefaultParams.p.subtract(BigInteger.ONE)}),
                new Case("[0,1,2]",
                        "0x0d84f64feaf9db879e3a2d79bcaabf0355b5b99c65f5399adf181bce173b4d65",
                        new BigInteger[]{BigInteger.valueOf(0), BigInteger.valueOf(1), BigInteger.valueOf(2)}),
                new Case("[0, 1, 2, 3, 4]",
                        "0x074357482eaffb63be4afc4d41de8d435a301051066671fb2826972d03ffd677",
                        new BigInteger[]{BigInteger.valueOf(0), BigInteger.valueOf(1), BigInteger.valueOf(2),
                                BigInteger.valueOf(3), BigInteger.valueOf(4)}),
        };

        for (Case c : cases) {
            assertEquals(c.name, c.expected, c.testPoseidonHash(c.numInputs));
        }
    }

    @Test
    public void checkMerklePoseidonHash() {
        PoseidonHash.PoseidonParamsType merklePoseidonParams =
                PoseidonHash.PoseidonParamsType.newInstance(SNARK_SCALAR_FIELD, 5, 6, 52,
                                                      "poseidon", 5, 128);
        Case.setNewParams(merklePoseidonParams);
        Case[] cases = new Case[]{
                new Case("0",
                        "0x2874a569c01627ec42738d210efcec8a9034000d070afcf92172ce4527fdfc59",
                        new BigInteger[]{new BigInteger(new byte[]{0})}),
                new Case("1",
                        "0x23c19d3974ab494170f6e695afd68c46f15c12e8730294f8443eca2dc43fdcd8",
                        new BigInteger[]{new BigInteger(new byte[]{1})}),
                new Case("[1,2]",
                        "0x1e4584dd1609e9a1872196ba07a0b0b1df22e09a162d981e6c41a5213894462b",
                        new BigInteger[]{new BigInteger(new byte[]{1}), new BigInteger(new byte[]{2})}),
                new Case("q-1",
                        "0x103b5a4bba22fb8b9f52cfc3b7cbf7abe35b38dce536aa0a5b774ab0f3a54654",
                        new BigInteger[]{PoseidonHash.DefaultParams.p.subtract(BigInteger.ONE)}),
                new Case("[0,1,2]",
                        "0x24b3a693db7f04cd0d32b66c9435510f7b18ff2da65bb65933c89627318dd8a1",
                        new BigInteger[]{BigInteger.valueOf(0), BigInteger.valueOf(1), BigInteger.valueOf(2)}),
                new Case("[0, 1, 2, 3]",
                        "0x0387b3c1c11beb491ba8f12eaf554ba91e17dfe8e47eb4ce463a26ee743a6346",
                        new BigInteger[]{BigInteger.valueOf(0), BigInteger.valueOf(1),
                                         BigInteger.valueOf(2), BigInteger.valueOf(3)}),
        };

        for (Case c : cases) {
            assertEquals(c.name, c.expected, c.testPoseidonHash(c.numInputs));
        }
    }

    @Test
    public void checkChainedPoseidon() {
        PoseidonHash.PoseidonParamsType merklePoseidonParams =
                PoseidonHash.PoseidonParamsType.newInstance(SNARK_SCALAR_FIELD, 5, 6, 52,
                        "poseidon", 5, 128);
        Case.setNewParams(merklePoseidonParams);
        BigInteger[] inputs = new BigInteger[]{BigInteger.valueOf(0), BigInteger.valueOf(1),
                                BigInteger.valueOf(2), BigInteger.valueOf(3),  BigInteger.valueOf(4)};
        Case c = new Case("Chain hash [0, 1, 2, 3, 4]", "", inputs);


        assertArrayEquals(new String[] {
                "15864913677115162934064607149194869253350241780874175050452413048185040098763",
                "7319943059005658379315734218739496583847470886995543107485878740768447351279",
                "15850557525889711069621591486692886703107356190007608399927534033240030180242",
                "20065657824293123484454605177858418945231474745592934103976154543264266512613",
                "15092432367470046186770254078717787125796199987224954741108014626080561676082",
                }, c.testChainPoseidonHash(inputs));
    }

    @Test
    public void checkMerklePoseidonHashBufferLength() {
        PoseidonHash.PoseidonParamsType merklePoseidonParams =
                PoseidonHash.PoseidonParamsType.newInstance(SNARK_SCALAR_FIELD, 5, 6, 52,
                        "poseidon", 5, 128);
        Case.setNewParams(merklePoseidonParams);
        Case[] cases = new Case[]{
                // 31 bytes
                new Case("0",
                        "0x7e57111afc5df6bfabb786d9fb9790c1791546c1d3186e0712ffae816ca9c7",
                        new BigInteger[]{BigInteger.valueOf(980)}),
                // 31 byte but 1 sign byte
                new Case("0",
                        "0x00f16533bed797a7d1ceda6ce71813e93e7dd0de7747a2cbe0d4cdd36125d524",
                        new BigInteger[]{BigInteger.valueOf(984)}),
        };

        for (Case c : cases) {
            assertEquals(c.name, c.expected, c.testPoseidonHash(c.numInputs));
        }
    }

    @Test
    public void checkSignaturePoseidonHash() {
        PoseidonHash.PoseidonParamsType merklePoseidonParams =
                PoseidonHash.PoseidonParamsType.newInstance(SNARK_SCALAR_FIELD, 66, 6, 56,
                        "poseidon", 5, 128);
        Case.setNewParams(merklePoseidonParams);
        Case[] cases = new Case[]{
                new Case("[1]",
                        "0x0f7868ef0d81ff4b765e1b461c99e83002fb28685bb22af2cf1246bac539c35c",
                        new BigInteger[]{BigInteger.valueOf(1)}),
                new Case("[1,3,5,7]",
                        "0x2655c8923d9d779a54c359c790d42d024cfbba78da4eab9911be9fab03414cc8",
                        new BigInteger[]{BigInteger.valueOf(1), BigInteger.valueOf(3),
                                         BigInteger.valueOf(5), BigInteger.valueOf(7)}),
        };

        for (Case c : cases) {
            assertEquals(c.name, c.expected, c.testPoseidonHash(c.numInputs));
        }
    }

    @Test
    public void checkFailureBoundary() {
        Case.setNewParams(PoseidonHash.DefaultParams);
        Case[] cases = new Case[]{
                new Case("[0, 1, 2, 3, 4, 5] out of boundary",
                        "",
                        new BigInteger[]{BigInteger.valueOf(0), BigInteger.valueOf(1), BigInteger.valueOf(2),
                                BigInteger.valueOf(3), BigInteger.valueOf(4), BigInteger.valueOf(5)}),
                new Case("[Fq + 1] out of boundary",
                        "",
                        new BigInteger[]{PoseidonHash.DefaultParams.p.add(BigInteger.ONE)}),
        };

        boolean pass = false;
        for (Case c : cases) {
            try {
                c.testPoseidonHash(c.numInputs);
                pass = true;
            } catch (AssertionError e) {
                assertTrue(true);
            }
            assertEquals("Expected an AssertionError to be thrown in " + c.name, false, pass);
        }
    }

    @Test
    public void checkHHashValue() {
        BigInteger a = BigInteger.ONE.add(BigInteger.ONE);
        BigInteger b = PoseidonHash.PoseidonParamsType.H(a.pow(256).subtract(BigInteger.ONE)); // blake2b(2**256 - 1)
        String res = "0x";
        for (byte v : b.toByteArray()) {
            res += String.format("%02x", v);
        }
        assertEquals("0x00d45e5814d43ea4dd6dedde70ae918ecfdad1d0f9369f8b0b19010f50601102e2", res);

        // 0xFFFF....FFFF == 2**256 - 1;
        BigInteger inputH = new BigInteger(1, new byte[]{-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
                                                                 -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1});
        BigInteger outH = PoseidonHash.PoseidonParamsType.H(inputH);
        res = "0x";
        for (byte v : outH.toByteArray()) {
            res += String.format("%02x", v);
        }
        assertEquals("0x00d45e5814d43ea4dd6dedde70ae918ecfdad1d0f9369f8b0b19010f50601102e2", res);
    }

    @Test
    public void checkBigIntegerBehavior() {
        BigInteger inputH = new BigInteger(1, new byte[]{-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
                                                                 -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1});
        assertEquals(33, inputH.toByteArray().length);
        assertEquals(0, inputH.toByteArray()[0]);

        // positive mod rather then -1 mod.
        assertEquals(new BigInteger("6350874878119819312338956282401532410528162663560392320966563075034087161850"),
                inputH.mod(PoseidonHash.DefaultParams.p));
    }

    @Ignore
    public void checkDefaultPoseidonContants() {
        PoseidonHash.PoseidonParamsType params = PoseidonHash.DefaultParams;
        BigInteger[] c = params.poseidon_constants(params.p, params.seed+"_constants", params.nRoundsF + params.nRoundsP);
        for (BigInteger v : c) {
            System.out.println(v.toString(16));
        }
    }

    @Ignore
    public void checkDefaultPoseidonMatrix() {
        PoseidonHash.PoseidonParamsType params = PoseidonHash.DefaultParams;
        BigInteger[][] c = params.poseidon_matrix(params.p, params.seed+"_matrix_0000", params.t);
        for (BigInteger[] v : c) {
            for (BigInteger vv : v) {
                System.out.println(vv.toString(16));
            }
        }
    }

    @Test
    public void checkByteArrayInput() {
        Case[] cases = new Case[]{
                new Case("[0xFF]",
                        "0x268b064dbf1d1bf60799872bcabc9d575c43248322a2e918fb8e64e695c28d54",
                        new byte[]{-1}),
                new Case("[0xFFFF]",
                        "0x13d3713fdfc7df203f3700b0305745e245fb05a251709404de320f4373b0e21e",
                        new byte[]{-1, -1}),
                new Case("[0xFFFF07]",
                        "0x2f4f49b46fc083c45864c289217c977e55565581dafd44ce6c11088850afc729",
                        new byte[]{-1, -1, 7}),
                new Case("[0x01020304]",
                        "0x2bbfe646483dedce96101d71610a6b340b21ea89d70f4b27694b00fbf8d0d73c",
                        new byte[]{1,2,3,4}),
        };

        for (Case c : cases) {
            assertEquals(c.name, c.expected, c.testPoseidonHash(new BigInteger(1, c.bytesInputs)));
        }
    }

    boolean threadSafe = true;
    @Test
    public void checkThreadSafe() throws Exception {
        int threads = 3;
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        final CountDownLatch countlatch = new CountDownLatch(threads);
        for (int i = 0; i < threads; i++) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        PoseidonHash.PoseidonParamsType userParams = PoseidonHash.PoseidonParamsType.newInstance(
                                SNARK_SCALAR_FIELD, 10, 8, 57, "poseidon",
                                5, null, null, 126);
                        Case.setNewParams(userParams);
                        Case[] cases = new Case[]{
                                new Case("0",
                                        "0x27476133378d92972301ea9ace2f75bbf9e9fb9abbed4b24c5c90b218e164444",
                                        new BigInteger[]{new BigInteger(new byte[]{0})}),
                                new Case("1",
                                        "0x1052f72d52e425bf02e1a62e9f89eba8ba522af8084f875ffeab6a2f69ba2614",
                                        new BigInteger[]{new BigInteger(new byte[]{1})}),
                                new Case("2",
                                        "0x1364b2857bf5b8fbe6f62b71cf3b619daa1e20cbea53c0121a8bf2d98f82aecb",
                                        new BigInteger[]{new BigInteger(new byte[]{2})}),
                                new Case("3",
                                        "0x15375c4785401643f109c7b5438bfa966d3efd06f8e5b0148b8a3e3fcba2f652",
                                        new BigInteger[]{new BigInteger(new byte[]{3})})
                        };

                        for (Case c : cases) {
                            threadSafe &= c.expected.equals(c.testPoseidonHash(c.numInputs));
                            assertEquals(c.expected, c.testPoseidonHash(c.numInputs));
                        }
                    } finally {
                        countlatch.countDown();
                    }
                }
            });
        }

        countlatch.await();
        assertTrue(threadSafe);
    }
}

