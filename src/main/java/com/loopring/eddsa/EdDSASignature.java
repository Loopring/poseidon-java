package com.loopring.eddsa;

import java.math.BigInteger;

public class EdDSASignature {
    byte[] rX;
    byte[] rY;
    byte[] signature;

    public EdDSASignature(byte[] signature) {
        assert (signature.length == BabyJubjubCurve.FIELD_SIZE * 3);

        this.rX = new byte[BabyJubjubCurve.FIELD_SIZE];
        this.rY = new byte[BabyJubjubCurve.FIELD_SIZE];
        this.signature  = new byte[BabyJubjubCurve.FIELD_SIZE];

        System.arraycopy(signature, 0, this.rX, 0, BabyJubjubCurve.FIELD_SIZE);
        System.arraycopy(signature, BabyJubjubCurve.FIELD_SIZE, this.rY, 0, BabyJubjubCurve.FIELD_SIZE);
        System.arraycopy(signature, BabyJubjubCurve.FIELD_SIZE*2, this.signature, 0, BabyJubjubCurve.FIELD_SIZE);
    }

    public EdDSASignature(Point R8, BigInteger S) {
        assert (signature.length == BabyJubjubCurve.FIELD_SIZE * 3);

        this.rX = new byte[BabyJubjubCurve.FIELD_SIZE];
        this.rY = new byte[BabyJubjubCurve.FIELD_SIZE];
        this.signature  = new byte[BabyJubjubCurve.FIELD_SIZE];

        System.arraycopy(R8.x.toByteArray(), 0, this.rX, 0, BabyJubjubCurve.FIELD_SIZE);
        System.arraycopy(R8.y.toByteArray(),  0, this.rY, 0, BabyJubjubCurve.FIELD_SIZE);
        System.arraycopy(S.toByteArray(), 0, this.signature, 0, BabyJubjubCurve.FIELD_SIZE);
    }

    public byte[] toByteArray() {
        byte[] signature = new byte[96];
        System.arraycopy(rX, 0, signature, 0, 32);
        System.arraycopy(rY, 0, signature, 32, 32);
        System.arraycopy(signature, 0, signature, 64, 32);
        return signature;
    }

}
