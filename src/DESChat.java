package src;

import java.math.BigInteger;
import java.security.SecureRandom;

public class DESChat {

  static int[] key_64bit = new int[64];
  static int[] key_56bit = new int[56];
  static int[][] key_48bit = new int[16][48];
  static int sbox[][][] = new int[8][4][16];

  public static String encrypt(String key, String input) {
    String output = new String("");
    try {

      //generate key when the user provides the key
      genDESkey(key.toString());
      String encryptedText;

      for (int i = 0; i < input.length(); i = i + 7) {
        if (input.length() - i >= 7) {
          String line_part = input.substring(i, i + 7);
          encryptedText = DES_encrypt(line_part);
          int encryptedText_length = encryptedText.length();

          //to hexadecimal
          String hex = new BigInteger(encryptedText, 2).toString(16);

          //pad the hex string to be equal to length 16:
          int pad = 16 - hex.length();
          for (int p = 0; p < pad; p++) {
            hex = "0" + hex;
          }
          output = output + hex;
        } else {
          String line_part = input.substring(i, input.length());
          encryptedText = DES_encrypt(line_part);

          //to hexadecimal
          String hex = new BigInteger(encryptedText, 2).toString(16);
          int pad = 16 - hex.length();

          for (int p = 0; p < pad; p++) {
            hex = "0" + hex;
          }
          output = output + hex;
          break;
        }
      }

      return output;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return output;
  }

  /*
   *generate the 16 DES keys each of 48 bits.
   */
  static void genDESkey(String hex) {
    try {

      long key_long = Long.decode(hex);

      String key_binary = Long.toBinaryString(key_long);

      if (key_binary.length() < 64) {
        int padding = 64 - key_binary.length();
        for (int p = 0; p < padding; p++) {
          key_binary = "0" + key_binary;
        }
      }

      for (int i = 0; i < 64; i++) {
        key_64bit[i] = Character.getNumericValue(key_binary.charAt(i));
      }
    } catch (NumberFormatException e) {
      genDESkey();
      return;
    } catch (Exception e) {

    }

    //apply the permutation from permutation box to convert the key to a 56 bit key
    key_56bit = permutation(Sboxes.PC1, key_64bit, 56);

    // split into 28-bit left and right (c and d) pairs.
    int[] half_a = new int[28];
    int[] half_b = new int[28];

    for (int i = 0; i < 28; i++) {
      half_a[i] = key_56bit[i];
    }

    for (int j = 0; j < 28; j++) {
      half_b[j] = key_56bit[j + 28];
    }

    // for each of the 16 needed subkeys, perform a bit
    // rotation on each 28-bit keystuff half, then join
    // the halves together and permute to generate the subkey.

    for (int k = 0; k < 16; k++) {

      int[] temp_a = new int[28];
      int[] temp_b = new int[28];

      if (k == 0 || k == 1 || k == 8 || k == 15) {

        for (int i = 0; i < 28; i++) {
          if (i == 0) {
            temp_a[27] = half_a[0];
            temp_b[27] = half_b[0];
          } else {
            temp_a[i - 1] = half_a[i];
            temp_b[i - 1] = half_b[i];
          }
        }
      } else {

        for (int i = 0; i < 28; i++) {
          if (i == 0) {
            temp_a[26] = half_a[0];
            temp_b[26] = half_b[0];
          } else if (i == 1) {
            temp_a[27] = half_a[1];
            temp_b[27] = half_b[1];
          } else {
            temp_a[i - 2] = half_a[i];
            temp_b[i - 2] = half_b[i];
          }
        }
      }

      //update half_a and half_b using temp_a and temp_b
      for (int i = 0; i < 28; i++) {
        half_a[i] = temp_a[i];
        half_b[i] = temp_b[i];
      }

      //joining the halves together
      int[] temp_ab = new int[56];
      for (int i = 0; i < 28; i++) {
        temp_ab[i] = half_a[i];
      }
      for (int i = 28; i < 56; i++) {
        temp_ab[i] = half_b[i - 28];
      }

      //generating the 48 bit key by applying permutation
      int[] key_48_temp = new int[48];
      key_48_temp = permutation(Sboxes.PC2, temp_ab, 48);

      //copying the key into our static variable key_48bit
      for (int i = 0; i < 48; i++) {
        key_48bit[k][i] = key_48_temp[i];
      }
    }

    return;
  }

  /*
   *generate DES key when no 64 bit key is given.
   */
  static void genDESkey() {
    try {
      SecureRandom rnd = SecureRandom.getInstance("SHA1PRNG");
      rnd.setSeed(System.currentTimeMillis());
      String hex1 = new String();
      String hex2 = new String();
      String hex = new String();
      hex1 = Integer.toHexString(rnd.nextInt());
      hex2 = Integer.toHexString(rnd.nextInt());

      hex = "0X" + hex1 + hex2;

      long key_long = Long.decode(hex);
      String key_binary = Long.toBinaryString(key_long);

      //If length of key is lesser than 64, pad it to make it 64 bits long
      if (key_binary.length() < 64) {
        int padding = 64 - key_binary.length();
        for (int p = 0; p < padding; p++) {
          key_binary = "0" + key_binary;
        }
      }
      //copy the bits to int array
      for (int i = 0; i < 64; i++) {
        key_64bit[i] = Character.getNumericValue(key_binary.charAt(i));
      }
    } catch (NumberFormatException e) {
      //generated when the string generated is greater than 7fffffffffffffff
      genDESkey();
      return;
    } catch (Exception e) {

    }

    //Now applying the permutation from permutation box to convert the key to a 56 bit key
    key_56bit = permutation(Sboxes.PC1, key_64bit, 56);

    // split into 28-bit left and right (c and d) pairs.
    int[] half_a = new int[28];
    int[] half_b = new int[28];

    for (int i = 0; i < 28; i++) {
      half_a[i] = key_56bit[i];
    }

    for (int j = 0; j < 28; j++) {
      half_b[j] = key_56bit[j + 28];
    }

    // for each of the 16 needed subkeys, perform a bit
    // rotation on each 28-bit keystuff half, then join
    // the halves together and permute to generate the subkey.

    for (int k = 0; k < 16; k++) {

      int[] temp_a = new int[28];
      int[] temp_b = new int[28];

      if (k == 0 || k == 1 || k == 8 || k == 15) {

        for (int i = 0; i < 28; i++) {
          if (i == 0) {
            temp_a[27] = half_a[0];
            temp_b[27] = half_b[0];
          } else {
            temp_a[i - 1] = half_a[i];
            temp_b[i - 1] = half_b[i];
          }
        }
      } else {

        for (int i = 0; i < 28; i++) {
          if (i == 0) {
            temp_a[26] = half_a[0];
            temp_b[26] = half_b[0];
          } else if (i == 1) {
            temp_a[27] = half_a[1];
            temp_b[27] = half_b[1];
          } else {
            temp_a[i - 2] = half_a[i];
            temp_b[i - 2] = half_b[i];
          }
        }
      }

      //update half_a and half_b using temp_a and temp_b
      for (int i = 0; i < 28; i++) {
        half_a[i] = temp_a[i];
        half_b[i] = temp_b[i];
      }

      //join the halves together
      int[] temp_ab = new int[56];
      for (int i = 0; i < 28; i++) {
        temp_ab[i] = half_a[i];
      }
      for (int i = 28; i < 56; i++) {
        temp_ab[i] = half_b[i - 28];
      }

      //generating the 48 bit key by applying permutation
      int[] key_48_temp = new int[48];
      key_48_temp = permutation(Sboxes.PC2, temp_ab, 48);

      //copying the key into our static variable key_48bit
      for (int i = 0; i < 48; i++) {
        key_48bit[k][i] = key_48_temp[i];
      }
    }

    return;
  }

  /*
   * apply permutation to the bits using the permutation box
   */
  static int[] permutation(int[] table, int[] key, int length) {
    int[] temp = new int[length];

    for (int j = 0; j < length; j++) {
      int val = table[j];
      temp[j] = key[val - 1];
    }

    return temp;
  }

/*
*SBOX for easy access. Here we convert the given sbox to a three dimensional matrix
*/

  public static void make_sbox() {
    int num;
    for (num = 0; num < 8; num++) {
      int k = 0;
      int row = 0;
      for (int i = 0; i < 64; i++) {
        sbox[num][row][k] = Sboxes.S[num][i];
        if (k == 15) {
          k = 0;
          row++;
        } else {
          k++;
        }
      }
    }
  }

  /*
  *This function does the encryption part.
   */
  private static String DES_encrypt(
      String line) {// need to handle multiple lines and long data>64 or <64

    int input[] = new int[64];
    int cipher[] = new int[64];
    int left[] = new int[32];
    int right[] = new int[32];
    int ip_input[] = new int[64];

    String binary = new BigInteger(line.getBytes()).toString(2);
    int line_length = line.length();
    //number of zeros to be padded for original
    int num = line_length * 8;
    int diff = num - binary.length();
    for (int i = 0; i < diff; i++) {
      binary = "0" + binary;
    }

    //Padding to make sure it is 56 bits
    int binary_length = binary.length();
    int padding = 56 - binary_length;
    for (int l = 0; l < padding; l++) {
      input[l] = 0;
    }

    for (int i = 0; i < 56 - padding; i++) {
      input[i + padding] =
          Character.getNumericValue(binary.charAt(i));
    }

    //last 8 bits will have the padding value
    //convert padding to binary and add it to last 8 bits

    String padding_binary = Integer.toBinaryString(padding);
    String temp_padding = new String("");
    int length_pad = 8 - padding_binary.length();
    for (int yy = 0; yy < length_pad; yy++) {
      temp_padding = temp_padding + "0";
    }
    temp_padding = temp_padding + padding_binary;
    //System.out.println("value of padding binary" + temp_padding);
    int q = 56;
    for (int m = 0; m < 8; m++) {
      input[q] = Character.getNumericValue(temp_padding.charAt(m));
      q++;
    }

    //initial Permutation
    ip_input = permutation(Sboxes.IP, input, 64);

    //creating left and right initial array
    for (int i = 0; i < 32; i++) {
      left[i] = ip_input[i];
    }
    for (int i = 0; i < 32; i++) {
      right[i] = ip_input[i + 32];
    }

    cipher = applyFiestal(left, right);

    String builder = new String();
    for (int i : cipher) {
      builder = builder + i;
    }

    return builder;
  }

  public static int[] applyFiestal(int[] left, int[] right) {

    int temp_left[] = new int[32];
    int temp_right[] = new int[32];
    int xor_key_right[] = new int[48];
    int right_expand[] = new int[48];
    int right_8_6[][] = new int[8][6];
    int right_8_4[][] = new int[8][4];
    int cipher[] = new int[64];
    int final_swap[] = new int[64];
    int temp_left_old[] = new int[32];

    make_sbox();

    for (int j = 0; j < 32; j++) {
      temp_right[j] = right[j];
    }
    for (int j = 0; j < 32; j++) {
      temp_left[j] = left[j];
    }
    // 16 rounds
    for (int i = 0; i < 16; i++) {

      //implementing Ln = Rn-1. So we need to copy all bits from right part to the left part.
      for (int j = 0; j < 32; j++) {
        temp_left_old[j] = temp_left[j];
      }
      for (int j = 0; j < 32; j++) {
        temp_left[j] = temp_right[j];
      }

      //step1: Apply the expansion permutation on right half
      right_expand = permutation(Sboxes.E, temp_right, 48);

      //step2: XOR with the 48 bit key
      for (int k = 0; k < 48; k++) {
        xor_key_right[k] = right_expand[k] ^ key_48bit[i][k];
      }

      //step3a: divide the 48 bits into 8 groups of 6 bits each
      int c = 0;
      for (int a = 0; a < 8; a++) {
        for (int b = 0; b < 6; b++) {
          right_8_6[a][b] = xor_key_right[c];
          c++;
        }
      }

      //step3b: apply the substitution method as S1(B1)S2(B2)S3(B3)S4(B4)S5(B5)S6(B6)S7(B7)S8(B8)
      right_8_4 = applySubstitution(sbox, right_8_6);

      //Convert right_8_4 to a one dimensional array because our substitution function takes a 1d array as input
      int right_8_4_1d[] = new int[32];
      int mm = 0;
      for (int p = 0; p < 8; p++) {
        for (int q = 0; q < 4; q++) {
          right_8_4_1d[mm] = right_8_4[p][q];
          mm++;
        }
      }

      //Apply the permutation p and update the value to temp_right
      temp_right = permutation(Sboxes.P, right_8_4_1d, 32);

      for (int j = 0; j < 32; j++) {
        temp_right[j] = temp_right[j] ^ temp_left_old[j];
      }
    }

    //we need to swap temp_left and temp_right
    for (int p = 0; p < 32; p++) {
      final_swap[p] = temp_right[p];//right part getting copied to left
    }

    for (int p = 0; p < 32; p++) {
      final_swap[p + 32] = temp_left[p];//left part getting copied to right
    }

    //Apply the final permutation
    cipher = permutation(Sboxes.FP, final_swap, 64);
    return cipher;
  }

/*
  *applySubstitution box to and convert 48 bits to 32 bits
*/

  public static int[][] applySubstitution(int sbox[][][], int right_8_6[][]) {

    int right_8_4[][] = new int[8][4];
    int row, column;
    for (int i = 0; i < 8; i++) {
      String row_string = Integer.toString(right_8_6[i][0]) + Integer.toString(right_8_6[i][5]);
      row = Integer.parseInt(row_string, 2);
      String column_string =
          Integer.toString(right_8_6[i][1]) + Integer.toString(right_8_6[i][2]) + Integer
              .toString(right_8_6[i][3]) + Integer.toString(right_8_6[i][4]);
      column = Integer.parseInt(column_string, 2);
      int s_box_value = sbox[i][row][column];
      String s_box_value_binary = Integer.toBinaryString(s_box_value);

      //now we need to make sure that there are 4 bits
      if (s_box_value_binary.length() == 1) {
        s_box_value_binary = "000" + s_box_value_binary;
      } else if (s_box_value_binary.length() == 2) {
        s_box_value_binary = "00" + s_box_value_binary;
      } else if (s_box_value_binary.length() == 3) {
        s_box_value_binary = "0" + s_box_value_binary;
      }

      //fill in the values to the variable right_8_4
      right_8_4[i][0] = Character.getNumericValue(s_box_value_binary.charAt(0));
      right_8_4[i][1] = Character.getNumericValue(s_box_value_binary.charAt(1));
      right_8_4[i][2] = Character.getNumericValue(s_box_value_binary.charAt(2));
      right_8_4[i][3] = Character.getNumericValue(s_box_value_binary.charAt(3));
    }

    return right_8_4;
  }

  public static String decrypt(String key, String input) {
    String output = new String("");

    try {

      genDESkey(key.toString());
      String encryptedText;

      for (int k = 0; k < input.length(); k = k + 16) {
        String line_part = input.substring(k, k + 16);
        encryptedText = DES_decrypt(line_part);
        String padding = new String("");
        for (int i = 56; i < 64; i++) {
          padding = padding + encryptedText.charAt(i);
        }

        String real_encrypted = new String(
            "");//this variable will contain binary leaving the last 8 info bits and the padding bits.
        //remove padding
        int padding_int_value = Integer.parseInt(padding, 2);
        for (int i = padding_int_value; i < 56; i++) {
          real_encrypted = real_encrypted + encryptedText.charAt(i);
        }

        //convert binary to string
        StringBuilder sb = new StringBuilder();
        char nextChar;
        String s = real_encrypted;
        for (int i = 0; i < s.length();
            i += 8) {
          nextChar = (char) Integer.parseInt(s.substring(i, i + 8), 2);
          sb.append(nextChar);
        }

        output = output + sb.toString();
      }

      return output;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return output;
  }

  private static String DES_decrypt(String line) {

    int input[] = new int[64];
    int cipher[] = new int[64];
    int left[] = new int[32];
    int right[] = new int[32];
    int ip_input[] = new int[64];

    //convert to binary
    String binary = new BigInteger(line, 16).toString(2);

    int binary_length = binary.length();
    int padding = 64 - binary_length;
    for (int l = 0; l < padding; l++) {
      input[l] = 0;
    }

    for (int i = 0; i < 64 - padding; i++) {

      input[i + padding] = Character.getNumericValue(binary.charAt(i));
    }

    //initial Permutation
    ip_input = permutation(Sboxes.IP, input, 64);

    //creating left and right initial array
    for (int i = 0; i < 32; i++) {
      left[i] = ip_input[i];
    }
    for (int i = 0; i < 32; i++) {
      right[i] = ip_input[i + 32];
    }

    cipher = applyFiestal_decrypt(left, right);

    String builder = new String();
    for (int i : cipher) {
      builder = builder + i;
    }

    return builder;
  }

  public static int[] applyFiestal_decrypt(int[] left, int[] right) {

    int temp_left[] = new int[32];
    int temp_right[] = new int[32];
    int xor_key_right[] = new int[48];
    int right_expand[] = new int[48];
    int right_8_6[][] = new int[8][6];
    int right_8_4[][] = new int[8][4];
    int cipher[] = new int[64];
    int final_swap[] = new int[64];
    int temp_left_old[] = new int[32];

    make_sbox();

    //copy the values to temp_right so that it can be used for the first iteration
    for (int j = 0; j < 32; j++) {
      temp_right[j] = right[j];
    }
    for (int j = 0; j < 32; j++) {
      temp_left[j] = left[j];
    }

    //we need to do 16 rounds of transformation
    for (int i = 0; i < 16; i++) {

      //implementing Ln = Rn-1. So we need to copy all bits from right part to the left part.
      for (int j = 0; j < 32; j++) {
        temp_left_old[j] = temp_left[j];
      }
      for (int j = 0; j < 32; j++) {
        temp_left[j] = temp_right[j];
      }

      //now we need to work on the right part
      //step1: Apply the expansion permutation
      right_expand = permutation(Sboxes.E, temp_right, 48);

      //step2: XOR with the 48 bit key
      for (int k = 0; k < 48; k++) {
        xor_key_right[k] = right_expand[k] ^ key_48bit[15 - i][k];
      }

      //step3: using sbox to generate 32 bits

      //step3a: divide the 48 bits into 8 groups of 6 bits each
      int c = 0;
      for (int a = 0; a < 8; a++) {
        for (int b = 0; b < 6; b++) {
          right_8_6[a][b] = xor_key_right[c];
          c++;
        }
      }

      //step3b: apply the substitution method as S1(B1)S2(B2)S3(B3)S4(B4)S5(B5)S6(B6)S7(B7)S8(B8)
      right_8_4 = applySubstitution(sbox, right_8_6);

      //Convert right_8_4 to a one dimensional array because our substitution function takes a 1d array as input
      int right_8_4_1d[] = new int[32];
      int mm = 0;
      for (int p = 0; p < 8; p++) {
        for (int q = 0; q < 4; q++) {
          right_8_4_1d[mm] = right_8_4[p][q];
          mm++;
        }
      }

      //Apply the permutation p and update the value to temp_right
      temp_right = permutation(Sboxes.P, right_8_4_1d, 32);

      for (int j = 0; j < 32; j++) {
        temp_right[j] = temp_right[j] ^ temp_left_old[j];
      }
    }

    //we need to swap temp_left and temp_right
    for (int p = 0; p < 32; p++) {
      final_swap[p] = temp_right[p];//right part getting copied to left
    }

    for (int p = 0; p < 32; p++) {
      final_swap[p + 32] = temp_left[p];//left part getting copied to right
    }

    //Apply the final permutation
    cipher = permutation(Sboxes.FP, final_swap, 64);
    return cipher;
  }
}
