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
        BigInteger key = enc.decodeJsBigInt(jsLeBigIntString);
        assertTrue(key.equals(new BigInteger("24964121663296690597301628486727307956934600944495843862298380770677613408304")));
    }
}

