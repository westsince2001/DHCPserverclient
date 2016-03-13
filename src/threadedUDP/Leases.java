package threadedUDP;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;


public class Leases {
	final static int LEASE_TIME = 10; // Lease time in seconds
	
	
	
	void addNewIP(InetAddress ip){
		assert(leases.get(ip) == null);
		leases.put(ip, null);

	}
	
	private long getCurrentTimeSeconds(){
		return System.currentTimeMillis() / 1000L;
	}
	
	void leaseIP(InetAddress ip, MACaddress chaddr){
		System.out.println("----- NEW LEASE: "+ip.getHostAddress()+" -----");
		assert(!isLeased(ip));
		
		long startTime = getCurrentTimeSeconds(); // start time in seconds
		leases.put(ip, new Leaser(chaddr, startTime, LEASE_TIME));
		
		Timer timer = new Timer();
		timer.schedule(new checkLeaseExpiration(this, ip, chaddr), LEASE_TIME*1000);
	}
	
	void extendLease(InetAddress ip, MACaddress chaddr){ 
		System.out.println("----- EXTENDING LEASE: "+ip.getHostAddress()+" -----");
		assert(isLeasedBy(ip, chaddr));
		
		Leaser leaser = getLeaser(ip);
		leaser.setStartTime(getCurrentTimeSeconds());
		
		Timer timer = new Timer();
		timer.schedule(new checkLeaseExpiration(this, ip, chaddr), LEASE_TIME*1000);
	}
	
	void release(InetAddress ip, MACaddress chaddr){
		assert(isLeasedBy(ip, chaddr));
		removeLease(ip);
	}
	
	private void removeLease(InetAddress ip){
		System.out.println("----- LEASE RELEASED: " + ip.getHostAddress() + " -----");
		
		assert(isLeased(ip));
		leases.put(ip, null); 
	}	
	
	private Leaser getLeaser(InetAddress ip){
		return leases.get(ip);
	}
	
	public boolean isLeased(InetAddress ip){ // TODO checken of dit ook werkt als ip niet in leases voorkomt!
		return getLeaser(ip) != null;
	}
	
	boolean isLeaseExpired(InetAddress ip){
		if(!isLeased(ip))
			return false;
		Leaser leaser = getLeaser(ip);
		if(leaser.getStartTime() + leaser.getLeaseTime() > getCurrentTimeSeconds())
			return false;
		return true;
	}
	
	public boolean isLeasedBy(InetAddress ip, MACaddress chaddr){
		if(!isLeased(ip))
			return false;
		Leaser leaser = getLeaser(ip);
		if(!leaser.getChaddr().equals(chaddr))
			return false;
		return true;
	}
	
	HashMap<InetAddress, Leaser> leases = new HashMap<>();
	
	public HashMap<InetAddress, Leaser> getLeases() {
		return leases;
	}

	public void setLeases(HashMap<InetAddress, Leaser> leases) {
		this.leases = leases;
	}
	
	InetAddress getNextAvailableIP(){
		for(InetAddress ip : getLeases().keySet()){
			if( getLeases().get(ip) == null){
				return ip;
			}
		}
		return null;
	}
	
	public InetAddress getIPbyMAC(MACaddress chaddr) {
		for(InetAddress ip : getLeases().keySet()){
			Leaser leaser = getLeases().get(ip);
			if( leaser != null && leaser.getChaddr().equals(chaddr)){
				return ip;
			}
		}
		return null;
	}
	
	@Override
	public String toString(){
		String str = "Leases : ";
		for(InetAddress ip : getLeases().keySet()){
			Leaser leaser = getLeases().get(ip);
			if( leaser != null){
				str += "| "+ip.getHostAddress()+" - MAC:"+ leaser.getChaddr().toString() + " - StartTime: "+leaser.getStartTime()+" ";
			}
		}
		return str;
	}
	
	public void print(){
		System.out.println(toString());
	}

	
}


class checkLeaseExpiration extends TimerTask {
	private Leases leases;
	private InetAddress ip;
	private MACaddress chaddr;
	
    public checkLeaseExpiration(Leases leases, InetAddress ip, MACaddress chaddr) {
    	this.leases = leases;
    	this.ip = ip;
    	this.chaddr = chaddr;
	}

    @Override
	public void run() {
      if(leases.isLeaseExpired(ip)){
    	  System.out.println("----- LEASE EXPIRED: " + ip.getHostAddress() + " -----");
    	  leases.release(ip, chaddr);
      }
    }
}
