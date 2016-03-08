/** Constructs a new DHCP Message, with the given type and parameters.
 * 
 *  Has a second constructor which translates a given data message to a DHCPmessage instance.
 *   
 * 
 * **/

package threadedUDP;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.nio.ByteBuffer;

import DHCPEnum.Hlen;
import DHCPEnum.Htype;
import DHCPEnum.Opcode;

public class DHCPMessage {

	/** Deze method moet mss ergens anders komen **/
	public static byte[] getByteRange(byte[] bt, int start, int end) {
		return Arrays.copyOfRange(bt, start, end);
	}

	/** Zet byte[] om naar int **/
	public static int byteToInt(byte[] bt) {
		return java.nio.ByteBuffer.wrap(bt).getInt();
	}
	
	/** Zet byte[] om naar short **/
	public static short byteToShort(byte[] bt) {
		return java.nio.ByteBuffer.wrap(bt).getShort();
	}

	/** Creates a new DHCPMesssage with given parameters **/
	public DHCPMessage(Opcode op, Htype htype, Hlen hlen, byte hops,
			int transactionID, short num_of_seconds, byte[] flags /* byte? */,
			InetAddress clientIP, InetAddress yourClientIP,
			InetAddress serverIP, InetAddress gatewayIP, byte[] chaddr,
			byte[] sname, byte[] file, MessageType type) {
		setOpcode(op);
		setHtype(htype);
		setHlen(hlen);
		setHops(hops);
		setTransactionID(transactionID);
		setNum_of_seconds(num_of_seconds);
		setFlags(flags);
		setClientIP(clientIP);
		setYourClientIP(yourClientIP);
		setServerIP(serverIP);
		setGatewayIP(gatewayIP);
		setChaddr(chaddr);
		setSname(sname);
		setFile(file);
		
		/*DHCPOptions newOptions = new DHCPOptions();
		newOptions.addOption(53, new byte[]{(byte) type.getValue()}); // Ik *hoop* dat 53 de juiste code voor het message type is
		setOptions(newOptions);*/
		
		// dummy
		this.options = new byte[]{99,(byte)130,83,99, // magic cookie
				53,1,1, // DHPC discover value
				(byte) 255}; // end option
		
		setType(type);

	}

	/** Creates empty DHCPMessage **/
	public DHCPMessage() {
		// TODO dit klopt niet aangezien sommige objects null zijn, en da's nie fijn. Sowieso mag deze methode nergens gebruikt worden om een echte "valid" msg te maken
	}

	/**
	 * creates a new DHCPMessage with as parameters the parameters of the
	 * decoded byte array
	 * @throws UnknownHostException 
	 **/
	public DHCPMessage(byte[] byteMsg) throws UnknownHostException {
		setOpcode(Opcode.getByVal((int) byteMsg[0]));
		setHtype(Htype.getByVal((int) byteMsg[1]));
		setHlen(Hlen.getByVal((int) byteMsg[2]));
		setHops(byteMsg[3]);		
		setTransactionID(DHCPMessage.byteToInt(DHCPMessage.getByteRange(
				byteMsg, 4, 8)));
		setNum_of_seconds(DHCPMessage.byteToShort(DHCPMessage.getByteRange(byteMsg, 8, 10)));
		setFlags(DHCPMessage.getByteRange(byteMsg, 10, 12));
		setClientIP(InetAddress.getByAddress(DHCPMessage.getByteRange(byteMsg, 12, 16)));
		setYourClientIP(InetAddress.getByAddress(DHCPMessage.getByteRange(byteMsg, 16, 20)));
		setServerIP(InetAddress.getByAddress(DHCPMessage.getByteRange(byteMsg, 20, 24)));
		setGatewayIP(InetAddress.getByAddress(DHCPMessage.getByteRange(byteMsg, 24, 28)));
		setChaddr(DHCPMessage.getByteRange(byteMsg, 28, 44));
		setSname(DHCPMessage.getByteRange(byteMsg, 44, 108));
		setFile(DHCPMessage.getByteRange(byteMsg, 108, 236));
		
		/*DHCPOptions newOptions = new DHCPOptions(DHCPMessage.getByteRange(byteMsg, 236, byteMsg.length));
		setOptions(newOptions);
		
		// Store type
		MessageType type = MessageType.getByVal(DHCPMessage.byteToInt(newOptions.getOption((byte) 53)));
		if(type != null){
			setType(type);
		}else{
			System.out.println("No DHCP Message type in given message!");
		}*/
		
		
	}

