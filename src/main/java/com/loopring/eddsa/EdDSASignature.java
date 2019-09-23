package com.loopring.eddsa;

import java.math.BigInteger;

public class EdDSASignature {

    private EddsaPoint rPoint;
    private FieldElement nSignature;

    public EdDSASignature(byte[] signature) {
        assert (signature.length == BabyJubjubCurve.FIELD_SIZE * 3);

        byte[] pointBuf = new byte[BabyJubjubCurve.FIELD_SIZE * 2];
        byte[] signBuf = new byte[BabyJubjubCurve.FIELD_SIZE];

        System.arraycopy(signature, 0, pointBuf, 0, pointBuf.length);
        System.arraycopy(signature, pointBuf.length, signBuf, 0, signBuf.length);

        rPoint = new EddsaPoint(pointBuf);
        nSignature = new FieldElement(BabyJubjubCurve.subOrder, BigInteger.ZERO).fromLeBuf(signBuf);
    }

    public EdDSASignature(EddsaPoint R8, FieldElement S) {
        rPoint = R8;
        nSignature = S;
    }

    public byte[] toByteArray() {
        byte[] signature = new byte[BabyJubjubCurve.FIELD_SIZE*3];

        System.arraycopy(rPoint.x.toLeBuf(), 0, signature, 0, 32);
        System.arraycopy(rPoint.y.toLeBuf(), 0, signature, 32, 32);
        System.arraycopy(nSignature.toLeBuf(), 0, signature, 64, 32);
        return signature;
    }

    public EddsaPoint getPointR() {
        return rPoint;
    }

    public FieldElement getS() {
        return nSignature;
    }
}
