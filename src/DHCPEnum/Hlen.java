package DHCPEnum;

import java.util.HashMap;

public enum Hlen {
	INTERNET(6);
	
	int value;
	
	Hlen(int val) {
        this.value = val;
    }
	
	public int getValue() {
		return value;
	}
	
	private static HashMap<Integer, Hlen> map = new HashMap<Integer,Hlen>();
    static {
    	for(Hlen hlen : Hlen.values()){
            map.put(hlen.value, hlen);
        }
    }

    public static Hlen getByVal(int val) {
        return map.get(val);
    }	
}
