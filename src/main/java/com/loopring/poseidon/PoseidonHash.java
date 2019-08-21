package com.loopring.poseidon;

import java.math.BigInteger;

import ove.crypto.digest.Blake2b;

class Field {
    static final BigInteger SNARK_SCALAR_FIELD = new BigInteger("21888242871839275222246405745257275088548364400416034343698204186575808495617");
}

class PoseidonParamsType {
    public BigInteger p, e;
    public int t, nRoundsF, nRoundsP;
    BigInteger[] constants_C;
    BigInteger[][] constants_M;
    public String seed;

    private final static int byteLength = 32;

    private PoseidonParamsType(BigInteger p, int t, int nRoundsF, int nRoundsP, String seed, int e,
            BigInteger[] constants_C, BigInteger[][] constants_M) {
        this.p = p;
        this.t = t;
        this.nRoundsF = nRoundsF;
        this.nRoundsP = nRoundsP;
        this.seed = seed;
        this.e = BigInteger.valueOf(e);
        this.constants_C = constants_C;
        this.constants_M = constants_M;

        // make sure big int byte length is OK.
        assert byteLength >= p.bitLength() >> 3;
    }

    static public PoseidonParamsType CreatePoseidonParams(BigInteger p, int t, int nRoundsF, int nRoundsP, String seed,
            int e, BigInteger[] constants_C, BigInteger[][] constants_M, int security_target) {
        assert (nRoundsF % 2 == 0 && nRoundsF > 0);
        assert (nRoundsP > 0);
        assert (t >= 2);

        int n = p.bitLength() - 1;
        int M = n;
        if (security_target >= 0) {
            M = security_target;
        }
        assert (n >= M);

//        int N = n * t;

        double grobner_attack_ratio_rounds = 0.0;
        double grobner_attack_ratio_sboxes = 0.0;
        double interpolation_attack_ratio = 0.0;
        if (p.mod(BigInteger.valueOf(2)).intValue() == 3) {
            assert (e == 3);
            grobner_attack_ratio_rounds = 0.32;
            grobner_attack_ratio_sboxes = 0.18;
            interpolation_attack_ratio = 0.63;
        } else if (p.mod(BigInteger.valueOf(5)).intValue() != 1) {
            assert (e == 5);
            grobner_attack_ratio_rounds = 0.21;
            grobner_attack_ratio_sboxes = 0.14;
            interpolation_attack_ratio = 0.43;
        } else {
            // XXX: in other cases use, can we use 7?
            throw new IllegalArgumentException("Invalid p for congruency");
        }

        // Verify that the parameter choice exceeds the recommendations to prevent
        // attacks
        // iacr.org/2019/458 § 3 Cryptanalysis Summary of Starkad and Poseidon Hashes
        // (pg 10)
        // Figure 1
        System.out.println("(nRoundsF + nRoundsP) = " + (nRoundsF + nRoundsP));
        System.out.println("Interpolation Attackable Rounds = "
                + ((interpolation_attack_ratio * Math.min(n, M)) + Math.log(t) / Math.log(2)));
        assert ((nRoundsF + nRoundsP) > ((interpolation_attack_ratio * Math.min(n, M)) + Math.log(t) / Math.log(2)));
        // Figure 3
        System.out.println("grobner_attack_ratio_rounds = " + ((2 + Math.min(M, n)) * grobner_attack_ratio_rounds));
        assert ((nRoundsF + nRoundsP) > ((2 + Math.min(M, n)) * grobner_attack_ratio_rounds));
        // Figure 4
        System.out.println("grobner_attack_ratio_sboxes = " + (M * grobner_attack_ratio_sboxes));
        assert ((nRoundsF + (t * nRoundsP)) > (M * grobner_attack_ratio_sboxes));

        // iacr.org/2019/458 § 4.1 Minimize "Number of S-Boxes"
        // In order to minimize the number of S-boxes for given `n` and `t`, the goal is
        // to and
        // the best ratio between RP and RF that minimizes:
        // number of S-Boxes = t · RF + RP
        // - Use S-box x^q
        // - Select R_F to 6 or rhigher
        // - Select R_P that minimizes tRF +RP such that no inequation (1),(3),(4),(5)
        // is satisfied.
        if (constants_C == null || constants_C.length == 0) {
            constants_C = poseidon_constants(p, seed + "_constants", nRoundsF + nRoundsP);
        }

        if (constants_M == null || constants_M.length == 0) {
            constants_M = poseidon_matrix(p, seed + "_matrix_0000", t);
        }

        // iacr.org/2019/458 § 4.1 6 SNARKs Application via Poseidon-π
        // page 16 formula (8) and (9)
        int n_constraints = (nRoundsF * t) + nRoundsP;
        if (e == 5) {
            n_constraints *= 3;
        } else if (e == 3) {
            n_constraints *= 2;
        }
        System.out.println("n_constraints = " + n_constraints);

        return new PoseidonParamsType(p, t, nRoundsF, nRoundsP, seed, e, constants_C, constants_M);
    }

