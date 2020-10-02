package com.loopring.eddsa;

import com.loopring.utils.BigIntLittleEndianEncoding;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Random;

import static org.junit.Assert.*;

public class BabyJubjubCurveTest {

    @Test
    public void CheckInCurve() {
        EddsaPoint[] onCurvePoints = generateOnCurvePoints(100);

        for (EddsaPoint point : onCurvePoints) {
            assertTrue(BabyJubjubCurve.inCurve(point));
        }
    }

    @Test
    public void CheckPointAdd() {
        int size = 100;
        EddsaPoint[] onCurvePoints = generateOnCurvePoints(size);

        for (int i = 0; i < size; i+=2) {
            assertTrue(BabyJubjubCurve.inCurve(onCurvePoints[i]));
            assertTrue(BabyJubjubCurve.inCurve(onCurvePoints[i+1]));
            EddsaPoint newP = BabyJubjubCurve.addPoint(onCurvePoints[i], onCurvePoints[i+1]);
            assertTrue(BabyJubjubCurve.inCurve(newP));
        }
    }

    @Test
    public void CheckPointMuliply() {
        int size = 100;
        assertTrue(BabyJubjubCurve.inCurve(BabyJubjubCurve.base8));

//        for (int i = 0; i < size; i++) {
//            EddsaPoint newP = BabyJubjubCurve.mulPointEscalar(BabyJubjubCurve.base8, BigInteger.valueOf(i));
//            assertTrue(BabyJubjubCurve.inCurve(newP));
//        }

        Random r = new Random();
        byte[] randomBytes = new byte[BabyJubjubCurve.FIELD_SIZE];
        for (int i = 0; i < size; i++) {
            r.nextBytes(randomBytes);
            BigInteger s = new BigInteger(1, randomBytes);
            EddsaPoint newP = BabyJubjubCurve.mulPointEscalar(BabyJubjubCurve.base8, s);
            assertTrue(BabyJubjubCurve.inCurve(newP));
        }
    }

    @Test
    public void CheckPointMuliply_WNAF() {
        int size = 100;
        assertTrue(BabyJubjubCurve.inCurve(BabyJubjubCurve.base8));

        Random r = new Random();
        byte[] randomBytes = new byte[BabyJubjubCurve.FIELD_SIZE];
        for (int i = 0; i < size; i++) {
            r.nextBytes(randomBytes);
            BigInteger s = new BigInteger(1, randomBytes);
            EddsaPoint newP = BabyJubjubCurve.mulPointEscalar_wnaf(BabyJubjubCurve.base8, s);
//            assertTrue(BabyJubjubCurve.inCurve(newP));
        }
    }

    @Test
    public void ValidatePointMuliply_WNAF() {
        int size = 100;
        assertTrue(BabyJubjubCurve.inCurve(BabyJubjubCurve.base8));

        Random r = new Random();
        byte[] randomBytes = new byte[BabyJubjubCurve.FIELD_SIZE];
        for (int i = 0; i < size; i++) {
            r.nextBytes(randomBytes);
            BigInteger s = new BigInteger(1, randomBytes);
            EddsaPoint newP_wnaf = BabyJubjubCurve.mulPointEscalar_wnaf(BabyJubjubCurve.base8, s);
            EddsaPoint newP = BabyJubjubCurve.mulPointEscalar(BabyJubjubCurve.base8, s);
            assertEquals(newP, newP_wnaf);
        }
    }

    @Test
    public void validatePointCompress() {
        for (PointCompressionPair c : getPointCompressTestCases()) {
            BigInteger selfCompress = BigIntLittleEndianEncoding.newInstance().decode(c.point.compress());
            assert BabyJubjubCurve.inCurve(c.point);
            assert selfCompress.equals(c.compressPt);
        }

    }

    private EddsaPoint[] generateOnCurvePoints(int size) {
        // a * x^2 + y^2 = 1 + d * x^2 * y^2
        assertTrue(BabyJubjubCurve.inCurve(BabyJubjubCurve.base8));

        EddsaPoint[] onCurvePts = new EddsaPoint[size];
        Random r = new Random();
        for (int i = 0; i < size; i++) {
            onCurvePts[i] = BabyJubjubCurve.mulPointEscalar(BabyJubjubCurve.base8, new BigInteger(BabyJubjubCurve.BIT_FIELD_SIZE, r));
        }
        return onCurvePts;
    }


    class PointCompressionPair {
        public EddsaPoint point;
        public BigInteger compressPt;

        public PointCompressionPair(EddsaPoint pt, byte[] compressPt) {
            new PointCompressionPair(pt, BigIntLittleEndianEncoding.newInstance().decode(compressPt));
        }

        public PointCompressionPair(EddsaPoint pt, BigInteger compressPt) {
            this.point = pt;
            this.compressPt = compressPt;
        }
    }

    private PointCompressionPair[] getPointCompressTestCases() {
        return new PointCompressionPair[]{
                new PointCompressionPair(
                        new EddsaPoint(
                                new BigInteger("20184909850302436146225569562835990908019865515635408541686028020194730104724", 10),
                                new BigInteger("15159890370000177866689016308497483549920283375406772533476493545204225841457", 10)),
                        new BigInteger("a18431a20aa0d652b3279630004a7ae61bf63198be5db7ed3e456126878a9931", 16)
                ),
//                new PointCompressionPair(
//                        new EddsaPoint(
//                                new BigInteger("14631379960006914369238839597861778511164526324854514835271097969173508180562", 10),
//                                new BigInteger("18360533817047488955917522174657466958617650357333268673735898976173580697030", 10)),
//                        new FieldElement(
//                                BabyJubjubCurve.p,
//                                new BigInteger("18360533817047488955917522174657466958617650357333268673735898976173580697030", 10)
//                        )),
        };
    }

}
