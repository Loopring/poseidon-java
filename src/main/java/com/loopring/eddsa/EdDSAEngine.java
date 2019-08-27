package com.loopring.eddsa;

import com.loopring.poseidon.PoseidonHash;
import com.loopring.utils.BigIntLittleEndianEncoding;
import com.loopring.utils.BLAKE512;

import java.math.BigInteger;
import java.security.SecureRandom;

public class EdDSAEngine {
    boolean initialized;
    final int DEFAULT_KEYSIZE = 256;
    private int keySize;
    private SecureRandom random;
    private BigIntLittleEndianEncoding bigIntEncoder;
    private BLAKE512 blake512HashEngine;
    private PoseidonHash poseidonHashEngine;

    public EdDSAEngine(){
    }

    public void initialize(int keySize, SecureRandom r) {
        this.keySize = keySize;
        this.random = r;
        bigIntEncoder = BigIntLittleEndianEncoding.newInstance();
        blake512HashEngine = new BLAKE512();
        PoseidonHash.PoseidonParamsType poseidonParams =
                PoseidonHash.PoseidonParamsType.newInstance(PoseidonHash.Field.SNARK_SCALAR_FIELD, 6, 6, 52,
                        "poseidon", 5, 128);
        poseidonHashEngine = PoseidonHash.Digest.newInstance(poseidonParams);
        initialized = true;
    }

    public EdDSAKeyPair generateKeyPair() {
        if (!initialized)
            initialize(DEFAULT_KEYSIZE, new SecureRandom());

        byte[] seed = new byte[DEFAULT_KEYSIZE/8];
        random.nextBytes(seed);

        BigInteger secretKey = new BigInteger(1, seed).mod(BabyJubjubCurve.subOrder);
        Point publicKey = BabyJubjubCurve.mulPointEscalar(BabyJubjubCurve.base8, secretKey);

        return new EdDSAKeyPair(publicKey, secretKey.toByteArray());
    }

    public byte[] sign(byte[] keyBytes, byte[] msg) {
//  const key = bigInt(strKey);
//  const prv = bigInt.leInt2Buff(key, 32);
//
//  const h1 = createBlakeHash("blake512")
//                .update(prv)
//                .digest();
//  const msgBuff = bigInt.leInt2Buff(bigInt(msg), 32);
//  const rBuff = createBlakeHash("blake512")
//                .update(Buffer.concat([h1.slice(32, 64), msgBuff]))
//    .digest();
//        let r = bigInt.leBuff2int(rBuff);
//        r = r.mod(babyJub.subOrder);
//
//  const A = babyJub.mulPointEscalar(babyJub.Base8, key);
//  const R8 = babyJub.mulPointEscalar(babyJub.Base8, r);
//
//  const hasher = poseidon.createHash(6, 6, 52);
//  const hm = hasher([R8[0], R8[1], A[0], A[1], msg]);
//  const S = r.add(hm.mul(key)).mod(babyJub.subOrder);
//
//  const signature: Signature = {
//                Rx: R8[0].toString(),
//                Ry: R8[1].toString(),
//                s: S.toString()
//  };
//        return signature;
        BigInteger key = new BigInteger(1, keyBytes);
        byte[] prv = bigIntEncoder.encode(key);

        byte[] h1 = blake512HashEngine.digest(prv);
        byte[] msgBuffer = new byte[64];

        assert (h1.length == 32);
        System.arraycopy(h1, 32, msgBuffer, 0, 32);

        assert (msg.length == 32);
        System.arraycopy(msg, 0, msgBuffer, 32, 32);

        blake512HashEngine.reset();
        byte[] rBuff = blake512HashEngine.digest(msgBuffer);
        BigInteger r = bigIntEncoder.decode(rBuff).mod(BabyJubjubCurve.subOrder);

        Point A = BabyJubjubCurve.mulPointEscalar(BabyJubjubCurve.base8, key);
        Point R8 = BabyJubjubCurve.mulPointEscalar(BabyJubjubCurve.base8, r);

        poseidonHashEngine.add(R8.x.toByteArray());
        poseidonHashEngine.add(R8.y.toByteArray());
        poseidonHashEngine.add(A.x.toByteArray());
        poseidonHashEngine.add(A.y.toByteArray());
        poseidonHashEngine.add(msg);

        byte[] hm = poseidonHashEngine.digest();
        BigInteger hmInt = new BigInteger(1, hm);
        BigInteger S = r.add(hmInt.multiply(key)).mod(BabyJubjubCurve.subOrder);

        EdDSASignature sign = new EdDSASignature(R8, S);

        return sign.toByteArray();
    }


    public boolean verify(byte[] msg, byte[] signature,  byte[] pubKey) {
//  const A = [bigInt(pubKey[0]), bigInt(pubKey[1])];
//  const R = [bigInt(sig.Rx), bigInt(sig.Ry)];
//  const S = bigInt(sig.s);
//
//        // Check parameters
//        if (!babyJub.inCurve(R)) return false;
//        if (!babyJub.inCurve(A)) return false;
//        if (S >= babyJub.subOrder) return false;
//
//  const hasher = poseidon.createHash(6, 6, 52);
//  const hm = hasher([R[0], R[1], A[0], A[1], bigInt(msg)]);
//
//  const Pleft = babyJub.mulPointEscalar(babyJub.Base8, S);
//        let Pright = babyJub.mulPointEscalar(A, hm);
//        Pright = babyJub.addPoint(R, Pright);
//
//        if (!Pleft[0].equals(Pright[0])) return false;
//        if (!Pleft[1].equals(Pright[1])) return false;
//
//        return true;
        assert (pubKey.length == 64);
        assert (msg.length == 32);
        assert (signature.length == 96);

        EdDSASignature sign = new EdDSASignature(signature);

        Point A = new Point(pubKey);
        Point R = new Point(sign.rX, sign.rY);
        BigInteger S = new BigInteger(sign.signature);

        if (!BabyJubjubCurve.inCurve(R)) return false;
        if (!BabyJubjubCurve.inCurve(A)) return false;
        if (S.compareTo(BabyJubjubCurve.subOrder) >= 0) return false;

        poseidonHashEngine.reset();
        poseidonHashEngine.add(R.x.toByteArray());
        poseidonHashEngine.add(R.y.toByteArray());
        poseidonHashEngine.add(A.x.toByteArray());
        poseidonHashEngine.add(A.y.toByteArray());
        poseidonHashEngine.add(msg);
        byte[] hm = poseidonHashEngine.digest();
        BigInteger hmInt = new BigInteger(1, hm);

        Point lPoint = BabyJubjubCurve.mulPointEscalar(BabyJubjubCurve.base8, S);
        Point rPoint = BabyJubjubCurve.mulPointEscalar(A, hmInt);
        rPoint = BabyJubjubCurve.addPoint(R, rPoint);

        return lPoint.x.equals(rPoint.x) && lPoint.y.equals(rPoint.y);
    }
}
