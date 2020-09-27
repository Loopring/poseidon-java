package com.loopring.eddsa;

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
}
