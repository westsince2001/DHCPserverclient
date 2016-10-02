package DHCPEnum;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

import threadedUDP.Utils;

public enum Options {
	MESSAGE_TYPE(53, 1){
		@Override
		public String toString(byte[] val){
			return Integer.toString(Utils.fromBytes(val));
		}
	},
	REQUESTED_IP(50, 4){
		@Override
		public String toString(byte[] val){
			try{
				return InetAddress.getByAddress(val).getHostName();
			}catch(UnknownHostException e){
				return "Invalid IP";
			}	
		}
	},
	LEASE_TIME(51, 4){
		@Override
		public String toString(byte[] val){
			return Integer.toString(Utils.fromBytes(val));
		}
	},
	SERVER_ID(54, 4){
		@Override
		public String toString(byte[] val){
			return Integer.toString(Utils.fromBytes(val));
		}
	},
	END(255, 0){
		@Override
		public String toString(byte[] val){
			return "";
		}
	};	
	
	/************************************* HELPER FUNCTIONS *************************************/
	int value;
	int datalength;
	
	Options(int val, int datalength) {
        this.value = val;
        this.datalength = datalength;
    }
	
	public abstract String toString(byte[] val);

	public int getValue() {
		return value;
	}
	
	public int getDataLength() {
		return datalength;
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
