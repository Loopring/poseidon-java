package com.loopring.eddsa;

import com.loopring.utils.BigIntLittleEndianEncoding;

import java.math.BigInteger;

public class FieldElement extends BigIntLittleEndianEncoding {

    BigInteger fq; // field prime number

    BigInteger v;  // value in this field

    public FieldElement(BigInteger prime, BigInteger v) {
        super(BabyJubjubCurve.BIT_FIELD_SIZE);
        this.fq = prime;
        this.v = v.mod(prime);
    }

    public FieldElement(BigInteger prime) {
        this(prime, BigInteger.ZERO);
    }

    public FieldElement add(FieldElement b) {
        assert (fq == b.fq);
        return new FieldElement(fq, v.add(b.v).mod(fq));
    }

    public FieldElement sub(FieldElement b) {
        assert (fq == b.fq);
        return new FieldElement(fq, v.subtract(b.v).mod(fq));
    }

    public FieldElement mul(FieldElement b) {
        assert (fq == b.fq);
        return new FieldElement(fq, v.multiply(b.v).mod(fq));
    }

    public FieldElement square() {
        return new FieldElement(fq, v.pow(2).mod(fq));
    }

    public FieldElement inv()
    {
        return new FieldElement(fq, v.modInverse(fq));
    }

    public FieldElement fromLeBuf(byte[] leBuf) {
        BigInteger bi = this.decode(leBuf);
        return new FieldElement(fq, bi);
    }

    public byte[] toLeBuf() {
        return this.encode(v);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof FieldElement)) return false;
        FieldElement e = (FieldElement)obj;
        return fq.compareTo(e.fq) == 0 && v.compareTo(e.v) == 0;
    }
}
