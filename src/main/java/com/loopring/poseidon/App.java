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
        BigInteger[] inputs = new BigInteger[]{BigInteger.ONE};
        System.out.println( "Hello World! hash = " + Poseidon.hash(inputs, null, false, false) );
    }
}
