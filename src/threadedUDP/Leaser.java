package threadedUDP;

import java.net.InetAddress;

public class Leaser {
	Leaser( MACadress chaddr, int startTime, int leaseTime){
		
	}
	
	
	int startTime;
	int leaseTime;
	MACadress chaddr;
	//InetAddress ip;
//	
//	public InetAddress getIp() {
//		return ip;
//	}
//	public void setIp(InetAddress ip) {
//		this.ip = ip;
//	}
	public int getStartTime() {
		return startTime;
	}
	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}
	public int getLeaseTime() {
		return leaseTime;
	}
	public void setLeaseTime(int leaseTime) {
		this.leaseTime = leaseTime;
	}
	public MACadress getChaddr() {
		return chaddr;
	}
	public void setChaddr(MACadress chaddr) {
		this.chaddr = chaddr;
	}
	
	public boolean isConfirmed(){
		return getStartTime() != 0;
	}
	
	
}
