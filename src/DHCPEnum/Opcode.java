package DHCPEnum;

import java.util.HashMap;

public enum Opcode {
	BOOTREQUEST(1),
	BOOTREPLY(2);
	
	int value;
	
	Opcode(int val) {
        this.value = val;
    }
	
	public int getValue() {
		return value;
	}
	
	private static HashMap<Integer, Opcode> map = new HashMap<Integer,Opcode>();
    static {
    	for(Opcode op : Opcode.values()){
            map.put(op.value, op);
        }
    }

    public static Opcode getByVal(int val) {
        return map.get(val);
    }	
	
//	andere manier: (waarschijnlijk trager):
//
//	public static DHCPOpcode getByVal(int val){
//		for(DHCPOpcode op : DHCPOpcode.values()){
//			if(op.getValue() == val)
//				return op;é
//		}
//		return null;
//	}
}
