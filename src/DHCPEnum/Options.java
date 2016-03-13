package DHCPEnum;

import java.util.HashMap;

public enum Options {
	MESSAGE_TYPE(53),
	REQUESTED_IP(50),
	LEASE_TIME(51),
	SERVER_ID(54),
	END(255);
	
	// TODO geen idee of we dit gaan gebruiken. Alle options in deze enum zetten is nogal overkill
	
	int value;
	Options(int val) {
        this.value = val;
    }
	
	public int getValue() {
		return value;
	}
	
	private static HashMap<Integer, Options> map = new HashMap<Integer,Options>();
    static {
    	for(Options option : Options.values()){
            map.put(option.value, option);
        }
    }

    public static Options getByVal(int val) {
        return map.get(val);
    }	
}