	/**
	 * Returns a message in DHCP format
	 * 
	 * @return
	 * @throws UnknownHostException 
	 */
	public byte[] encode() throws UnknownHostException {
		byte[] byteMsg = new byte[512]; /*
										 * TODO: lengte afhankelijk van lengte
										 * van options
										 */
		ByteBuffer buf = ByteBuffer.wrap(byteMsg);

		buf.put((byte) getOpcode().getValue());
		buf.put((byte) getHtype().getValue());
		buf.put((byte) getHlen().getValue());
		buf.put((byte) getHops());
		buf.putInt(getTransactionID());
		buf.putShort(getNum_of_seconds());
		buf.put(getFlags());
		buf.put(getClientIP().getAddress());
		buf.put(getYourClientIP().getAddress());
		buf.put(getServerIP().getAddress());
		buf.put(getGatewayIP().getAddress());
		buf.put(getChaddr());
		buf.put(getSname());
		buf.put(getFile());
		//buf.put(getOptions().encode());
		buf.put(options);
		
		return byteMsg;

	}

	Opcode opcode;
	Htype htype;
	Hlen hlen;
	byte hops;
	int transactionID;
	short num_of_seconds;
	byte[] flags;
	InetAddress clientIP;
	InetAddress yourClientIP;
	InetAddress serverIP;
	InetAddress gatewayIP;
	byte[] chaddr;
	byte[] sname; // TODO byte of string?
	byte[] file; // TODO byte of string?
	MessageType type;
	//DHCPOptions options;
	byte[] options; // dummy

	public Opcode getOpcode() {
		return opcode;
	}

	public void setOpcode(Opcode opcode) {
		this.opcode = opcode;
	}

	public Htype getHtype() {
		return htype;
	}

	public void setHtype(Htype htype) {
		this.htype = htype;
	}

	public Hlen getHlen() {
		return hlen;
	}

	public void setHlen(Hlen hlen) {
		this.hlen = hlen;
	}

	public byte getHops() {
		return hops;
	}

	public void setHops(byte hops) {
		this.hops = hops;
	}

	public int getTransactionID() {
		return transactionID;
	}

	public void setTransactionID(int transactionID) {
		this.transactionID = transactionID;
	}

	public short getNum_of_seconds() {
		return num_of_seconds;
	}

	public void setNum_of_seconds(short num_of_seconds) {
		this.num_of_seconds = num_of_seconds;
	}

	public byte[] getFlags() {
		return flags;
	}

	public void setFlags(byte[] flags) {
		this.flags = flags;
	}

	public InetAddress getClientIP() {
		return clientIP;
	}

	public void setClientIP(InetAddress clientIP) {
		this.clientIP = clientIP;
	}

	public InetAddress getYourClientIP() {
		return yourClientIP;
	}

	public void setYourClientIP(InetAddress yourClientIP) {
		this.yourClientIP = yourClientIP;
	}

	public InetAddress getServerIP() {
		return serverIP;
	}

	public void setServerIP(InetAddress serverIP) {
		this.serverIP = serverIP;
	}

	public InetAddress getGatewayIP() {
		return gatewayIP;
	}

	public void setGatewayIP(InetAddress gatewayIP) {
		this.gatewayIP = gatewayIP;
	}

	public byte[] getChaddr() {
		return chaddr;
	}

	public void setChaddr(byte[] chaddr) {
		this.chaddr = chaddr;
	}

	public byte[] getSname() {
		return sname;
	}

	public void setSname(byte[] sname) {
		this.sname = sname;
	}

	public byte[] getFile() {
		return file;
	}

	public void setFile(byte[] file) {
		this.file = file;
	}

	public MessageType getType() {
		return type;
	}

	public void setType(MessageType type) {
		this.type = type;
	}

	/*public DHCPOptions getOptions() {
		return options;
	}

	public void setOptions(DHCPOptions options) {
		this.options = options;
	}*/

}
