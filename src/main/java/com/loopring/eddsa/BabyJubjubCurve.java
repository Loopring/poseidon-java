package com.loopring.eddsa;

import java.math.BigInteger;


public class BabyJubjubCurve {

    public static int FIELD_SIZE = 32;

    public static int BIT_FIELD_SIZE = 32 * 8;

    public static BigInteger p = new BigInteger("21888242871839275222246405745257275088548364400416034343698204186575808495617");

    public static EddsaPoint base8 = new EddsaPoint(
            new FieldElement(p, new BigInteger("16540640123574156134436876038791482806971768689494387082833631921987005038935")),
            new FieldElement(p, new BigInteger("20819045374670962167435360035096875258406992893633759881276124905556507972311")));

    public static BigInteger order = new BigInteger("21888242871839275222246405745257275088614511777268538073601725287587578984328");

    public static BigInteger subOrder = order.shiftRight(3);

    static FieldElement a = new FieldElement(p, new BigInteger("168700"));

    static FieldElement d = new FieldElement(p, new BigInteger("168696"));

    static EddsaPoint mulPointEscalar(EddsaPoint pointP, BigInteger e) {
        EddsaPoint res = new EddsaPoint(BigInteger.ZERO, BigInteger.ONE);
        EddsaPoint exp = pointP;
        BigInteger rem = e;

        while (rem.compareTo(BigInteger.ZERO) != 0) {
            if (rem.testBit(0)) {
                res = addPoint(res, exp);
            }
            exp = addPoint(exp, exp);
            rem = rem.shiftRight(1);
        }
        return res;
    }

    static boolean inCurve(EddsaPoint pointP) {
        FieldElement x2 = pointP.x.square();
        FieldElement y2 = pointP.y.square();
        // check iff a * x**2 + y**2 == 1 + d * x**2 * y**2
        FieldElement l = x2.mul(a).add(y2);
        FieldElement one = new FieldElement(BabyJubjubCurve.p, BigInteger.ONE);
        FieldElement r = one.add(d.mul(x2).mul(y2));

        assert (l.equals(r));
        return l.equals(r);
    }

    static EddsaPoint addPoint(EddsaPoint pointA, EddsaPoint pointB) {
        // TODO: optimize performance
        FieldElement one = new FieldElement(BabyJubjubCurve.p, BigInteger.ONE);
        FieldElement x0 = pointA.x;
        FieldElement y0 = pointA.y;
        FieldElement x1 = pointB.x;
        FieldElement y1 = pointB.y;

        // dxxyy = d * x0 * x1 * y0 * y1
        // x' = x0*y1 + x1*y0 / (1 + dxxyy)
        FieldElement dxxyy = d.mul(x0).mul(x1).mul(y0).mul(y1);
        FieldElement newX = x0.mul(y1).add(x1.mul(y0)).mul(
                one.add(dxxyy).inv());
        // y' = y0*y1 - a * x0 * x1 / (1 - dxxyy)
        FieldElement newY = y0.mul(y1).sub(a.mul(x0).mul(x1)).mul(
                one.sub(dxxyy).inv());

        return new EddsaPoint(newX, newY);
    }
}
