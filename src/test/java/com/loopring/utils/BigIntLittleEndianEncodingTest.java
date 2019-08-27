package com.loopring.utils;

import com.loopring.eddsa.BabyJubjubCurve;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Random;

public class BigIntLittleEndianEncodingTest {

    @Test
    public void TestBitLength() {
        BigIntLittleEndianEncoding enc = BigIntLittleEndianEncoding.newInstance();
        byte[] buf = enc.encode(BigInteger.ZERO);
        assert (buf.length == enc.byteLength());
        assert (buf.length == BabyJubjubCurve.FIELD_SIZE);
        enc.decode(buf).equals(BigInteger.ZERO);

        buf = enc.encode(BigInteger.ZERO.subtract(BigInteger.ONE));
        assert (buf.length == enc.byteLength());
        assert (buf.length == BabyJubjubCurve.FIELD_SIZE);
        enc.decode(buf).equals(BigInteger.ZERO.subtract(BigInteger.ONE));

        Random r = new Random();
    }
}
