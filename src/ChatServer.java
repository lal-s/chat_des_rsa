package src;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.Scanner;

public class ChatServer {
  static String host;
  static int port;
  static Socket s;
  static String username;

  static String privateKeyAlice;
  static String privateKeyBob;
  static String publicKeyAlice;
  static String publicKeyBob;
  static String aliceModulus;
  static String bobModulus;
  static int step = 0;
  static String handshake_complete = "no";

  static String key_hex_global;

  public static void main(String[] args) throws IOException {

    @SuppressWarnings("resource")
    Scanner keyboard = new Scanner(System.in);
    pcl(args);

    setupServer();

    System.out.println("Welcome to encrypted chat program.\nChat starting below:");

    ChatListenter chatListener = new ChatListenter();
    chatListener.start();

    PrintStream output = null;
    try {
      output = new PrintStream(s.getOutputStream());
    } catch (IOException e) {
      e.printStackTrace();
    }

    String input = "";
    while (true) {

      input = keyboard.nextLine();
      if (handshake_complete == "yes") {
        String encrypt_input = DESChat.encrypt(key_hex_global, input);
        input = username + ":" + encrypt_input;
      } else {
        input = username + ":" + input;
      }
      output.println(input);
      output.flush();
    }
  }

  /**
   * Upon running this function it first tries to make a connection on
   * the given ip:port pairing. If it find another client, it will accept
   * and leave function.
   * If there is no client found then it becomes the listener and waits for
   * a new client to join on that ip:port pairing.
   */
  private static void setupServer() {
    try {
      s = new Socket(host, port);
    } catch (IOException e1) {
      System.out
          .println("There is no other client on this IP:port pairing, waiting for them to join.");

      try {
        ServerSocket listener = new ServerSocket(port);
        s = listener.accept();
        listener.close();
      } catch (IOException e) {
        e.printStackTrace();
        System.exit(1);
      }
    }
    System.out.println("Client Connected.");
    System.out.println("I AM " + username);
  }

  /**
   * This function Processes the Command Line Arguments.
   * Right now the three accepted Arguments are:
   * -p for the port number you are using
   * -i for the IP address/host name of system
   * -h for calling the usage statement.
   */
  private static void pcl(String[] args) {
    LongOpt[] longopts = new LongOpt[2];
    longopts[0] = new LongOpt("alice", LongOpt.NO_ARGUMENT, null, 1);
    longopts[1] = new LongOpt("bob", LongOpt.NO_ARGUMENT, null, 2);
    Getopt g = new Getopt("Chat Program", args, "p:i:a:b:m:n:", longopts);
    int c;
    String arg;
    while ((c = g.getopt()) != -1) {
      switch (c) {
        case 1:
          username = "alice";
          break;
        case 2:
          username = "bob";
          break;
        case 'p':
          arg = g.getOptarg();
          port = Integer.parseInt(arg);
          break;
        case 'i':
          arg = g.getOptarg();
          host = arg;
          break;
        case 'a':
          arg = g.getOptarg();
          if (username == "alice") {
            privateKeyAlice = arg;
          }
          if (username == "bob") {
            publicKeyAlice = arg;
          }
          break;
        case 'm':
          arg = g.getOptarg();
          aliceModulus = arg;
          break;
        case 'b':
          arg = g.getOptarg();
          if (username == "alice") {
            publicKeyBob = arg;
          }
          if (username == "bob") {
            privateKeyBob = arg;
          }
          break;
        case 'n':
          arg = g.getOptarg();
          bobModulus = arg;
          break;
        case 'h':
          callUsage(0);
        case '?':
          break;
        default:
          break;
      }
    }
  }

  /**
   * A helper function that prints out the useage help statement
   * and exits with the given exitStatus
   * @param exitStatus
   */
  private static void callUsage(int exitStatus) {

    String useage = "";
    System.out.println("--username specifies the name of the chat user");
    System.out.println("-n: specifies bobs modulus");
    System.out.println("-m: specifies alices modulus");
    System.out.println("-a: specifies alices public/private key");
    System.out.println("-b: specifies bobs public/private key");
    System.out.println("-p: specifies port number");
    System.out.println("-i: specifies ip address");
    System.err.println(useage);
    System.exit(exitStatus);
  }

  /**
   * A private class which runs as a thread listening to the other
   * client. It prints out the message on screen.
   */
  static private class ChatListenter implements Runnable {
    private Thread t;

    ChatListenter() {

    }

