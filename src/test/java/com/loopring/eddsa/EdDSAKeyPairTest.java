package com.loopring.eddsa;

import org.junit.Test;
import static org.junit.Assert.*;

public class EdDSAKeyPairTest {
    @Test
    public void TestKeyPairCreateAndOnCurve() {
        EdDSAEngine eddsaEngine = new EdDSAEngine();
        EdDSAKeyPair keyPair = eddsaEngine.generateKeyPair();

        assertEquals(BabyJubjubCurve.FIELD_SIZE, keyPair.publicKeyX.length);
        assertEquals(BabyJubjubCurve.FIELD_SIZE, keyPair.publicKeyY.length);
        assertEquals(BabyJubjubCurve.FIELD_SIZE, keyPair.secretKey.length);
        assertTrue(BabyJubjubCurve.inCurve(new Point(keyPair.publicKeyX, keyPair.publicKeyY)));
    }
}
