package com.loopring.eddsa;

import java.math.BigInteger;

public class FieldElement {

    BigInteger p;

    public FieldElement(BigInteger p) {
        this.p = p;
    }

    public BigInteger add(BigInteger a, BigInteger b) {
        return a.add(b).mod(p);
    }

    public BigInteger sub(BigInteger a, BigInteger b) {
        return a.subtract(a).mod(p);
    }

    public BigInteger mul(BigInteger a, BigInteger b) {
        return a.multiply(b).mod(p);
    }

    public BigInteger inv(BigInteger a) {
        return a.modInverse(p);
    }
}
