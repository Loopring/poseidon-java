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

    /*
    def neg(self):
		"""
		Twisted Edwards Curves, BBJLP-2008, section 2 pg 2
		"""
		return Point(-self.x, self.y)
     */
    public EddsaPoint neg() {
        return new EddsaPoint(this.x.v.negate(), this.y.v);
    }

    public byte[] compress() {
        /*
            From: https://ed25519.cr.yp.to/eddsa-20150704.pdf
            TODO: No decompress.
         */
        FieldElement x = this.x;
        FieldElement y = this.y;
        byte[] output = y.toLeBuf();
        if (x.isNegative()) {
            output[31] |= 0x80;
        }
        return output;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof EddsaPoint)) return false;
        EddsaPoint pt = (EddsaPoint)obj;
        return x.equals(pt.x) && y.equals(pt.y);
    }
}