package threadedUDP;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import DHCPEnum.Options;

public class DHCPOptions {
	Map<Byte, byte[]> options = new LinkedHashMap<>();

	/** 
	 * Constructor that will create a new empty options class
	 * */
	public DHCPOptions() {

	}
	
	/**
	 *  Constructor to decode byte array formatted as options 
	 *  
	 *  @param
	 *  	byteOptions
	 *  */
	public DHCPOptions(byte[] byteOptions) {
		decode(byteOptions);
	}
	
	/************************************************* ADD OPTION ***********************************************/
	
	/**
	 * Adds a new option to the class
	 * 
	 * @param code
	 * 			The Option to add to the class
	 * @param value
	 * 			int value that is added as data for this option
	 */
	public void addOption(Options code, int value) {
		addOption(code.getValue(), value, code.getDataLength());
	}
	
	/**
	 * Adds a new option to the class
	 * 
	 * @param code
	 * 			The Option to add to the class
	 * @param value
	 * 			byte array value that is added as data for this option
	 */
	public void addOption(Options code, byte[] value) {
		addOption(code.getValue(), value, code.getDataLength());
	}
	
	/**
	 * Adds a new option to the class
	 * 
	 * @param code
	 * 			The Option to add to the class
	 * @param value
	 * 			int value that is added as data for this option
	 * @param length
	 * 			the byte length of the data
	 */
	public void addOption(int code, int data , int length) {
		assert(code <= 255 && code >= 0);
		addOption((byte) code, Utils.getBytes(data, length));
	}
	
	/**
	 * 
	 * @param code
	 * @param data
	 * @param length
	 */
	public void addOption(int code, byte[] data , int length) {
		assert(code <= 255 && code >= 0);
		addOption((byte) code, data);
	}
	
	/**
	 * 
	 * @param code
	 */
	public void addOption(int code){
		assert(code <= 255 && code >= 0);
		addOption((byte) code, null);
	}
	
	/**
	 * 
	 * @param code
	 * @param data
	 */
	public void addOption(byte code, byte[] data ) {
		options.put(code, data);
	}
	
	/***********************************************  GET OPTION ***********************************************/
	
	public byte[] getOption(int code) {
		assert(code <= 255 && code >= 0);
		return options.get((byte) code);
	}
		
	public byte[] getOption(Options option) {
		return getOption(option.getValue());
	}
	
	/*********************************************** CHECKERS ***********************************************/
	
	public boolean isSet(Options option){
		return getOption(option) != null;
	}

	/************************************************ FORMATTING ********************************************/
	
	/**
	 * Returns an IPadress in 4 bytes
	 * 
	 * @return
	 * 		4 bytes with magic cookie IP adresss
	 */
	public byte[] getMagicCookie() throws UnknownHostException {
		return InetAddress.getByName("99.130.83.99").getAddress();
	}
	
	
	/**
	 * Will decode given byte array that are formatted as options. This makes it possible to read the options by using the getters of this class
	 * 
	 * @param options
	 * 			byte msg that is formatted as options and should be decoded
	 */
	public void decode(byte[] options) {		
		int index = 4; // Eerste 4 bytes zijn magicCookie
		while (index < options.length) {
			int code = options[index]  & 0xFF;
			if (code == 0){ // start code
				addOption(code);
				index+=1;
			}else if(code == 255) { // end code
				addOption(code);
				index += 1;
				break;
			} else {
				byte length = options[index + 1];
				byte[] data = Arrays.copyOfRange(options, index + 2, index + 2 + length);
				addOption((byte) code, data);
				index = index + 2 + length;
			}

		}
	}

	/**
	 * Encodes the options that are added to this class. Will return a string with a DHCP options byte array. 
	 * 
	 * @return
	 * 			returns 
	 * @throws IOException
	 */
	public byte[] encode() throws IOException {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		
		// Add magic cookie
		buf.write(getMagicCookie());
		
		// Add every registered option
		for (Map.Entry<Byte, byte[]> entry : options.entrySet()) {
			int code = entry.getKey()  & 0xFF;
			byte[] data = entry.getValue();
			
			buf.write(code);
			if( code != 0 && code != 255){ // if option is not 'start' or 'end' option, add length and data bytes
				buf.write((byte) data.length);
				buf.write(data);
			}
		}
		
		// Return byte array
		return buf.toByteArray();

	}
	
	/**
	 * Returns a string representation of the DHCP options
	 * 
	 * @returns
	 * 		String representation of the options
	 */
	@Override
	public String toString(){
		String str = "";
		
		// Loop for every registered option
		for (Map.Entry<Byte, byte[]> entry : options.entrySet()) {
			int code = entry.getKey() & 0xFF;
			Options codeItem = Options.getByVal(code);
			
			byte[] data = entry.getValue();
			if( code == 0 || code == 255){ // if option is start or end option, add no data string
				if(codeItem != null){
					str += " "+codeItem + "("+ code +"),";
				}else{
					str += " "+ code +",";
				}
				
			}else{ // add data string
				if(codeItem != null){
					str += " "+codeItem+"("+code+"): "+codeItem.toString(data)+",";
				}else{
					str += " "+code+": "+Utils.fromBytes(data)+",";
				}
				
			}
		}
		return str;
	}
	
	/**
	 * Prints the options
	 */
	public void print(){
		System.out.println("Printing options: " + toString());
	}

	
}