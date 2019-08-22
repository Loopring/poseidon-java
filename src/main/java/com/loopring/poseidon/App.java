package com.loopring.poseidon;

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
        if (args.length > 1) {
            bytes = args[1].getBytes();
        }
        poseidon.add(bytes);
        System.out.println( "default poseidon hash of <" + new String(bytes) + "> = 0x" + poseidon.digest(false)[0].toString(16) );
    }
}
