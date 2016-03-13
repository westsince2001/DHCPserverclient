package threadedUDP;

import java.net.InetAddress;

public class Leaser {
	Leaser( MACaddress chaddr, long startTime, long leaseTime){
		setChaddr(chaddr);
		setStartTime(startTime);
		setLeaseTime(leaseTime);
	}
	
	
	private long startTime;
	private long leaseTime;
	private MACaddress chaddr;
	//InetAddress ip;
//	
//	public InetAddress getIp() {
//		return ip;
//	}
//	public void setIp(InetAddress ip) {
//		this.ip = ip;
//	}
	
	
	public MACaddress getChaddr() {
		return chaddr;
	}
	public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	public long getLeaseTime() {
		return leaseTime;
	}
	public void setLeaseTime(long leaseTime) {
		this.leaseTime = leaseTime;
	}
	public void setChaddr(MACaddress chaddr) {
		this.chaddr = chaddr;
	}
	
	public boolean isConfirmed(){
		return getStartTime() != 0;
	}
	
	
}
