package com.loopring.utils;

import com.loopring.eddsa.BabyJubjubCurve;

import java.math.BigInteger;

public class BigIntLittleEndianEncoding {

    private static int DEFAULT_BIT_LENGTH = BabyJubjubCurve.BIT_FIELD_SIZE;

    private static BigInteger DEFAULT_P = BabyJubjubCurve.p;

    /**
     * Mask where only the first b-1 bits are set.
     */
    private BigInteger mask;

    private BigInteger p;

    private int bitLength;

    private int byteLength;

    private BigIntLittleEndianEncoding(int n, BigInteger p) {
        assert (p.bitLength() <= n);
        this.setBitLength(n);
        this.setFieldParam(p);
    }

    static public BigIntLittleEndianEncoding newInstance() {
        return new BigIntLittleEndianEncoding(DEFAULT_BIT_LENGTH, DEFAULT_P);
    }

    public BigIntLittleEndianEncoding setBitLength(int n) {
        this.mask = BigInteger.ONE.shiftLeft(n).subtract(BigInteger.ONE);
        this.bitLength = n;
        this.byteLength = (n + 7) / 8;
        return this;
    }

    public BigIntLittleEndianEncoding setFieldParam(BigInteger p) {
        this.p = p;
        return this;
    }

    /**
     *  Convert $x$ to little endian.
     *  Constant time.
     *
     *  @param x the BigInteger value to encode
     *  @return array of length $b/8$
     *  @throws IllegalStateException if field not set
     */
    public byte[] encode(BigInteger x) {
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

    /**
     *  Decode a FieldElement from its $(b-1)$-bit encoding.
     *  The highest bit is masked out.
     *
     *  @param in the $(b-1)$-bit encoding of a FieldElement.
     *  @return the FieldElement represented by 'val'.
     *  @throws IllegalStateException if field not set
     *  @throws IllegalArgumentException if encoding is invalid
     */
    public BigInteger decode(byte[] in) {
        if (in.length != p.bitLength()/8)
            throw new IllegalArgumentException("Not a valid encoding");
        return toBigInteger(in).mod(p);
    }

    /**
     *  Convert in to big endian
     *
     *  @param in the $(b-1)$-bit encoding of a FieldElement.
     *  @return the decoded value as a BigInteger
     */
    public BigInteger toBigInteger(byte[] in) {
        byte[] out = new byte[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = in[in.length-1-i];
        }
        return new BigInteger(1, out);
    }

    /**
     * From the Ed25519 paper:<br>
     * $x$ is negative if the $(b-1)$-bit encoding of $x$ is lexicographically larger
     * than the $(b-1)$-bit encoding of $-x$. If $q$ is an odd prime and the encoding
     * is the little-endian representation of $\{0, 1,\dots, q-1\}$ then the negative
     * elements of $F_q$ are $\{1, 3, 5,\dots, q-2\}$.
     * @return true if negative
     */
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