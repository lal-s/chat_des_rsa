## secure chat program

This is a secure chatting system for two users.
User1 generates the DES key and shares with the other user after encrypting using RSA.
Once the users both have the same key they use it to securely send encrypted messages over the network and
encrypt and decrypt them on their machines.

This kind of system is called [hybrid cryptosystem](https://en.wikipedia.org/wiki/Hybrid_cryptosystem)


* To know all the command line options supported by the program, run:
```
java CHAT -h
```

When Alice and Bob communicate with each other:

* Alice runs the following command to start, and lays down to wait for Bob to come online
```
java CHAT --alice -a private_key_alice -m alice_modulus -b public_key_bob -n bob_modulus -p port -i ip_address
```

* When Bob starts up, he initializes the hybrid protocol, by generating a DES key, encrypting it
with Alice’s public key, and sending her the encrypted package by using the following command:
```
java CHAT --bob -b private_key_bob -n bob_modulus -a public_key_alice -m alice_modulus -p port -i ip_address:
```

* Alice returns the string “OK” (encrypted) to Bob.
* Once they are done with this simple handshake, they exchange messages read from the command
line, encrypted with DES in CBC mode.
