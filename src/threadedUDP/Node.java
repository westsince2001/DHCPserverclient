package threadedUDP;

import java.io.IOException;
import java.net.UnknownHostException;

public abstract class Node {
	public static final byte[] BROADCAST_FLAG = new byte[]{(byte) 128,0};
	public static final byte[] UNICAST_FLAG = new byte[]{0,0};
	
	// Discover
	abstract DHCPMessage getDiscoverMsg() throws UnknownHostException;
	abstract DHCPMessage getDiscoverAnswer(DHCPMessage msg) throws UnknownHostException;
	
	// Offer
	abstract DHCPMessage getOfferMsg(DHCPMessage msg) throws UnknownHostException;
	abstract DHCPMessage getOfferAnswer(DHCPMessage msg) throws UnknownHostException;
	
	// Request
	abstract DHCPMessage getNewIPRequestMsg(DHCPMessage msg) throws UnknownHostException;
	abstract DHCPMessage extendLeaseRequestMessage() throws UnknownHostException;
	abstract DHCPMessage getRequestAnswer(DHCPMessage msg) throws UnknownHostException;
	
	//Acknowledge
	abstract DHCPMessage getAckMsg(DHCPMessage msg) throws UnknownHostException;
	abstract DHCPMessage getAckAnswer(DHCPMessage msg);
	
	// Not Acknowledge
	abstract DHCPMessage getNakMsg(DHCPMessage msg) throws UnknownHostException;
	abstract DHCPMessage getNakAnswer(DHCPMessage msg);
	
	// Release
	abstract DHCPMessage getReleaseMsg() throws UnknownHostException;
	abstract DHCPMessage getReleaseAnswer(DHCPMessage msg);
	
	//qabstract void sendMsg(DHCPMessage msg) throws IOException; Mag niet hier want anders sendMsg zit in handler en niet in ServerUDP! + 't is beter als sendMsg private is
}
