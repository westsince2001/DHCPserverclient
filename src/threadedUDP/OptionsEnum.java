package threadedUDP;

import java.util.HashMap;

import DHCPEnum.Htype;

public enum OptionsEnum {
	
	// TODO geen idee of we dit gaan gebruiken. Alle options in deze enum zetten is nogal overkill
	
	int value;
	OptionsEnum(int val) {
        this.value = val;
    }
	
	public int getValue() {
		return value;
	}
	
	private static HashMap<Integer, Htype> map = new HashMap<Integer,OptionsEnum>();
    static {
    	for(OptionsEnum optoin : OptionsEnum.values()){
            map.put(option.value, option);
        }
    }

    public static OptionsEnum getByVal(int val) {
        return map.get(val);
    }	
}
