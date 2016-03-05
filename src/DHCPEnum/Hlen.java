package DHCPEnum;

public enum Hlen {
	INTERNET(6);
	
	int value;
	
	Hlen(int val) {
        this.value = val;
    }
	
	public int getValue() {
		return value;
	}
}
