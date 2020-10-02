package com.loopring.utils;

import com.loopring.eddsa.BabyJubjubCurve;

import java.math.BigInteger;

import static java.lang.Math.min;

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
     *     i.e. 01  ==  0x01 => [1, 0, ...{32}..., 0]
     *          257 == 0x101 => [1, 1, ...{32}..., 0]
     */
    public byte[] encode(BigInteger x) {
        assert (!isNegative(x));

        byte[] in = x.toByteArray();
        byte[] out = new byte[byteLength];
        assert(in.length <= byteLength || in.length == byteLength + 1);
        int outputLen = min(byteLength, in.length);
        for (int i = 0; i < outputLen; i++) {
            out[i] = in[in.length-1-i];
        }
        for (int i = in.length; i < out.length; i++) {
            out[i] = 0;
        }
        return out;
    }

    /*
     * decode a le buffer to a BigInteger
     *     i.e. [1, 0, ...{32}..., 0] == 0...{32}...01
     *          [2, 1, ...{32}..., 0] == 0...{32}.0258
     */
    public BigInteger decode(byte[] in) {
        return toBigInteger(in);
    }

    /*
     * encode a BigInteger String to JS compatible le buffer String, because JS lib uses
     * this method to format a leBuf and transfers which to bigint.
     *     i.e. 001 == "001" => String([0, ...{32}..., 0, 1])
     *          257 == "257" => String([0, ...{32}..., 1, 1])
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

    /*
     * decode a BigInteger String to JS compatible le buffer String, because JS lib uses
     * this method to format a leBuf and transfers which to bigint.
     *  Refer to snarkjs.bigInt.leBuff2Int().
     * NOTE:
     *  Convert result of le buffer '123' is 49 + 50*256 + 51*256^2,
     *                           rather than  1 +  2*256 +  3*256^2.
     *  This is because js lib adopts such conversion.
     *  SO, please also NOTE: JsleBuffToBigInt(encodeJsBigInt(a)) != a.
     */
    public BigInteger decodeJsLeBuffToBigInt(String jsLeBigIntString) {
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
