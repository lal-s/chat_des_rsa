## chat program

Alice and Bob communicate with each other by sharing the DES key after encrypting using RSA and then using the DES to encrypt and decrypt the messages

### java CHAT -h:
* This contains all the command line options supported by the program.

### java CHAT --alice -a private_key_alice -m alice_modulus -b public_key_bob -n bob_modulus -p port -i ip_address:
* This is what Alice runs
* She starts up first, and lays down to wait for Bob to come online

### java CHAT --bob -b private_key_bob -n bob_modulus -a public_key_alice -m alice_modulus -p port -i ip_address:
* This is what Bob  runs
* When Bob starts up, he initializes the hybrid protocol, by generating a DES key, encrypting it
with Alice’s public key, and sending her the encrypted package.
* Alice returns the string “OK” (encrypted) to Bob.
* Once they are done with this simple handshake, they exchange messages read from the command
line, encrypted with DES in CBC mode.
