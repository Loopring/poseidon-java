package com.loopring.poseidon;

import java.math.BigInteger;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        PoseidonHash.PoseidonParamsType params = PoseidonHash.DefaultParams;
        PoseidonHash.Digest poseidon = PoseidonHash.Digest.newInstance(params);
        poseidon.add(BigInteger.ONE.toByteArray());
        System.out.println( "Hello World! poseidon hash [1] = " + poseidon.digest(false)[0].toString(10) );
    }
}
