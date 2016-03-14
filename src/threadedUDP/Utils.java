package threadedUDP;

import java.util.Arrays;

import javax.xml.bind.DatatypeConverter;

public class Utils {
//	public static void printByteArray(byte[] ba) {
//		System.out.println("Start printing bytes");
//		for (byte b : ba) {
//			System.out.print(b);
//		}
//		System.out.println("");
//		System.out.println("");
//	}

	/** Deze method moet mss ergens anders komen **/
	public static byte[] getByteRange(byte[] bt, int start, int end) {
		return Arrays.copyOfRange(bt, start, end);
	}

	//CONVERSIONS: byte[] <-> int
	
	//for 4 byte array
	public static byte[] toBytes(int i)
	{
	  byte[] result = new byte[4];

	  result[0] = (byte) (i >> 24);
	  result[1] = (byte) (i >> 16);
	  result[2] = (byte) (i >> 8);
	  result[3] = (byte) (i /*>> 0*/);

	  return result;
	}
	
	//for variable byte length
	public static byte[] toBytes(int i, int length)
	{
	  byte[] result = new byte[length];
	  for(int k=0; k<length; k++){
		  result[k] = (byte) (i >> (length-1-k)*8);
	  }
	  return result;
	}
	
	public static int fromBytes(byte[] bytes) {
		int result = 0;
		for(int k=0; k<bytes.length; k++){
			result = result | ((bytes[k]  & 0xFF) << (bytes.length-1-k)*8);
		}
		return result;
	}
	
	public static void printHex(byte[] msg){
		System.out.println(toHexString(msg));
	}
	
	public static String toHexString(byte[] array) {
	    return DatatypeConverter.printHexBinary(array);
	}

	public static byte[] toByteArray(String s) {
	    return DatatypeConverter.parseHexBinary(s);
	}


}