    @Override
    public void run() {
      BufferedReader input = null;

      try {
        input = new BufferedReader(new InputStreamReader(s.getInputStream()));
      } catch (IOException e1) {
        e1.printStackTrace();
        System.err.println("System would not make buffer reader");
        System.exit(1);
      }
      String inputStr;
      while (true) {
        try {
          inputStr = input.readLine();
          step = step + 1;
          System.out.println("Step:" + step);
          System.out.println(inputStr);
          if (username == "bob" && step == 1)//we need to generate DES key now.
          {
            //generate 64 bit key
            String des_key = generate_64_DES_Key();

            BigInteger b = new BigInteger(des_key, 2);
            System.out.println("DES in HEXADECIMAL: " + b.toString(16));

            //save to a global variable
            key_hex_global = "0X" + b.toString(16);

            BigInteger decimal_value = new BigInteger(des_key, 2);
            String des_rsa_encrypt = RSAencrypt(decimal_value, aliceModulus, publicKeyAlice);
            PrintStream output = null;
            try {
              output = new PrintStream(s.getOutputStream());
            } catch (IOException e) {
              e.printStackTrace();
            }
            output.println("bob:" + des_rsa_encrypt);
            output.flush();
          }
          if (username == "alice" && step == 1)//we need to generate DES key now.
          {

            String[] parts = inputStr.split(":");
            String part2 = parts[1];
            String key = RSAdecrypt(part2, aliceModulus, privateKeyAlice);
            key = "0X" + key;
            key_hex_global = key;
            System.out.println("Alice gets key in hexadecimal:" + key);
            if (key != null) {
              PrintStream output = null;
              try {
                output = new PrintStream(s.getOutputStream());
              } catch (IOException e) {
                e.printStackTrace();
              }
              handshake_complete = "yes";
              BigInteger ok = new BigInteger("55555");
              String message = RSAencrypt(ok, bobModulus, publicKeyBob);
              output.println("alice:" + message);
              output.flush();
            }
          }

          if (step == 2 && username == "bob") {
            String[] parts = inputStr.split(":");
            String part2 = parts[1];
            String ok = RSAdecrypt(part2, bobModulus, privateKeyBob);
            BigInteger b = new BigInteger(ok, 16);
            if ((b.toString(10)).equals("55555")) {
              System.out.println("----HandShake Complete----");
              handshake_complete = "yes";
            }
          }

          if ((username == "bob" && step > 2) || (username == "alice" && step > 1)) {
            String[] parts = inputStr.split(":");
            String part2 = parts[1];
            System.out.println("Decrypted message:" + DESChat.decrypt(key_hex_global, part2));
          }

          if (inputStr == null) {
            System.err.println("The other user has disconnected, closing program...");
            System.exit(1);
          }
        } catch (IOException e) {
          e.printStackTrace();
          System.exit(1);
        }
      }
    }

    public void start() {
      if (t == null) {
        t = new Thread(this);
        t.start();
      }
    }
  }

  static String generate_64_DES_Key() {
    String key_binary;

    try {
      SecureRandom rnd;
      rnd = SecureRandom.getInstance("SHA1PRNG");
      rnd.setSeed(System.currentTimeMillis());
      String hex1 = new String();
      String hex2 = new String();
      String hex3 = new String();
      String hex = new String();
      hex1 = Integer.toHexString(rnd.nextInt());
      hex2 = Integer.toHexString(rnd.nextInt());
      hex = "0X" + hex1 + hex2;
      long key_long = Long.decode(hex);

      //check for weak keys
      if (hex.equalsIgnoreCase("0X0101010101010101") || hex.equalsIgnoreCase("0XFEFEFEFEFEFEFEFE")
          || hex.equalsIgnoreCase("0XE0E0E0E0F1F1F1F1") || hex
          .equalsIgnoreCase("0X1F1F1F1F0E0E0E0E") || hex.equalsIgnoreCase("0X0000000000000000")
          || hex.equalsIgnoreCase("0XFFFFFFFFFFFFFFFF") || hex
          .equalsIgnoreCase("0XE1E1E1E1F0F0F0F0") || hex.equalsIgnoreCase("0X1E1E1E1E0F0F0F0F")) {
        //generate key again
        generate_64_DES_Key();
      }

      key_binary = Long.toBinaryString(key_long);

      //check the length of the key. If it is lesser than 64 we pad it to make it 64 bits long
      if (key_binary.length() < 64) {
        int padding = 64 - key_binary.length();
        for (int p = 0; p < padding; p++) {
          key_binary = "0" + key_binary;
        }
      }
      return key_binary;
    } catch (NumberFormatException e) { // string generated in greater than 7fffffffffffffff
      key_binary = generate_64_DES_Key();
      return key_binary;
    } catch (Exception e) {
      return "ERROR";
    }
  }

  /*
   * encrypt RSA
   */
  private static String RSAencrypt(BigInteger m, String nStr, String eStr) {

    BigInteger n_b = new BigInteger(nStr);
    BigInteger e_b = new BigInteger(eStr);
    BigInteger answer = m.modPow(e_b, n_b);
    String encrypted_string = answer.toString();

    return encrypted_string;
  }

  private static String RSAdecrypt(String cStr, String nStr, String dStr) {

    BigInteger c_b = new BigInteger(cStr);
    BigInteger n_b = new BigInteger(nStr);
    BigInteger d_b = new BigInteger(dStr);
    BigInteger result = c_b.modPow(d_b, n_b);
    //convert to string
    String result2 = result.toString();
    //convert to hexadecimal
    BigInteger b = new BigInteger(result2, 10);
    return b.toString(16);
  }

  /*
   *This Function applies permutation to the bits using the permutation box
	*/
  static int[] permutation(int[] table, int[] key, int length) {
    int[] temp = new int[length];

    for (int j = 0; j < length; j++) {
      int val = table[j];
      temp[j] = key[val - 1];
    }
    return temp;
  }
}

