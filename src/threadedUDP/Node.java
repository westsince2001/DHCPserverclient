package threadedUDP;

import java.io.IOException;
import java.net.UnknownHostException;

public abstract class Node {
	abstract DHCPMessage getDiscoverMsg() throws UnknownHostException;
	abstract DHCPMessage getDiscoverAnswer(DHCPMessage msg);
	abstract DHCPMessage getOfferMsg(DHCPMessage msg) throws UnknownHostException;
	abstract DHCPMessage getOfferAnswer(DHCPMessage msg) throws UnknownHostException;
	abstract DHCPMessage getRequestMsg(DHCPMessage msg) throws UnknownHostException;
	abstract DHCPMessage getRequestAnswer(DHCPMessage msg);
	abstract DHCPMessage getAckMsg(DHCPMessage msg) throws UnknownHostException;
	abstract DHCPMessage getAckAnswer(DHCPMessage msg);
	abstract DHCPMessage getNakMsg(DHCPMessage msg) throws UnknownHostException;
	abstract DHCPMessage getNakAnswer(DHCPMessage msg);
	abstract DHCPMessage getReleaseMsg(DHCPMessage msg) throws UnknownHostException;
	abstract DHCPMessage getReleaseAnswer(DHCPMessage msg);
	
	//qabstract void sendMsg(DHCPMessage msg) throws IOException; Mag niet hier want anders sendMsg zit in handler en niet in ServerUDP! + 't is beter als sendMsg private is
}
