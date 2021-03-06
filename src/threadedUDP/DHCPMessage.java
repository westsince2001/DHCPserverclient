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
import DHCPEnum.Options;
import Exceptions.UnknownMessageTypeException;

public class DHCPMessage {
	
	/*********************************************************************** CONSTRUCTOR ***********************************************************************************************/
	
	/** Creates a new DHCPMesssage with given parameters **/
	public DHCPMessage(Opcode op, Htype htype, Hlen hlen, byte hops, int transactionID, short num_of_seconds, byte[] flags, InetAddress clientIP,
		InetAddress yourClientIP, InetAddress serverIP, InetAddress gatewayIP, MACaddress chaddr, byte[] sname, byte[] file, DHCPOptions options) {
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
	
	public DHCPMessage(Opcode op, int transactionID, byte[] flags, InetAddress clientIP, InetAddress yourClientIP, InetAddress serverIP, MACaddress chaddr, DHCPOptions options) throws UnknownHostException {
		this(op, Htype.ETHERNET, Hlen.INTERNET, (byte) 0, transactionID, (short) 0, flags, clientIP, yourClientIP, serverIP, InetAddress.getByName("0.0.0.0"), chaddr, new byte[0], new byte[0], options);
	}
	
	/**
	 * DECODE
	 * Creates a new DHCPMessage with as parameters the parameters of the
	 * decoded byte array.
	 * 
	 * @throws UnknownHostException
	 * @throws UnknownMessageTypeException 
	 **/
	public DHCPMessage(byte[] byteMsg) throws UnknownHostException, UnknownMessageTypeException {
		setOpcode(Opcode.getByVal((int) byteMsg[0]));
		setHtype(Htype.getByVal((int) byteMsg[1]));
		setHlen(Hlen.getByVal((int) byteMsg[2]));
		setHops(byteMsg[3]);
		setTransactionID(Utils.fromBytes(Utils.getByteRange(byteMsg, 4, 8)));
		setNum_of_seconds((short) Utils.fromBytes(Utils.getByteRange(byteMsg, 8, 10)));
		setFlags(Utils.getByteRange(byteMsg, 10, 12));
		setClientIP(InetAddress.getByAddress(Utils.getByteRange(byteMsg, 12, 16)));
		setYourClientIP(InetAddress.getByAddress(Utils.getByteRange(byteMsg, 16, 20)));
		setServerIP(InetAddress.getByAddress(Utils.getByteRange(byteMsg, 20, 24)));
		setGatewayIP(InetAddress.getByAddress(Utils.getByteRange(byteMsg, 24, 28)));
		setChaddr(new MACaddress(Utils.getByteRange(byteMsg, 28, 44)));
		setSname(Utils.getByteRange(byteMsg, 44, 108));
		setFile(Utils.getByteRange(byteMsg, 108, 236));
		DHCPOptions newOptions = new DHCPOptions(Utils.getByteRange(byteMsg, 236, byteMsg.length));
		setOptions(newOptions);
		if(getType() == null)
			throw new UnknownMessageTypeException();
	}
	
	/************************************************************ ENCODE ********************************************************************************/

	/**
	 * Returns a byte array in in DHCP format
	 * 
	 * @return
	 * @throws IOException
	 */
	public byte[] encode() throws IOException {
		byte[] byteMsg = new byte[236+getOptions().encode().length];
		
		ByteBuffer buf = ByteBuffer.wrap(byteMsg);

		buf.put((byte) getOpcode().getValue());
		buf.put((byte) getHtype().getValue());
		buf.put((byte) getHlen().getValue());
		buf.put((byte) getHops());
		buf.putInt(getTransactionID());
		buf.putShort(getNum_of_seconds());
		buf.put(Arrays.copyOfRange(getFlags(),0,2));
		buf.put(getClientIP().getAddress());
		buf.put(getYourClientIP().getAddress());
		buf.put(getServerIP().getAddress());
		buf.put(getGatewayIP().getAddress());
		buf.put(Arrays.copyOfRange(getChaddr().toBytes(), 0, 16));
		buf.put(Arrays.copyOfRange(getSname(),0, 64));
		buf.put(Arrays.copyOfRange(getFile(),0, 128));
		buf.put(getOptions().encode());
		
		return byteMsg;
	}
	
//	0                   1                   2                   3
//	   0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
//	   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
//	   |     op (1)    |   htype (1)   |   hlen (1)    |   hops (1)    |
//	   +---------------+---------------+---------------+---------------+
//	   |                            xid (4)                            |
//	   +-------------------------------+-------------------------------+
//	   |           secs (2)            |           flags (2)           |
//	   +-------------------------------+-------------------------------+
//	   |                          ciaddr  (4)                          |
//	   +---------------------------------------------------------------+
//	   |                          yiaddr  (4)                          |
//	   +---------------------------------------------------------------+
//	   |                          siaddr  (4)                          |
//	   +---------------------------------------------------------------+
//	   |                          giaddr  (4)                          |
//	   +---------------------------------------------------------------+
//	   |                                                               |
//	   |                          chaddr  (16)                         |
//	   |                                                               |
//	   |                                                               |
//	   +---------------------------------------------------------------+
//	   |                                                               |
//	   |                          sname   (64)                         |
//	   +---------------------------------------------------------------+
//	   |                                                               |
//	   |                          file    (128)                        |
//	   +---------------------------------------------------------------+
//	   |                                                               |
//	   |                          options (variable)                   |
//	   +---------------------------------------------------------------+


	
	/************************************************************************************************************************/

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
	MACaddress chaddr;
	byte[] sname;
	byte[] file;
	MessageType type;
	DHCPOptions options;
	
	/***************************************************** GETTERS AND SETTERS *******************************************************/

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

	public MACaddress getChaddr() {
		return chaddr;
	}

	public void setChaddr(MACaddress chaddr) {
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
		if(getOptions().getOption(53) == null)
			return null;
		return MessageType.getByVal(Utils.fromBytes(this.getOptions().getOption(53)));
	}

	public DHCPOptions getOptions() {
		return options;
	}
	
	public byte[] getOption(Options opt){
		return getOptions().getOption(opt);
	}

	public void setOptions(DHCPOptions options) {
		this.options = options;
	}

	// DEBUGGING
	public void print() {
		System.out.print(" MessageType: " + getType());
		System.out.print(" | Opcode: " + getOpcode().getValue());
		//System.out.print(" | Hlen: " + getHlen().getValue());
		//System.out.print(" | Hops: " + getHops());
		System.out.print(" | Transaction ID: " + getTransactionID());
		//System.out.print(" | Number of seconds: " + getNum_of_seconds());
		System.out.print(" | Flags: " + Utils.toHexString(getFlags()));
		System.out.print(" | clientIP: " + getClientIP().getHostAddress());
		System.out.print(" | your Client IP: " + getYourClientIP().getHostAddress());
		System.out.print(" | server IP: " + getServerIP().getHostAddress());
		//System.out.print(" | gateway IP: " + getGatewayIP().getHostAddress());
		System.out.print(" | Client Hardware Adresss: " + (getChaddr().toString()));
		//System.out.print(" | Server name: " + Utils.toHexString(getSname()));
		//System.out.print(" | File: " + Utils.toHexString(getFile()));
		System.out.print(" | Options: " + options.toString()); // TODO
		System.out.println();
	}
}