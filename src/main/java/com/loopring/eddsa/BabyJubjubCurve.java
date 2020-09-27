package com.loopring.eddsa;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;


public class BabyJubjubCurve {

    public static int FIELD_SIZE = 32;

    public static int BIT_FIELD_SIZE = 32 * 8;

    public static BigInteger p = new BigInteger("21888242871839275222246405745257275088548364400416034343698204186575808495617");

    public static EddsaPoint base8 = new EddsaPoint(
            new FieldElement(p, new BigInteger("16540640123574156134436876038791482806971768689494387082833631921987005038935")),
            new FieldElement(p, new BigInteger("20819045374670962167435360035096875258406992893633759881276124905556507972311")));

    public static BigInteger order = new BigInteger("21888242871839275222246405745257275088614511777268538073601725287587578984328");

    public static BigInteger subOrder = order.shiftRight(3);

    public static FieldElement a = new FieldElement(p, new BigInteger("168700"));

    public static FieldElement d = new FieldElement(p, new BigInteger("168696"));

    public static EddsaPoint mulPointEscalar(EddsaPoint pointP, BigInteger e) {
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

    private static int WNAF_WIDTH = 2;
    private static BigInteger bIwNAF_a = BigInteger.valueOf(2).pow(WNAF_WIDTH);
    private static byte bwNAF_a = bIwNAF_a.byteValue();
    private static BigInteger bIwNAF_b = BigInteger.valueOf(2).pow(WNAF_WIDTH - 1);
    private static byte bwNAF_b = bIwNAF_b.byteValue();
    private static List<Integer> wNAF(BigInteger k) {
        /*
        def wNAF(k, width=2):
            # windowed Non-Adjacent-Form
            # https://bristolcrypto.blogspot.com/2015/04/52-things-number-26-describe-naf-scalar.html
            # https://en.wikipedia.org/wiki/Elliptic_curve_point_multiplication#w-ary_non-adjacent_form_(wNAF)_method
            k = int(k)
            a = 2**width
            b = 2**(width-1)
            output = []
            while k > 0:
                if (k % 2) == 1:
                    c = k % a
                    if c > b:
                        k_i = c - a
                    else:
                        k_i = c
                    k = k - k_i
                else:
                    k_i = 0
                output.append(k_i)
                k = k // 2
            return output[::-1]
         */
        int wNafK_i;
        List<Integer> output = new ArrayList<>();
        while (k.compareTo(BigInteger.ZERO) > 0) {
            if (k.testBit(0)) {
                byte wNAF_c = k.mod(bIwNAF_a).byteValue();
                wNafK_i = wNAF_c;
                if (wNAF_c > bwNAF_b) {
                    wNafK_i = wNAF_c - bwNAF_a;
                }
                k = k.subtract(BigInteger.valueOf(wNafK_i));
            } else {
                wNafK_i = 0;
            }
            output.add(wNafK_i);
            k = k.shiftRight(1);
        }
        return output;
    }

    public static EddsaPoint mulPointEscalar_wnaf(EddsaPoint pointP, BigInteger e) {
        EddsaPoint res = new EddsaPoint(BigInteger.ZERO, BigInteger.ONE);
        EddsaPoint exp = pointP;
        List<Integer> wNaf_array = wNAF(e);
        /*
            for k_i in wNAF(scalar):
                a = a.double()
                if k_i == 1:
                    a = a.add(point)
                elif k_i == -1:
                    a = a.add(point.neg())
            return a
         */
        // Generate an iterator. Start just after the last element.
        ListIterator<Integer> iter = wNaf_array.listIterator(wNaf_array.size());

        // Iterate in reverse.
        while(iter.hasPrevious()) {
            res = addPoint(res, res);
            Integer ki = iter.previous();
            if (ki == 1) {
                res = addPoint(res, exp);
            } else if (ki == -1) {
                res = addPoint(res, exp.neg());
            }
        }

        return res;
    }

    public static boolean inCurve(EddsaPoint pointP) {
        FieldElement x2 = pointP.x.square();
        FieldElement y2 = pointP.y.square();
        // check iff a * x**2 + y**2 == 1 + d * x**2 * y**2
        FieldElement l = x2.mul(a).add(y2);
        FieldElement one = new FieldElement(BabyJubjubCurve.p, BigInteger.ONE);
        FieldElement r = one.add(d.mul(x2).mul(y2));

        assert (l.equals(r));
        return l.equals(r);
    }

    public static EddsaPoint addPoint(EddsaPoint pointA, EddsaPoint pointB) {
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
