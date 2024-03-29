# Loopring Java Poseidon

A java implementation of poseidon hash, reference is ethsnarks.

## Build & Test
mvn package

## Commandline check
```shell
$java -cp ./lib/ove.blake2b-alpha.0.jar:./target/loopring-poseidon-1.0-SNAPSHOT.jar com.loopring.poseidon.App "sampleinput" 
0x1d498712c162500947c7bf54c665b2e922144f27216227a5c8e44327ab7497d7
```

## Check with reference ethsnarks python
```python
>hex(int.from_bytes(b'sampleinput', 'big'))
>hex(poseidon([0x73616d706c65696e707574], DefaultParams))
0x1d498712c162500947c7bf54c665b2e922144f27216227a5c8e44327ab7497d7
```

## Dependencies
1. blake2b lib: https://github.com/alphazero/blake2b
2. ethsnarks python impl: https://github.com/HarryR/ethsnarks/ethsnarks/poseidon
