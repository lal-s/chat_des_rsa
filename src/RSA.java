package src;

import gnu.getopt.Getopt;
import java.math.BigInteger;
import java.util.Random;

public class RSA {

  public static void main(String[] args) {

    StringBuilder bitSizeStr = new StringBuilder();
    StringBuilder nStr = new StringBuilder();
    StringBuilder dStr = new StringBuilder();
    StringBuilder eStr = new StringBuilder();
    StringBuilder kStr = new StringBuilder();
    StringBuilder m = new StringBuilder();

    pcl(args, bitSizeStr, nStr, dStr, eStr, m, kStr);

    if (!bitSizeStr.toString().equalsIgnoreCase("") || kStr.toString()
        .equalsIgnoreCase("k")) {//This means you want to create a new key
      genRSAkey(bitSizeStr);
    }

    if (!eStr.toString().equalsIgnoreCase("")) {
      RSAencrypt(m, nStr, eStr);
    }

    if (!dStr.toString().equalsIgnoreCase("")) {
      RSAdecrypt(m, nStr, dStr);
    }
  }

  private static void RSAencrypt(StringBuilder m, StringBuilder nStr, StringBuilder eStr) {

    BigInteger m_b = new BigInteger(m.toString());
    BigInteger n_b = new BigInteger(nStr.toString());
    BigInteger e_b = new BigInteger(eStr.toString());

    System.out.println("Encrypted: " + m_b.modPow(e_b, n_b));
  }

  private static void RSAdecrypt(
      StringBuilder cStr, StringBuilder nStr,
      StringBuilder dStr) {

    BigInteger c_b = new BigInteger(cStr.toString());
    BigInteger n_b = new BigInteger(nStr.toString());
    BigInteger d_b = new BigInteger(dStr.toString());

    System.out.println("Decrypted: " + c_b.modPow(d_b, n_b));
  }

  private static BigInteger gcd(BigInteger a, BigInteger b) {
    BigInteger t;
    while (b.compareTo(new BigInteger("0")) != 0) {
      t = a;
      a = b;
      b = t.mod(b);
    }
    return a;
  }

  private static void genRSAkey(StringBuilder bitSizeStr) {
    // TODO Auto-generated method stub
    BigInteger p;
    int bitLength;
    if (bitSizeStr.length() != 0) {

      bitLength = Integer.valueOf(bitSizeStr.toString());
    } else {
      bitLength = 1024;
    }
    Random rnd = new Random();
    p = BigInteger.probablePrime(bitLength, rnd);
    BigInteger q;
    Random rnd2 = new Random();

    q = BigInteger.probablePrime(bitLength, rnd2);

    System.out.println("P= " + p);
    System.out.println("Q= " + q);
    BigInteger n = p.multiply(q);
    System.out.println("N= " + n);
    BigInteger one = new BigInteger("1");
    BigInteger ph = (p.subtract(one)).multiply(q.subtract(one));
    System.out.println("ph= " + ph);
    BigInteger e = new BigInteger("0");
    BigInteger i = new BigInteger("3");
    while (gcd(ph, i).compareTo(new BigInteger("1")) != 0)

    {
      i = i.add(new BigInteger("2"));
    }
    e = i;

    System.out.println("e= " + e);

    BigInteger b11, b22, b33;
    BigInteger d;

    d = e.modInverse(ph);

    System.out.println("d= " + d);

    System.out.println("Public Key: (e,n) , e= " + e + " n= " + n);
    System.out.println("Private Key: (d,n) , d= " + d + " n= " + n);
  }

  /**
   * This function Processes the Command Line Arguments.
   */
  private static void pcl(
      String[] args, StringBuilder bitSizeStr,
      StringBuilder nStr, StringBuilder dStr, StringBuilder eStr,
      StringBuilder m, StringBuilder kStr) {
    Getopt g = new Getopt("Chat Program", args, "hke:d:b:n:i:");
    int c;
    String arg;
    while ((c = g.getopt()) != -1) {
      switch (c) {
        case 'i':
          arg = g.getOptarg();
          m.append(arg);
          break;
        case 'e':
          arg = g.getOptarg();
          eStr.append(arg);
          break;
        case 'n':
          arg = g.getOptarg();
          nStr.append(arg);
          break;
        case 'd':
          arg = g.getOptarg();
          dStr.append(arg);
          break;
        case 'k':
          kStr.append("k");

          break;
        case 'b':
          arg = g.getOptarg();
          bitSizeStr.append(arg);
          break;
        case 'h':
          callUsage(0);
        case '?':
          break; // getopt() already printed an error
        default:
          break;
      }
    }
  }

  private static void callUsage(int exitStatus) {

    System.out.println("-k: generates key of 1024 bits");
    System.out.println("-b: specifies desired key length");
    System.out.println("-e: encrypts the message with the public key");
    System.out.println("-d: decrypts the message with the private key");
    System.out.println("-i: specifies the message to be encrypted/decrypted");
    System.err.println("");
    System.exit(exitStatus);
  }
}
