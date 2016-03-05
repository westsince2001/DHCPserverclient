package DHCPEnum;

import java.util.HashMap;

public enum MessageType {
	DHCPDISCOVER(1),
	DHCPOFFER(2),
	DHCPREQUEST(3),
	DHCPDECLINE(4),
	DHCPACK(5),
	DHCPNAK(6),
	DHCPRELEASE(7),
	DHCPINFORM(8);
	
	
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
