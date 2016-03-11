package threadedUDP;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class DHCPOptions {
	Map<Byte, byte[]> options = new LinkedHashMap<>();

	public DHCPOptions() {

	}

	public DHCPOptions(byte[] byteOptions) {
		decode(byteOptions);
	}
	
	public void addOption(OptionsEnum code, int value) {
		addOption(code.getValue(), value);
	}

	public void addOption(byte code, byte[] data ) {
		options.put(code, data);
	}

	public void addOption(byte code){
		options.put(code, null);
	}
	
	public void addOption(int code){
		addOption((byte) code);
	}

	public void addOption(int code, byte[] data) {
		options.put((byte) code, data);
	}
	
	public void addOption(int code, byte data ) {
		addOption(code, new byte[]{data});
	}
	
	public void addOption(int code, int data ) {
		addOption(code, Utils.toBytes(data));
	}
	
	public byte[] getOption(int code) {
		return getOption((byte) code);
	}
	
	public byte[] getOption(byte code) {
		return options.get(code);
	}
	
	public byte[] getOption(OptionsEnum option) {
		return getOption(option.getValue());
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
				addOption(code, data);
				index = index + 2 + length;
			}

		}
	}

	public byte[] encode() throws IOException {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		buf.write(getMagicCookie());
		for (Map.Entry<Byte, byte[]> entry : options.entrySet()) {
			int code = entry.getKey()  & 0xFF;
			byte[] data = entry.getValue();
			buf.write(code);
			if( code != 0 && code != 255){
				buf.write((byte) data.length);
				buf.write(data);
			}
		}
		return buf.toByteArray();

	}
	
	@Override
	public String toString(){
		String str = "";
		for (Map.Entry<Byte, byte[]> entry : options.entrySet()) {
			int code = entry.getKey() & 0xFF;
			OptionsEnum codeItem = OptionsEnum.getByVal(code);
			if(codeItem != null){
				
			}
			
			byte[] data = entry.getValue();
			if( code == 0 || code == 255){
				if(codeItem != null){
					str += " "+codeItem + "("+ code +"),";
				}else{
					str +=  " "+ code +",";
				}
				
			}else{
				if(codeItem != null){
					str += " "+codeItem+"("+code+"): "+Utils.fromBytes(data)+",";
				}else{
					str += " "+code+": "+Utils.fromBytes(data)+",";
				}
				
			}
		}
		return str;
	}
	
	public void print(){
		System.out.println("Printing options: " + toString());
	}

	
}
