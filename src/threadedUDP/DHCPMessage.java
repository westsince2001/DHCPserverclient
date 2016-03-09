/** Constructs a new DHCP Message, with the given type and parameters.
 * 
 *  Has a second constructor which translates a given data message to a DHCPmessage instance.
 *   
 * 
 * **/

package threadedUDP;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.nio.ByteBuffer;

import DHCPEnum.Hlen;
import DHCPEnum.Htype;
import DHCPEnum.Opcode;

public class DHCPMessage {
	public static void printByteArray(byte[] ba) {
		System.out.println("Start printing bytes");
		for (byte b : ba) {
			System.out.print(b);
		}
		System.out.println("");
		System.out.println("");
	}

	/** Deze method moet mss ergens anders komen **/
	public static byte[] getByteRange(byte[] bt, int start, int end) {
		return Arrays.copyOfRange(bt, start, end);
	}

	/** Zet byte[] om naar int 
	 *  TODO dit moet beter
	 * **/
	public static int byteToInt(byte[] bt) {
		// DHCPMessage.printByteArray(bt);
		switch (bt.length) {
		case 0:
			return 0;
		case 1:
			return bt[0];
		case 2:
			return java.nio.ByteBuffer.wrap(bt).getShort();
		case 4:
		default:
			return java.nio.ByteBuffer.wrap(bt).getInt();
		}

	}

	/** Zet byte[] om naar short **/
	public static short byteToShort(byte[] bt) {
		return java.nio.ByteBuffer.wrap(bt).getShort();
	}

	/** Creates a new DHCPMesssage with given parameters **/
	public DHCPMessage(Opcode op, Htype htype, Hlen hlen, byte hops, int transactionID, short num_of_seconds, byte[] flags /*
																													 */, InetAddress clientIP,
		InetAddress yourClientIP, InetAddress serverIP, InetAddress gatewayIP, byte[] chaddr, byte[] sname, byte[] file, DHCPOptions options) {
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
		setOptions(options);
	}

	/** Creates empty DHCPMessage **/
	public DHCPMessage() {
		// TODO dit klopt niet aangezien sommige objects null zijn, en da's nie
		// fijn. Sowieso mag deze methode nergens gebruikt worden om een echte
		// "valid" msg te maken
	}

	/**
	 * creates a new DHCPMessage with as parameters the parameters of the
	 * decoded byte array
	 * 
	 * @throws UnknownHostException
	 **/
	public DHCPMessage(byte[] byteMsg) throws UnknownHostException {
		setOpcode(Opcode.getByVal((int) byteMsg[0]));
		setHtype(Htype.getByVal((int) byteMsg[1]));
		setHlen(Hlen.getByVal((int) byteMsg[2]));
		setHops(byteMsg[3]);
		setTransactionID(DHCPMessage.byteToInt(DHCPMessage.getByteRange(byteMsg, 4, 8)));
		setNum_of_seconds(DHCPMessage.byteToShort(DHCPMessage.getByteRange(byteMsg, 8, 10)));
		setFlags(DHCPMessage.getByteRange(byteMsg, 10, 12));
		setClientIP(InetAddress.getByAddress(DHCPMessage.getByteRange(byteMsg, 12, 16)));
		setYourClientIP(InetAddress.getByAddress(DHCPMessage.getByteRange(byteMsg, 16, 20)));
		setServerIP(InetAddress.getByAddress(DHCPMessage.getByteRange(byteMsg, 20, 24)));
		setGatewayIP(InetAddress.getByAddress(DHCPMessage.getByteRange(byteMsg, 24, 28)));
		setChaddr(DHCPMessage.getByteRange(byteMsg, 28, 44));
		setSname(DHCPMessage.getByteRange(byteMsg, 44, 108));
		setFile(DHCPMessage.getByteRange(byteMsg, 108, 236));

		DHCPOptions newOptions = new DHCPOptions(DHCPMessage.getByteRange(byteMsg, 236, byteMsg.length));
		setOptions(newOptions);

		// Store typeé
//		MessageType type = MessageType.getByVal(newOptions.getOption((byte) 53)[0]);
//		if (type != null) {
//			setType(type);
//		} else {
//			System.out.println("No DHCP Message type in given message!");
//		}

	}

