package com.loopring.eddsa;

import java.math.BigInteger;

final class Point {

    public BigInteger x;
    public BigInteger y;

    public Point(String xStr, String yStr) {
        this.x = new BigInteger(xStr);
        this.y = new BigInteger(yStr);
    }

    public Point(BigInteger x, BigInteger y) {
        this.x = x;
        this.y = y;
    }

    public Point(byte[] buffer) {
        byte[] xBuf = new byte[BabyJubjubCurve.FIELD_SIZE];
        byte[] yBuf = new byte[BabyJubjubCurve.FIELD_SIZE];

        assert (buffer.length == 2 * BabyJubjubCurve.FIELD_SIZE);
        System.arraycopy(buffer, 0, xBuf, 0, 32);
        System.arraycopy(buffer, 32, yBuf, 0, 32);
        this.x = new BigInteger(1, xBuf);
        this.y = new BigInteger(1, yBuf);
    }

    public Point(byte[] x, byte[] y) {
        this.x = new BigInteger(1, x);
        this.y = new BigInteger(1, y);
    }
}

public class BabyJubjubCurve {

    public static int FIELD_SIZE = 32;

    public static int BIT_FIELD_SIZE = 32 * 8;

    public static BigInteger p = new BigInteger("21888242871839275222246405745257275088548364400416034343698204186575808495617");

    public static Point base8 = new Point(new BigInteger("16540640123574156134436876038791482806971768689494387082833631921987005038935"),
                             new BigInteger("20819045374670962167435360035096875258406992893633759881276124905556507972311"));

    public static BigInteger order = new BigInteger("21888242871839275222246405745257275088614511777268538073601725287587578984328");

    public static BigInteger subOrder = order.shiftRight(3);

    static BigInteger a = new BigInteger("168700");

    static BigInteger d = new BigInteger("168696");

    static FieldElement fieldOp = new FieldElement(p);


    static Point mulPointEscalar(Point pointP, BigInteger e) {
/*
function mulPointEscalar(base, e) {
  let res = [bigInt("0"), bigInt("1")];
  let rem = bigInt(e);
  let exp = base;

  while (!rem.isZero()) {
    if (rem.isOdd()) {
      res = addPoint(res, exp);
    }
    exp = addPoint(exp, exp);
    rem = rem.shr(1);
  }

  return res;
}
 */
        Point res = new Point(BigInteger.ZERO, BigInteger.ONE);
        Point exp = pointP;
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

    static boolean inCurve(Point pointP) {
/*
  const F = bn128.Fr;

  const a = bigInt("168700");
  const d = bigInt("168696");

  const x2 = F.square(P[0]);
  const y2 = F.square(P[1]);

  if (!F.equals(F.add(F.mul(a, x2), y2), F.add(F.one, F.mul(F.mul(x2, y2), d))))
    return false;

  return true;
 */
        BigInteger x2 = fieldOp.mul(pointP.x, pointP.x);
        BigInteger y2 = fieldOp.mul(pointP.y, pointP.y);
        // a * x**2 + y**2 == 1 + d * x**2 * y**2
        BigInteger l = fieldOp.add(fieldOp.mul(a, x2), y2);
        BigInteger r = fieldOp.add(BigInteger.ONE, fieldOp.mul(fieldOp.mul(x2, y2), d));

        assert (l.equals(r));
        return l.equals(r);
    }

    static Point addPoint(Point pointA, Point pointB) {
        // TODO: optimize performance
        // x' = x0*y1 + x1*y0 / (1 + d * x0 * x1 * y0 * y1)
        BigInteger newX = pointA.x.multiply(pointB.y).add(pointB.x.multiply(pointA.y))
                                .multiply(BigInteger.ONE.add(
                                        d.multiply(pointA.x)
                                                .multiply(pointB.x)
                                                .multiply(pointA.y)
                                                .multiply(pointB.y)).modInverse(p)).mod(p);

        // y' = y0*y1 - a * x0 * x1 / (1 - d * x0 * x1 * y0 * y1)
        BigInteger newY = pointA.y.multiply(pointB.y).subtract(a.multiply(pointA.x).multiply(pointB.x))
                                .multiply(BigInteger.ONE.subtract(
                                        d.multiply(pointA.x)
                                                .multiply(pointB.x)
                                                .multiply(pointA.y)
                                                .multiply(pointB.y)).modInverse(p)).mod(p);

        return new Point(newX, newY);
/*
  const q = bn128.r;
  const cta = bigInt("168700");
  const d = bigInt("168696");
  const res = [];

  res[0] = bigInt(
    bigInt(a[0])
      .mul(b[1])
      .add(bigInt(b[0]).mul(a[1]))
      .mul(
        bigInt(
          bigInt("1").add(
            d
              .mul(a[0])
              .mul(b[0])
              .mul(a[1])
              .mul(b[1])
          )
        ).inverse(q)
      )
  ).affine(q);
  res[1] = bigInt(
    bigInt(a[1])
      .mul(b[1])
      .sub(cta.mul(a[0]).mul(b[0]))
      .mul(
        bigInt(
          bigInt("1").sub(
            d
              .mul(a[0])
              .mul(b[0])
              .mul(a[1])
              .mul(b[1])
          )
        ).inverse(q)
      )
  ).affine(q);

  return res;
 */
    }
}
