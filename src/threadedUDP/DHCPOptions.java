package threadedUDP;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javafx.util.Pair; /* TODO: Pair zit wel standaard in java JDK7 ma geen idee of we dat mogen gebruiken!*/

public class DHCPOptions {
	Map<Byte, byte[]> options;
	
	public DHCPOptions() {
		
	}
	
	public DHCPOptions(byte[] byteOptions) {
		decode(byteOptions);
	}
	
	public void addOption(byte code, byte[] data /* byte[]? */){
		options.put(code,data);
	}
	
	public void addOption(int code, byte[] data /* byte[]? */){
		options.put((byte) code,data);
	}
	
	public byte[] getOption(byte code){
		return options.get(code);
	}
	
	/**
	 * Returns an IPadress in 4 bytes
	 * @return
	 * @throws UnknownHostException
	 */
	public byte[] getMagicCookie() throws UnknownHostException{
		return InetAddress.getByName("99.130.83.99").getAddress();
	}
	
	public void decode(byte[] options){
		int index = 0;
		while(index < options.length){
			byte code = options[index];
			byte length = options[index+1];
			byte[] data = Arrays.copyOfRange(options, index+2, index+2+length);
			addOption(code, data);
			index = index + 2 + length;
		}
	}
	
	public byte[] encode() throws UnknownHostException{
		byte[] byteOptions = new byte[512]; // TODO Deze lengte klopt niet, moet dynamisch (?) bepaald worden
		ByteBuffer buf = ByteBuffer.wrap(byteOptions);
		
		buf.put(getMagicCookie());
		
		for (Map.Entry<Byte,byte[]> entry : options.entrySet()) {
		    byte code = entry.getKey();
		    byte[] data = entry.getValue();
		    buf.put(code);
		    buf.put((byte) data.length);
		    buf.put(data);
		}
		return byteOptions;
	}
}

