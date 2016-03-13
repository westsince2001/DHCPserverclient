package threadedUDP;

import java.net.InetAddress;

public class Leaser {
	Leaser( MACaddress chaddr, int startTime, int leaseTime){
		setChaddr(chaddr);
		setStartTime(startTime);
		setLeaseTime(leaseTime);
	}
	
	
	private int startTime;
	private int leaseTime;
	private MACaddress chaddr;
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
	public MACaddress getChaddr() {
		return chaddr;
	}
	public void setChaddr(MACaddress chaddr) {
		this.chaddr = chaddr;
	}
	
	public boolean isConfirmed(){
		return getStartTime() != 0;
	}
	
	
}
