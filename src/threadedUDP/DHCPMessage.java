/** Constructs a new DHCP Message, with the given type and parameters.
 * 
 *  Has a second constructor which translates a given data message to a DHCPmessage instance.
 *   
 * 
 * **/

package threadedUDP;

import java.net.InetAddress;
import java.util.Arrays;
import java.nio.ByteBuffer;

import DHCPEnum.Hlen;
import DHCPEnum.Hops;
import DHCPEnum.Htype;
import DHCPEnum.MessageType;
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

	/** Creates a new DHCPMesssage with given parameters **/
	public DHCPMessage(Opcode op, Htype htype, Hlen hlen, Hops hops,
			int transactionID, int num_of_seconds, byte[] flags /* byte? */,
			InetAddress clientIP, InetAddress serverIP, InetAddress gatewayIP,
			byte[] chaddr, byte[] sname, byte[] file, MessageType type
	/*
	 * byte[] options -- niet nodig, vervangen door Type veld? (of zijn er nog
	 * andere options?)
	 */

	) {
		setOpcode(op);
		setHtype(htype);
		setTransactionID(transactionID);
		setType(type);
		setChaddr(chaddr);
		/* TODO En nog andere velden... */

	}

	/**
	 * creates a new DHCPMessage with as parameters the parameters of the
	 * decoded byte array
	 **/

	public DHCPMessage(byte[] byteMsg) {
		// Dit is dus echt vreselijk lelijk
		setOpcode(Opcode.getByVal(DHCPMessage.byteToInt(DHCPMessage
				.getByteRange(byteMsg, 0, 1))));
		setHtype(Htype.getByVal(DHCPMessage.byteToInt(DHCPMessage.getByteRange(
				byteMsg, 1, 2))));
		setTransactionID(DHCPMessage.byteToInt(DHCPMessage.getByteRange(
				byteMsg, 4, 8)));
		/* TODO enzovoort */
	}

	/**
	 * Returns a message in DHCP format
	 * 
	 * @return
	 */
	public byte[] encode() {
		byte[] byteMsg = new byte[512]; /*
										 * TODO: lengte afhankelijk van lengte
										 * van options
										 */
		ByteBuffer buf = ByteBuffer.wrap(byteMsg);
		// opcode
		buf.putInt(getOpcode().getValue());
		// htype
		buf.putInt(getHtype().getValue());
		// hlen
		buf.putInt(6);
		// hops
		buf.putInt(0);
		// xid
		buf.put(ByteBuffer.allocate(4).putInt(getTransactionID()));
		/*
		 * TODO: hoe zeker weten dat dit altijd 4 bytes zijn?! dit is echt dirty
		 * nu
		 */
		
		// secondss
		buf.put((byte) 0);
		buf.put((byte) 0);
		// flags
		buf.put((byte) 0);
		buf.put((byte) 0);

		// TODO enzovoort

		return byteMsg;

	}

	Opcode opcode;
	Htype htype;
	MessageType type;
	byte[] chaddr;
	int transactionID;

	public byte[] getChaddr() {
		return chaddr;
	}

	public void setChaddr(byte[] chaddr) {
		this.chaddr = chaddr;
	}

	public MessageType getType() {
		return type;
	}

	public void setType(MessageType type) {
		this.type = type;
	}

	public int getTransactionID() {
		return transactionID;
	}

	public void setTransactionID(int transactionID) {
		this.transactionID = transactionID;
	}

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
}
