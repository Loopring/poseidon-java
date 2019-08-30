package com.loopring.eddsa;

import com.loopring.poseidon.PoseidonHash;
import com.loopring.utils.BLAKE512;
import com.loopring.utils.BigIntLittleEndianEncoding;

import java.math.BigInteger;
import java.security.SecureRandom;

public class EdDSAEngine extends BigIntLittleEndianEncoding {
    boolean initialized;
    final int DEFAULT_KEY_BITLENGTH = BabyJubjubCurve.BIT_FIELD_SIZE;
    private int keySize;
    private SecureRandom random;
    private BLAKE512 blake512HashEngine;
    private PoseidonHash poseidonHashEngine;

    public EdDSAEngine(){
        super(BabyJubjubCurve.BIT_FIELD_SIZE);
        initialize(DEFAULT_KEY_BITLENGTH, new SecureRandom());
    }

    public void initialize(int keySize, SecureRandom r) {
        this.keySize = keySize;
        this.random = r;
        blake512HashEngine = new BLAKE512();
        PoseidonHash.PoseidonParamsType poseidonParams =
                PoseidonHash.PoseidonParamsType.newInstance(PoseidonHash.Field.SNARK_SCALAR_FIELD, 6, 6, 52,
                        "poseidon", 5, 128);
        poseidonHashEngine = PoseidonHash.Digest.newInstance(poseidonParams);
        initialized = true;
    }

    public EdDSAKeyPair generateKeyPair() {
        if (!initialized)
            initialize(DEFAULT_KEY_BITLENGTH, new SecureRandom());

        byte[] seed = new byte[DEFAULT_KEY_BITLENGTH /8];
        random.nextBytes(seed);

        FieldElement secretKey = new FieldElement(BabyJubjubCurve.subOrder, new BigInteger(1, seed));
        Point publicKey = BabyJubjubCurve.mulPointEscalar(BabyJubjubCurve.base8, secretKey.v);

        return new EdDSAKeyPair(publicKey.x.toLeBuf(),
                                publicKey.y.toLeBuf(),
                                secretKey.toLeBuf());
    }

    public byte[] sign(byte[] keyBytes, byte[] msg) {
        assert (keyBytes.length == 32);
        byte[] h1 = blake512HashEngine.digest(keyBytes);
        byte[] msgBuffer = new byte[64];

        assert (h1.length == 64);
        System.arraycopy(h1, 32, msgBuffer, 0, 32);

        assert (msg.length == 32);
        System.arraycopy(msg, 0, msgBuffer, 32, 32);

        blake512HashEngine.reset();
        byte[] rBuff = blake512HashEngine.digest(msgBuffer);

        FieldElement r = new FieldElement(BabyJubjubCurve.subOrder).fromLeBuf(rBuff);
        FieldElement key = new FieldElement(BabyJubjubCurve.subOrder).fromLeBuf(keyBytes);
        Point A = BabyJubjubCurve.mulPointEscalar(BabyJubjubCurve.base8, key.v);
        Point R8 = BabyJubjubCurve.mulPointEscalar(BabyJubjubCurve.base8, r.v);

        poseidonHashEngine.add(R8.x.v.toByteArray());
        poseidonHashEngine.add(R8.y.v.toByteArray());
        poseidonHashEngine.add(A.x.v.toByteArray());
        poseidonHashEngine.add(A.y.v.toByteArray());
        poseidonHashEngine.add(decode(msg).toByteArray());

        byte[] hm = poseidonHashEngine.digest();
        FieldElement hmInt = new FieldElement(BabyJubjubCurve.subOrder, new BigInteger(1, hm));

        FieldElement S = r.add(hmInt.mul(key));

        EdDSASignature sign = new EdDSASignature(R8, S);

        return sign.toByteArray();
    }


    public boolean verify(byte[] msg, byte[] signature,  byte[] pubKey) {
        assert (pubKey.length == 64);
        assert (msg.length == 32);
        assert (signature.length == 96);

        EdDSASignature sign = new EdDSASignature(signature);
        Point A = new Point(pubKey);

        return verify(msg, sign, A);
    }

    public boolean verify(byte[] msg, byte[] signature, Point pubKey) {
        assert (msg.length == 32);
        assert (signature.length == 96);

        EdDSASignature sign = new EdDSASignature(signature);
        return verify(msg, sign, pubKey);
    }

    private boolean verify(byte[] msg, EdDSASignature sign, Point A) {
        Point R = sign.getPointR();
        FieldElement S = sign.getS();

        if (!BabyJubjubCurve.inCurve(R)) return false;
        if (!BabyJubjubCurve.inCurve(A)) return false;
        if (S.v.compareTo(BabyJubjubCurve.subOrder) >= 0) return false;

        poseidonHashEngine.reset();
        poseidonHashEngine.add(R.x.v.toByteArray());
        poseidonHashEngine.add(R.y.v.toByteArray());
        poseidonHashEngine.add(A.x.v.toByteArray());
        poseidonHashEngine.add(A.y.v.toByteArray());
        poseidonHashEngine.add(decode(msg).toByteArray());
        byte[] hm = poseidonHashEngine.digest();
        BigInteger hmInt = new BigInteger(1, hm);

        Point lPoint = BabyJubjubCurve.mulPointEscalar(BabyJubjubCurve.base8, S.v);
        Point rPoint = BabyJubjubCurve.mulPointEscalar(A, hmInt);
        rPoint = BabyJubjubCurve.addPoint(R, rPoint);

        return lPoint.x.equals(rPoint.x) && lPoint.y.equals(rPoint.y);
    }
}
