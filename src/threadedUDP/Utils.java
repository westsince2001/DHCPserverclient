package threadedUDP;

import java.util.Arrays;

import javax.xml.bind.DatatypeConverter;

public class Utils {
	
	/**
	 * Returns a selection of a byte array
	 * @param bt
	 * @param start
	 * @param end
	 * @return
	 */
	public static byte[] getByteRange(byte[] bt, int start, int end) {
		return Arrays.copyOfRange(bt, start, end);
	}

	// CONVERSIONS: int <-> byte[]
	
	/**
	 * Returns a byte array of length 4 with the given int
	 * 
	 * @param i
	 * @return
	 */
	public static byte[] getBytes(int i)
	{
	  byte[] bt = new byte[4];

	  bt[0] = (byte) (i >> 24);
	  bt[1] = (byte) (i >> 16);
	  bt[2] = (byte) (i >> 8);
	  bt[3] = (byte) (i); /*>> 0*/
	  return bt;
	}
	
	/**
	 * Returns a byte array of the given length with the given int
	 * @param i
	 * @param length
	 * @return
	 */
	public static byte[] getBytes(int i, int length)
	{
	  byte[] bt = new byte[length];
	  for(int k=0; k<length; k++){
		  bt[k] = (byte) (i >> (length - 1 - k)*8);
	  }
	  return bt;
	}
	
	/**
	 * Returns an int derived from the given byte[]
	 * @param bt
	 * @return
	 */
	public static int fromBytes(byte[] bt) {
		int result = 0;
		for(int k=0; k<bt.length; k++){
			result = result | ((bt[k]  & 0xFF) << (bt.length-1-k)*8);
		}
		return result;
	}
	
	/**
	 * Prints the given byte in hex characters
	 * @param bt
	 */
	public static void printHex(byte[] bt){
		System.out.println(toHexString(bt));
	}
	
	/**
	 * Returns a string of the hex representation of the given byte
	 * @param bt
	 * @return
	 */
	public static String toHexString(byte[] bt) {
	    return DatatypeConverter.printHexBinary(bt);
	}
}
