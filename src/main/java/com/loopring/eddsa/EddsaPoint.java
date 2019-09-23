package com.loopring.eddsa;

import java.math.BigInteger;

final public class EddsaPoint {

    public FieldElement x;
    public FieldElement y;

    public EddsaPoint(BigInteger x, BigInteger y) {
        this.x = new FieldElement(BabyJubjubCurve.p, x);
        this.y = new FieldElement(BabyJubjubCurve.p, y);
    }

    public EddsaPoint(FieldElement x, FieldElement y) {
        this.x = x;
        this.y = y;
    }

    public EddsaPoint(byte[] buffer) {
        byte[] xBuf = new byte[BabyJubjubCurve.FIELD_SIZE];
        byte[] yBuf = new byte[BabyJubjubCurve.FIELD_SIZE];

        assert (buffer.length == 2 * BabyJubjubCurve.FIELD_SIZE);
        System.arraycopy(buffer, 0, xBuf, 0, 32);
        System.arraycopy(buffer, 32, yBuf, 0, 32);

        this.x = new FieldElement(BabyJubjubCurve.p, BigInteger.ZERO).fromLeBuf(xBuf);
        this.y = new FieldElement(BabyJubjubCurve.p, BigInteger.ZERO).fromLeBuf(yBuf);
    }

    public EddsaPoint(byte[] x, byte[] y) {
        this.x = new FieldElement(BabyJubjubCurve.p, BigInteger.ZERO).fromLeBuf(x);
        this.y = new FieldElement(BabyJubjubCurve.p, BigInteger.ZERO).fromLeBuf(y);
    }
}