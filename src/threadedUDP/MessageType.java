package threadedUDP;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Random;

import Exceptions.UnknownMessageTypeException;

public enum MessageType {
	DHCPDISCOVER(1){		
		@Override
		public DHCPMessage getAnswer(DHCPMessage msg, Node obj) throws UnknownHostException{
			return obj.getDiscoverAnswer(msg);
		}

		@Override
		public void process(DHCPMessage msg, Node node) {
			node.processDiscover(msg);
		}
		
	},
	DHCPOFFER(2){
		@Override
		public DHCPMessage getAnswer(DHCPMessage msg, Node node)
				throws UnknownHostException {
			return node.getOfferAnswer(msg);
		}

		@Override
		public void process(DHCPMessage msg, Node node) {
			node.processOffer(msg);
		}

	},
	DHCPREQUEST(3){
		@Override
		public DHCPMessage getAnswer(DHCPMessage msg, Node node)
				throws UnknownHostException {
			return node.getRequestAnswer(msg);
		}

		@Override
		public void process(DHCPMessage msg, Node node) {
			node.processRequest(msg);
			
		}

	
	},
	DHCPACK(5){
		@Override
		public DHCPMessage getAnswer(DHCPMessage msg, Node node)
				throws UnknownHostException {
			return node.getAckAnswer(msg);
		}

		@Override
		public void process(DHCPMessage msg, Node node) {
			 node.processAck(msg);
		}

	},
	DHCPNAK(6){
		@Override
		public DHCPMessage getAnswer(DHCPMessage msg, Node node)
				throws UnknownHostException {
			return node.getNakAnswer(msg);
		}

		@Override
		public void process(DHCPMessage msg, Node node) throws IOException, UnknownMessageTypeException{
			node.processNak();
		}

		
	},
	DHCPRELEASE(7){
		@Override
		public DHCPMessage getAnswer(DHCPMessage msg, Node node)
				throws UnknownHostException {
			return node.getReleaseAnswer(msg);
		}

		@Override
		public void process(DHCPMessage msg, Node node) {
			node.processRelease(msg);
		}

		
	};
	
	public abstract DHCPMessage getAnswer(DHCPMessage msg, Node node) throws UnknownHostException;
	public abstract void process(DHCPMessage msg, Node node) throws IOException, UnknownMessageTypeException;
	
	
	int value;
	
	MessageType(int val) {
        this.value = val;
    }
	
	public int getValue() {
		return value;
	}	
	
	private static HashMap<Integer, MessageType> map = new HashMap<Integer,MessageType>();
    static {
    	for(MessageType msgType : MessageType.values()){
            map.put(msgType.value, msgType);
        }
    }

    public static MessageType getByVal(int val) {
        return map.get(val);
    }	
}