	/**
	 * Returns a message in DHCP format
	 * 
	 * @return
	 * @throws IOException
	 */
	public byte[] encode() throws IOException {
		boolean debugEncodingLength = false;

		byte[] byteMsg = new byte[236+getOptions().encode().length]; /*
										 * TODO: lengte afhankelijk van lengte
										 * van options
										 * 
										 * Voorlopig lengte options = 8 bytes
										 * (magic cookie (4) + message type (3)
										 * + end option (2))
										 */
		ByteBuffer buf = ByteBuffer.wrap(byteMsg);

		buf.put((byte) getOpcode().getValue());
		if (debugEncodingLength)
			System.out.println("na opcode " + buf.position());
		buf.put((byte) getHtype().getValue());
		if (debugEncodingLength)
			System.out.println("na Htype " + buf.position());
		buf.put((byte) getHlen().getValue());
		if (debugEncodingLength)
			System.out.println("Na Hlen " + buf.position());
		buf.put((byte) getHops());
		if (debugEncodingLength)
			System.out.println("getHops() " + buf.position());
		buf.putInt(getTransactionID());
		if (debugEncodingLength)
			System.out.println("getTransactionId() " + buf.position());
		buf.putShort(getNum_of_seconds());
		if (debugEncodingLength)
			System.out.println("nb of seconds " + buf.position());
		buf.put(getFlags());
		if (debugEncodingLength)
			System.out.println("flags " + buf.position());
		buf.put(getClientIP().getAddress());
		if (debugEncodingLength)
			System.out.println("clientIp " + buf.position());
		buf.put(getYourClientIP().getAddress());
		if (debugEncodingLength)
			System.out.println("YourClientIp " + buf.position());
		buf.put(getServerIP().getAddress());
		if (debugEncodingLength)
			System.out.println("serverIP " + buf.position());
		buf.put(getGatewayIP().getAddress());
		if (debugEncodingLength)
			System.out.println("gatewayIP " + buf.position());
		buf.put(getChaddr());
		if (debugEncodingLength)
			System.out.println("MAC " + buf.position());
		buf.put(getSname());
		if (debugEncodingLength)
			System.out.println("sname " + buf.position());
		buf.put(getFile());
		if (debugEncodingLength)
			System.out.println("file " + buf.position());
		// System.out.println("options length "+getOptions().encode().length);
		buf.put(getOptions().encode());
		if (debugEncodingLength)
			System.out.println("options " + buf.position());

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
	byte[] sname;
	byte[] file;
	MessageType type;
	DHCPOptions options;

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
		return MessageType.getByVal(DHCPMessage.byteToInt(this.getOptions().getOption(53)));
	}

//	public void setType(MessageType type) {
//		this.type = type;
//	}

	public DHCPOptions getOptions() {
		return options;
	}

	public void setOptions(DHCPOptions options) {
		this.options = options;
	}

	// DEBUGGING
	public void print() {
		System.out.println("Printing message: ");
		System.out.print(" MessageType: " + getType());
		System.out.print("| Opcode: " + getOpcode().getValue());
		System.out.print("| Hlen: " + getHlen().getValue());
		System.out.print("| Hops: " + getHops());
		System.out.print("| Transaction ID: " + getTransactionID());
		System.out.print("| Number of seconds: " + getNum_of_seconds());
		System.out.print("| Flags: " + getFlags());
		System.out.print("| clientIP: " + getClientIP());
		System.out.print("| your Client IP: " + getYourClientIP());
		System.out.print("| server IP: " + getServerIP());
		System.out.print("| gateway IP: " + getGatewayIP());
		System.out.print("| Client Hardware Adresss: " + getChaddr());
		System.out.print("| Server name: " + getSname());
		System.out.print("| File: " + getFile());
		System.out.print("| Options: " + "not implemented yet..."); // TODO
		System.out.println("End printing message");
		getOptions().print();
	}
}
