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
        EddsaPoint publicKey = BabyJubjubCurve.mulPointEscalar(BabyJubjubCurve.base8, secretKey.v);

        return new EdDSAKeyPair(publicKey.x.toLeBuf(),
                                publicKey.y.toLeBuf(),
                                secretKey.toLeBuf());
    }

    /*
     * generateJsCompatibleKeyPair generates js compatible key pair, which means use string bytes value
     * instead of string number.
     * For example, leBuffer '12' to int is 49 + 50*256, rather than intuitively 1 + 2*256.
     * See decodeJsLeBuffToBigInt for detail
     * That to make sure we have unified key pairs between java and js.
     */
    public EdDSAKeyPair generateJsCompatibleKeyPair(String jsLeBigIntBuffer) {
        BigInteger keySeed = decodeJsLeBuffToBigInt(jsLeBigIntBuffer);
        FieldElement secretKey = new FieldElement(BabyJubjubCurve.subOrder, keySeed);
        EddsaPoint publicKey = BabyJubjubCurve.mulPointEscalar(BabyJubjubCurve.base8, secretKey.v);

        return new EdDSAKeyPair(publicKey.x.toLeBuf(), publicKey.y.toLeBuf(), secretKey.toLeBuf());
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
        EddsaPoint A = BabyJubjubCurve.mulPointEscalar(BabyJubjubCurve.base8, key.v);
        EddsaPoint R8 = BabyJubjubCurve.mulPointEscalar(BabyJubjubCurve.base8, r.v);

        poseidonHashEngine.add(R8.x.v);
        poseidonHashEngine.add(R8.y.v);
        poseidonHashEngine.add(A.x.v);
        poseidonHashEngine.add(A.y.v);
        poseidonHashEngine.add(decode(msg));

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
        EddsaPoint A = new EddsaPoint(pubKey);

        return verify(msg, sign, A);
    }

    public boolean verify(byte[] msg, byte[] signature, EddsaPoint pubKey) {
        assert (msg.length == 32);
        assert (signature.length == 96);

        EdDSASignature sign = new EdDSASignature(signature);
        return verify(msg, sign, pubKey);
    }

    private boolean verify(byte[] msg, EdDSASignature sign, EddsaPoint A) {
        EddsaPoint R = sign.getPointR();
        FieldElement S = sign.getS();

        if (!BabyJubjubCurve.inCurve(R)) return false;
        if (!BabyJubjubCurve.inCurve(A)) return false;
        if (S.v.compareTo(BabyJubjubCurve.subOrder) >= 0) return false;

        poseidonHashEngine.reset();
        poseidonHashEngine.add(R.x.v);
        poseidonHashEngine.add(R.y.v);
        poseidonHashEngine.add(A.x.v);
        poseidonHashEngine.add(A.y.v);
        poseidonHashEngine.add(decode(msg));
        byte[] hm = poseidonHashEngine.digest();
        BigInteger hmInt = new BigInteger(1, hm);

        EddsaPoint lPoint = BabyJubjubCurve.mulPointEscalar(BabyJubjubCurve.base8, S.v);
        EddsaPoint rPoint = BabyJubjubCurve.mulPointEscalar(A, hmInt);
        rPoint = BabyJubjubCurve.addPoint(R, rPoint);

        return lPoint.x.equals(rPoint.x) && lPoint.y.equals(rPoint.y);
    }
}
