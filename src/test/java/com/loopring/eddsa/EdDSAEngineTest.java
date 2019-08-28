package com.loopring.eddsa;

import com.loopring.utils.BigIntLittleEndianEncoding;
import org.junit.Test;

//import java.math.BigInteger;

import static org.junit.Assert.*;

public class EdDSAEngineTest {
    static BigIntLittleEndianEncoding bigIntEnc = BigIntLittleEndianEncoding.newInstance();
    @Test
    public void testEngineSignVerify() {
        EdDSAEngine engine = new EdDSAEngine();

        EdDSAKeyPair key = engine.generateKeyPair();
        byte[] msg = new byte[]{1, 0, 0, 0, 0, 0, 0, 0,
                                0, 0, 0, 0, 0, 0, 0, 0,
                                0, 0, 0, 0, 0, 0, 0, 0,
                                0, 0, 0, 0, 0, 0, 0, 0};

        System.out.println(bigIntEnc.decode(key.privateKey()));
        byte[] sign = engine.sign(key.privateKey(), msg);

        assertEquals(BabyJubjubCurve.FIELD_SIZE*3, sign.length);
        assertTrue(engine.verify(msg, sign, key.publicKey()));
    }

}
