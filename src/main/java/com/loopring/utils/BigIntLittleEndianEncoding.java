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

    /*
     * encode a BigInteger to le buffer
     *     i.e. 01 == 0x01 => [1, 0, ...{32}..., 0]
     *          17 == 0x12 => [2, 1, ...{32}..., 0]
     */
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

    /*
     * encode a BigInteger String to JS compatible le buffer String, because JS lib uses
     * this method to format a leBuf and transfers which to bigint.
     *     i.e. 01 == "01" => String([0, ...{32}..., 0, 1])
     *          17 == "17" => String([0, ...{32}..., 1, 7])
     */
    public String encodeJsBigInt(String bigIntStr) {
        byte[] in = bigIntStr.getBytes();
        assert (in.length <= byteLength);
        if (in.length > byteLength) {
            return new String("");
        }

        byte[] out = new byte[byteLength];
        for (int i = 0; i < in.length; i++) {
            int c = in[i];
            assert(c >= '0' && c <= '9');
            if (c < '0' || c > '9') {
                return new String("");
            }
            out[byteLength - in.length + i] = (byte)(c);
        }

        for (int i = 0; i < byteLength - in.length; i++) {
            out[i] = '0';
        }

        return new String(out);
    }

    public BigInteger decodeJsBigInt(String jsLeBigIntString) {
        byte[] in = jsLeBigIntString.getBytes();
        BigInteger result = BigInteger.ZERO;
        for (int i = 0; i < in.length; i++) {
            int c = in[i];
            assert(c >= '0' && c <= '9');
            if (c < '0' || c > '9') {
                return BigInteger.ZERO;
            }
            BigInteger bigIntC = BigInteger.valueOf(c);
            result = result.add(bigIntC.shiftLeft(i * 8));
        }
        return result;
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