    static byte[] getIntBytes(BigInteger input, int size, String mode) {
        byte[] src = input.toByteArray();
        byte[] dst = new byte[size];

//        assert (src.length <= size);

        if (mode == "little") {
            for (int i = size - 1; i >= 0; i--){
                if (i < src.length){
                    dst[i] = src[src.length - 1 - i];
                } else {
                    dst[i] = 0;
                }
            }
        } else {
            for (int i = 0; i < size; i++){
                if (i >= size - src.length){
                    dst[i] = src[i - size + src.length];
                } else {
                    dst[i] = 0;
                }
            }
        }

        return dst;
    }

    static BigInteger toBigInteger(byte[] src, int size, String mode) {
        if (mode == "little") {
            for(int i = 0; i < src.length / 2; i++)
            {
                byte temp = src[i];
                src[i] = src[src.length - i - 1];
                src[src.length - i - 1] = temp;
            }
            return new BigInteger(1, src);
        }

        return new BigInteger(1, src);
    }

    static BigInteger H(BigInteger input) {
        // input should be positive, otherwise 0xFFFF....FF will be interpreted as -1
        assert (input.compareTo(BigInteger.ZERO) >= 0);
        byte[] buf = getIntBytes(input, byteLength, "little");
        return H(buf);
    }

    static BigInteger H(byte[] input) {
        // if isinstance(arg, int):
        // arg = arg.to_bytes(32, 'little')
        // hashed = blake2b(data=arg, digest_size=32).digest()
        // return int.from_bytes(hashed, 'little')
        Blake2b.Param param = new Blake2b.Param().
                setDigestLength(byteLength);
        final Blake2b blake2b = Blake2b.Digest.newInstance( param );
        blake2b.reset();
        blake2b.update(input);
        byte[] hashBuf = blake2b.digest();
        return toBigInteger(hashBuf, byteLength, "little");
    }

    static BigInteger[] poseidon_constants(BigInteger p, String seed, int n) {
        BigInteger[] constants = new BigInteger[n];
        BigInteger newSeed = H(seed.getBytes());
        constants[0] = newSeed.mod(p);
        for (int i = 1; i < n; i++) {
            newSeed = H(newSeed);
            constants[i] = newSeed.mod(p);
        }
        return constants;
    }

    static BigInteger[][] poseidon_matrix(BigInteger p, String seed, int t) {
        // """
        // iacr.org/2019/458 § 2.3 About the MDS Matrix (pg 8)
        // Also:
        // - https://en.wikipedia.org/wiki/Cauchy_matrix
        // """
        BigInteger[] c = poseidon_constants(p, seed, t * 2);
        BigInteger[][] matrix = new BigInteger[t][t];
        // c = list(poseidon_constants(p, seed, t * 2))
        // return [[pow((c[i] - c[t+j]) % p, p - 2, p) for j in range(t)]
        // for i in range(t)]
        for (int i = 0; i < t; i++) {
            for (int j = 0; j < t; j++) {
                matrix[i][j] = c[i].subtract(c[t + j]).mod(p).modInverse(p);
            }
        }
        return matrix;
    }
}

