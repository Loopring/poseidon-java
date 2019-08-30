package com.loopring.utils;

import com.loopring.eddsa.BabyJubjubCurve;

import java.math.BigInteger;

public class BigIntLittleEndianEncoding {

    private static int DEFAULT_BIT_LENGTH = BabyJubjubCurve.BIT_FIELD_SIZE;

    private int bitLength;

    private int byteLength;

    protected BigIntLittleEndianEncoding(int n) {
        this.setBitLength(n);
    }

    static public BigIntLittleEndianEncoding newInstance() {
        return new BigIntLittleEndianEncoding(DEFAULT_BIT_LENGTH);
    }

    public BigIntLittleEndianEncoding setBitLength(int n) {
        this.bitLength = n;
        this.byteLength = (n + 7) / 8;
        return this;
    }

    public byte[] encode(BigInteger x) {
        assert (!isNegative(x));

        byte[] in = x.toByteArray();
        byte[] out = new byte[byteLength];
        for (int i = 0; i < in.length; i++) {
            out[i] = in[in.length-1-i];
        }
        for (int i = in.length; i < out.length; i++) {
            out[i] = 0;
        }
        return out;
    }

    public BigInteger decode(byte[] in) {
        return toBigInteger(in);
    }

    private BigInteger toBigInteger(byte[] in) {
        byte[] out = new byte[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = in[in.length-1-i];
        }
        return new BigInteger(1, out);
    }

    public boolean isNegative(BigInteger x) {
        return x.compareTo(BigInteger.ZERO) < 0;
    }

    public int bitLength() {
        return bitLength;
    }

    public int byteLength() {
        return byteLength;
    }
}