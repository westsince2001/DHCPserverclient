package threadedUDP;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

//import javafx.util.Pair; /* TODO: Pair zit wel standaard in java JDK7 ma geen idee of we dat mogen gebruiken!*/

public class DHCPOptions {
	Map<Byte, byte[]> options = new HashMap<>();

	public DHCPOptions() {

	}

	public DHCPOptions(byte[] byteOptions) {
		decode(byteOptions);
	}

	public void addOption(byte code, byte[] data /* byte[]? */) {
		options.put(code, data);
	}

	public void addOption(int code, byte[] data /* byte[]? */) {
		options.put((byte) code, data);
	}
	
	public byte[] getOption(int code) {
		return getOption((byte) code);
	}
	
	public byte[] getOption(byte code) {
		return options.get(code);
	}

	/**
	 * Returns an IPadress in 4 bytes
	 * 
	 * @return
	 * @throws UnknownHostException
	 */
	public byte[] getMagicCookie() throws UnknownHostException {
		return InetAddress.getByName("99.130.83.99").getAddress();
	}

	public void decode(byte[] options) {		
		int index = 4; // Eerste 4 bytes zijn magicCookie
		while (index < options.length) {
			byte code = options[index];
			if (code == 0 || code == 255) { // start of end
				index += 1;
			} else {
				byte length = options[index + 1];
				byte[] data = Arrays.copyOfRange(options, index + 2, index + 2 + length);
				addOption(code, data);
				index = index + 2 + length;
			}

		}
	}

	public byte[] encode() throws IOException {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		buf.write(getMagicCookie());
		for (Map.Entry<Byte, byte[]> entry : options.entrySet()) {
			byte code = entry.getKey();
			byte[] data = entry.getValue();
			buf.write(code);
			buf.write((byte) data.length);
			buf.write(data);
		}
		return buf.toByteArray();

	}
	
	public void print(){
		System.out.println("Print options");
		for (Map.Entry<Byte, byte[]> entry : options.entrySet()) {
			byte code = entry.getKey();
			byte[] data = entry.getValue();
			System.out.print(" | "+code+" "+DHCPMessage.byteToInt(data));
		}
		System.out.println("");
		System.out.println("End print otions");
	}
}
