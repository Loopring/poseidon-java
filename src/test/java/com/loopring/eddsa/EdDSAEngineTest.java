package com.loopring.eddsa;

import com.loopring.poseidon.PoseidonHash;
import com.loopring.utils.BigIntLittleEndianEncoding;
import org.junit.Test;

//import java.math.BigInteger;

import java.math.BigInteger;

import static org.junit.Assert.*;

public class EdDSAEngineTest {
    @Test
    public void testEngineSignVerify() {
        EdDSAEngine engine = new EdDSAEngine();

        EdDSAKeyPair key = engine.generateKeyPair();
        byte[] msg = new byte[]{1, 0, 0, 0, 0, 0, 0, 0,
                                0, 0, 0, 0, 0, 0, 0, 0,
                                0, 0, 0, 0, 0, 0, 0, 0,
                                0, 0, 0, 0, 0, 0, 0, 0};

        byte[] sign = engine.sign(key.privateKey(), msg);

        assertEquals(BabyJubjubCurve.FIELD_SIZE*3, sign.length);
        assertTrue(engine.verify(msg, sign, key.publicKey()));
    }

    @Test
    public void testImportedKeyPairs() {
        FieldElement sk = new FieldElement(BabyJubjubCurve.subOrder,
                                            new BigInteger("1018795972161967035259139852407783214760023844479199194395635687306033280272"));

        FieldElement pkX = new FieldElement(BabyJubjubCurve.p,
                                            new BigInteger("16416073411975395190673982159862238683910540258193173307942575622314590043376"));

        FieldElement pkY = new FieldElement(BabyJubjubCurve.p,
                                            new BigInteger("10862070804333336766833470090031140186909883561173798338300378814650286539312"));

        assertTrue(BabyJubjubCurve.inCurve(new Point(pkX, pkY)));

        EdDSAEngine engine = new EdDSAEngine();
        byte[] msg = new byte[]{1, 0, 0, 0, 0, 0, 0, 0,
                                0, 0, 0, 0, 0, 0, 0, 0,
                                0, 0, 0, 0, 0, 0, 0, 0,
                                0, 0, 0, 0, 0, 0, 0, 0};
        EdDSASignature selfSign = new EdDSASignature(engine.sign(sk.toLeBuf(), msg));
//        System.out.println("Self sign x =" + selfSign.getPointR().x.v.toString(10));
//        System.out.println("Self sign y =" + selfSign.getPointR().y.v.toString(10));
//        System.out.println("Self sign s =" + selfSign.getS().v.toString(10));

        FieldElement Rx = new FieldElement(BabyJubjubCurve.p,
                new BigInteger("3166674687637521858468012689758121020116636080101029117458290475648230622506"));

        FieldElement Ry = new FieldElement(BabyJubjubCurve.p,
                new BigInteger("207629657795324857859234085561158563174648781441000792587995561995220079794"));

        FieldElement sign = new FieldElement(BabyJubjubCurve.subOrder,
                new BigInteger("2091871811328635289840733479843828481994307293349704568621677507103143560153"));

        EdDSASignature signature = new EdDSASignature(new Point(Rx, Ry), sign);

        assertTrue(engine.verify(msg, signature.toByteArray(), new Point(pkX, pkY)));
    }

}