class Poseidon {

    static PoseidonParamsType DefaultParams = PoseidonParamsType.CreatePoseidonParams(Field.SNARK_SCALAR_FIELD, 6, 8,
            57, "poseidon", 5, null, null, 126);

    // TODO: chain return value.
    static public BigInteger hash(BigInteger[] inputs, PoseidonParamsType params, Boolean chained, Boolean trace) {
        return hash(inputs, params, trace)[0];
    }

    static public BigInteger[] hash(BigInteger[] inputs, PoseidonParamsType params, Boolean trace) {
        if (params == null) {
            params = DefaultParams;
        }

        assert (inputs != null && inputs.length != 0);
        assert (inputs.length < params.t);

        BigInteger[] states = new BigInteger[params.t];
        System.arraycopy(inputs, 0, states, 0, inputs.length);
        for (int i = inputs.length; i < params.t; i++) {
            states[i] = BigInteger.valueOf(0);
        }

        // for i, C_i in enumerate(params.constants_C):
        // state = [_ + C_i for _ in state] # ARK(.)
        // poseidon_sbox(state, i, params)
        // state = poseidon_mix(state, params.constants_M, params.p)
        // if trace:
        // for j, val in enumerate(state):
        // print('%d %d' % (i, j), '=', val)

        for (int i = 0; i < params.constants_C.length; i++) {
            for (int ti = 0; ti < states.length; ti++) {
                states[ti] = states[ti].add(params.constants_C[i]);
            }
            poseidon_sbox(states, i, params);
            states = poseidon_mix(states, params.constants_M, params.p);

            if (trace) {
                int j = 0;
                for (BigInteger state : states) {
                    System.out.println("trace: " + i + " + " + j + "=" + state);
                    j++;
                }
            }
        }

        return states;
    }

    static void poseidon_sbox(BigInteger[] states, int i, PoseidonParamsType params) {
        // """
        // iacr.org/2019/458 § 2.2 The Hades Strategy (pg 6)

        // In more details, assume R_F = 2 · R_f is an even number. Then
        // - the first R_f rounds have a full S-Box layer,
        // - the middle R_P rounds have a partial S-Box layer (i.e., 1 S-Box layer),
        // - the last R_f rounds have a full S-Box layer
        // """
        int half_F = params.nRoundsF / 2;
        BigInteger e = params.e;
        BigInteger p = params.p;

        // half_F = params.nRoundsF // 2
        // e, p = params.e, params.p
        // if i < half_F or i >= (half_F + params.nRoundsP):
        // for j, _ in enumerate(state):
        // state[j] = pow(_, e, p)
        // else:
        // state[0] = pow(state[0], e, p)
        if (i < half_F || i >= half_F + params.nRoundsP) {
            int j = 0;
            for (BigInteger state : states) {
                states[j] = state.modPow(e, p);
                j++;
            }
        } else {
            states[0] = states[0].modPow(e, p);
        }

    }

    static private BigInteger[] poseidon_mix(BigInteger[] states, BigInteger[][] M, BigInteger p) {
        // """
        // The mixing layer is a matrix vector product of the state with the mixing matrix
        //  - https://mathinsight.org/matrix_vector_multiplication
        // """
        // return [ sum([M[i][j] * _ for j, _ in enumerate(state)]) % p 
        //          for i in range(len(M)) ]
        BigInteger[] newStates = new BigInteger[states.length];
        for (int i = 0; i < newStates.length; i++) {
            BigInteger sum = BigInteger.ZERO;
            for (int j = 0; j < states.length; j++){
                sum = sum.add(M[i][j].multiply(states[j])).mod(p);
            }
            newStates[i] = sum;
        }
        return newStates;
    }

}
