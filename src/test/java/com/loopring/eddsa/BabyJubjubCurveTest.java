package com.loopring.eddsa;

import com.loopring.utils.BigIntLittleEndianEncoding;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Random;

import static org.junit.Assert.*;

public class BabyJubjubCurveTest {

    @Test
    public void CheckInCurve() {
        Point[] onCurvePoints = generateOnCurvePoints(100);

        for (Point point : onCurvePoints) {
            assertTrue(BabyJubjubCurve.inCurve(point));
        }
    }

    @Test
    public void CheckPointAdd() {
        int size = 100;
        Point[] onCurvePoints = generateOnCurvePoints(size);

        for (int i = 0; i < size; i+=2) {
            assertTrue(BabyJubjubCurve.inCurve(onCurvePoints[i]));
            assertTrue(BabyJubjubCurve.inCurve(onCurvePoints[i+1]));
            Point newP = BabyJubjubCurve.addPoint(onCurvePoints[i], onCurvePoints[i+1]);
            assertTrue(BabyJubjubCurve.inCurve(newP));
        }
    }

    @Test
    public void CheckPointMuliply() {
        int size = 100;
        assertTrue(BabyJubjubCurve.inCurve(BabyJubjubCurve.base8));

//        for (int i = 0; i < size; i++) {
//            Point newP = BabyJubjubCurve.mulPointEscalar(BabyJubjubCurve.base8, BigInteger.valueOf(i));
//            assertTrue(BabyJubjubCurve.inCurve(newP));
//        }

        Random r = new Random();
        byte[] randomBytes = new byte[BabyJubjubCurve.FIELD_SIZE];
        for (int i = 0; i < size; i++) {
            r.nextBytes(randomBytes);
            BigInteger s = new BigInteger(1, randomBytes);
            Point newP = BabyJubjubCurve.mulPointEscalar(BabyJubjubCurve.base8, s);
            assertTrue(BabyJubjubCurve.inCurve(newP));
        }
    }

    private Point[] generateOnCurvePoints(int size) {
        // a * x^2 + y^2 = 1 + d * x^2 * y^2
        assertTrue(BabyJubjubCurve.inCurve(BabyJubjubCurve.base8));

        Point[] onCurvePts = new Point[size];
        Random r = new Random();
        for (int i = 0; i < size; i++) {
            onCurvePts[i] = BabyJubjubCurve.mulPointEscalar(BabyJubjubCurve.base8, new BigInteger(BabyJubjubCurve.BIT_FIELD_SIZE, r));
        }
        return onCurvePts;
    }
}
