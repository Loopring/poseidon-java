package com.loopring.utils;

import com.loopring.eddsa.BabyJubjubCurve;
import org.junit.Test;
import static org.junit.Assert.*;

import java.math.BigInteger;
import java.util.Random;

public class BigIntLittleEndianEncodingTest {

    @Test
    public void TestBitLength() {
        BigIntLittleEndianEncoding enc = BigIntLittleEndianEncoding.newInstance();
        byte[] buf = enc.encode(BigInteger.ZERO);
        assert (buf.length == enc.byteLength());
        assert (buf.length == BabyJubjubCurve.FIELD_SIZE);
        assertTrue(enc.decode(buf).equals(BigInteger.ZERO));

        buf = enc.encode(BigInteger.ONE);
        byte[] refBuf = new byte[]{ 1, 0, 0, 0, 0, 0, 0, 0,
                                    0, 0, 0, 0, 0, 0, 0, 0,
                                    0, 0, 0, 0, 0, 0, 0, 0,
                                    0, 0, 0, 0, 0, 0, 0, 0,};
        assert (buf.length == enc.byteLength());
        assert (buf.length == BabyJubjubCurve.FIELD_SIZE);
        assertArrayEquals (refBuf, buf);
        assertEquals(enc.decode(buf), BigInteger.ONE);

        for (int i = 0; i < 100; i++) {
            Random r = new Random();
            BigInteger num = new BigInteger(BabyJubjubCurve.FIELD_SIZE, r);
            buf = enc.encode(num);
            assert (buf.length == enc.byteLength());
            assert (buf.length == BabyJubjubCurve.FIELD_SIZE);
            assertEquals(enc.decode(buf), num);
        }
    }

    @Test
    public void TestStringEncode() {
        BigIntLittleEndianEncoding enc = BigIntLittleEndianEncoding.newInstance();
        String jsLeBigIntString = enc.encodeJsBigInt("1268930117");
        assert (jsLeBigIntString.length() == enc.byteLength());
        assert (jsLeBigIntString.length() == BabyJubjubCurve.FIELD_SIZE);
        BigInteger key = enc.decodeJsLeBuffToBigInt(jsLeBigIntString);
        assertTrue(key.equals(new BigInteger("24964121663296690597301628486727307956934600944495843862298380770677613408304")));
    }

    @Test
    public void TestByteDecode() {
        BigIntLittleEndianEncoding enc = BigIntLittleEndianEncoding.newInstance();
        // bigInt2e256m1 = 2**256 - 1 == 0xFF..<-255bits->..FF
        BigInteger bigInt2e256m1 = new BigInteger("115792089237316195423570985008687907853269984665640564039457584007913129639935");
        byte[] refBuf1 = new byte[]{
                -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1};
        BigInteger key = enc.decode(refBuf1);
        assertTrue(key.equals(bigInt2e256m1));

        byte[] refBuf2 = new byte[]{
                -3, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1};
        key = enc.decode(refBuf2);
        assertTrue(key.equals(bigInt2e256m1.subtract(BigInteger.valueOf(2))));
    }
}

