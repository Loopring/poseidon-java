package com.loopring.utils;

import com.loopring.eddsa.BabyJubjubCurve;
import com.loopring.eddsa.FieldElement;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.*;

public class Blake512HashTest {
    @Test
    public void checkHash() {
        BLAKE512 hasher = new BLAKE512();
        FieldElement e = new FieldElement(BabyJubjubCurve.p, BigInteger.valueOf(256));
        hasher.update(e.toLeBuf());
        byte[] hashBytes = hasher.digest();

        assertEquals(
                "0x5874e9fc9841f6b7d96f05ca833c34e58c9895e8e6bad4bef8f16fe76f7649b9f2e71499ac2eba932bca9b8f50cb2c4836bc1735293ed5ee9a5e02d7b01bb4f2",
                byteToHex(hashBytes)
        );
    }

    private String byteToHex(byte[] bytes) {
        String hexStr = "0x";
        for (byte b : bytes) {
            hexStr += String.format("%02x", b);
        }
        return hexStr;
    }
}
