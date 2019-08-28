package com.loopring.eddsa;

import com.loopring.utils.BigIntLittleEndianEncoding;

import java.math.BigInteger;

public class EdDSASignature {
    byte[] signature;

    private Point rPoint;
    private BigInteger nSignature;
    BigIntLittleEndianEncoding enc;


    public EdDSASignature(byte[] signature) {
        assert (signature.length == BabyJubjubCurve.FIELD_SIZE * 3);

        byte[] pointBuf = new byte[BabyJubjubCurve.FIELD_SIZE * 2];
        byte[] signBuf = new byte[BabyJubjubCurve.FIELD_SIZE];

        System.arraycopy(signature, 0, pointBuf, 0, pointBuf.length);
        System.arraycopy(signature, pointBuf.length, signBuf, 0, signBuf.length);

        if (enc == null) {
            enc = BigIntLittleEndianEncoding.newInstance();
        }
        rPoint = new Point(pointBuf);
        nSignature = enc.decode(signBuf);
    }

    public EdDSASignature(Point R8, BigInteger S) {
        rPoint = R8;
        nSignature = S;
    }

    public byte[] toByteArray() {
        byte[] signature = new byte[96];
        if (enc == null) {
            enc = BigIntLittleEndianEncoding.newInstance();
        }
        System.arraycopy(enc.encode(rPoint.x), 0, signature, 0, 32);
        System.arraycopy(enc.encode(rPoint.y), 0, signature, 32, 32);
        System.arraycopy(enc.encode(nSignature), 0, signature, 64, 32);
        return signature;
    }

    public Point getPointR() {
        return rPoint;
    }

    public BigInteger getS() {
        return nSignature;
    }
}
