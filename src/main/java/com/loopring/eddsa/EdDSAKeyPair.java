package com.loopring.eddsa;

public class EdDSAKeyPair {
    public byte[] publicKeyX;
    public byte[] publicKeyY;
    public byte[] secretKey;

    public EdDSAKeyPair(byte[] x, byte[] y, byte[] secret) {
        assert (x.length == BabyJubjubCurve.FIELD_SIZE);
        assert (y.length == BabyJubjubCurve.FIELD_SIZE);

        this.publicKeyX = x;
        this.publicKeyY = y;
        this.secretKey = secret;
    }

    public byte[] publicKey() {
        byte[] bytes = new byte[BabyJubjubCurve.FIELD_SIZE*2];
        System.arraycopy(publicKeyX, 0, bytes, 0, BabyJubjubCurve.FIELD_SIZE);
        System.arraycopy(publicKeyY, 0, bytes, BabyJubjubCurve.FIELD_SIZE, BabyJubjubCurve.FIELD_SIZE);
        return bytes;
    }

    public byte[] privateKey() {
        return secretKey;
    }
}
