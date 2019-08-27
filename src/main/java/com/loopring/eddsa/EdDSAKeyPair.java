package com.loopring.eddsa;

import java.math.BigInteger;

public class EdDSAKeyPair {
    byte[] publicKeyX;
    byte[] publicKeyY;
    byte[] secretKey;

    public EdDSAKeyPair(Point g, byte[] secret) {
        assert (g.x.compareTo(BigInteger.ZERO) > 0);
        assert (g.y.compareTo(BigInteger.ZERO) > 0);

        this.publicKeyX = g.x.toString(10).getBytes();
        this.publicKeyY = g.y.toString(10).getBytes();
        this.secretKey = secret;
    }
}
