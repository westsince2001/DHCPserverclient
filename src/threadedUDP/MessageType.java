package threadedUDP;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Random;

public enum MessageType {
	DHCPDISCOVER(1){		
		@Override
		public DHCPMessage getAnswer(DHCPMessage msg, Node obj) throws UnknownHostException{
			return obj.getDiscoverAnswer(msg);
		}
	},
	DHCPOFFER(2){
		@Override
		public DHCPMessage getAnswer(DHCPMessage msg, Node node)
				throws UnknownHostException {
			// TODO Auto-generated method stub
			return null;
		}

	},
	DHCPREQUEST(3){
		@Override
		public DHCPMessage getAnswer(DHCPMessage msg, Node node)
				throws UnknownHostException {
			// TODO Auto-generated method stub
			return null;
		}

	
	},
	DHCPACK(5){
		@Override
		public DHCPMessage getAnswer(DHCPMessage msg, Node node)
				throws UnknownHostException {
			// TODO Auto-generated method stub
			return null;
		}

	},
	DHCPNAK(6){
		@Override
		public DHCPMessage getAnswer(DHCPMessage msg, Node node)
				throws UnknownHostException {
			// TODO Auto-generated method stub
			return null;
		}

		
	},
	DHCPRELEASE(7){
		@Override
		public DHCPMessage getAnswer(DHCPMessage msg, Node node)
				throws UnknownHostException {
			// TODO Auto-generated method stub
			return null;
		}

		
	};
	
	
	int value;
	
	MessageType(int val) {
        this.value = val;
    }
	
	public int getValue() {
		return value;
	}
	
	//public abstract DHCPMessage generateMsg(DHCPMessage msg) throws UnknownHostException;
	public abstract DHCPMessage getAnswer(DHCPMessage msg, Node node) throws UnknownHostException;
	
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
