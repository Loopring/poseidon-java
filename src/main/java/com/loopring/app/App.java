package com.loopring.app;

import com.loopring.poseidon.PoseidonHash;

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
        byte[] bytes = "hello world!".getBytes();
        if (args.length > 0) {
            bytes = args[0].getBytes();
        }
        poseidon.add(new BigInteger(1, bytes));
        System.out.println( "default poseidon hash of <" + new String(bytes) + "> = 0x" + poseidon.digest(false)[0].toString(16) );
    }
}
