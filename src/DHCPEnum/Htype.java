package DHCPEnum;

import java.util.HashMap;

public enum Htype {
	ETHERNET(1);
	
	int value;
	
	Htype(int val) {
        this.value = val;
    }
	
	public int getValue() {
		return value;
	}
	
	private static HashMap<Integer, Htype> map = new HashMap<Integer,Htype>();
    static {
    	for(Htype htype : Htype.values()){
            map.put(htype.value, htype);
        }
    }

    public static Htype getByVal(int val) {
        return map.get(val);
    }	
}
