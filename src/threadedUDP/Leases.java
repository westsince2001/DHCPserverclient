package threadedUDP;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


public class Leases {
	static int LEASE_TIME; // Lease time in seconds
	
	/**
	 * CONSTRUCTOR
	 */
	public Leases() {
		LEASE_TIME = getConfig().getLeaseTime();
	}

	/**
	 * Returns the IP pool (all IP addresses registered, leased and unleased)
	 * @return
	 */
	public Set<InetAddress> getPool(){
		return leases.keySet();
		
	}
	
	/**
	 * Adds a new IP to the pool
	 * 
	 * Does nothing if IP is already in pool
	 * @param ip the IP to be added
	 */
	void addNewIP(InetAddress ip){
		if(!isInPool(ip)){
			leases.put(ip, null);
			System.out.println("Added new IP. New IP Pool: "+poolToString());
		}
			
	}
	
	/**
	 * Adds all IP's from the given list to the pool
	 *
	 * @param ip the IP to be added
	 */
	void addNewIPList(ArrayList<InetAddress> iplist){
		for(InetAddress ip: iplist){
			addNewIP(ip);
		}
	}
	
	
	/**
	 * Checks if given ip is already in the pool
	 * @param ip
	 * @return
	 */
	boolean isInPool(InetAddress ip){
		return getLeases().containsKey(ip);
	}
	
	/**
	 * Returns the current time in Seconds
	 * @return the current time in seconds
	 */
	private long getCurrentTimeSeconds(){
		return System.currentTimeMillis() / 1000L;
	}
	
	/**
	 * Assigns the given IP to the client with the given MACAddress.
	 * 
	 * Assumes ip is not leased yet and given ip is valid
	 * @param ip
	 * @param chaddr
	 */
	void leaseIP(InetAddress ip, MACaddress chaddr){
		System.out.println("----- NEW LEASE: "+ip.getHostAddress()+" -----");
		assert(isInPool(ip));
		assert(!isLeased(ip));
		
		
		long startTime = getCurrentTimeSeconds(); // start time in seconds
		leases.put(ip, new LeasedIP(chaddr, startTime, LEASE_TIME));
		
		Timer timer = new Timer();
		timer.schedule(new checkLeaseExpiration(this, ip, chaddr), LEASE_TIME*1000);
	}
	
	/**
	 * Extends a given IP for the client with the given MAC address. The IP should be leased by the user with this IP address, otherwise the assert will fail.
	 * @param ip
	 * @param chaddr
	 */
	void extendLease(InetAddress ip, MACaddress chaddr){ 
		System.out.println("----- EXTENDING LEASE: "+ip.getHostAddress()+" -----");
		assert(isInPool(ip));
		assert(isLeasedBy(ip, chaddr));
		
		LeasedIP leaser = getLeaser(ip);
		leaser.setStartTime(getCurrentTimeSeconds());
		
		Timer timer = new Timer();
		timer.schedule(new checkLeaseExpiration(this, ip, chaddr), LEASE_TIME*1000);
	}
	
	/**
	 * Releases a given IP for the client with the given MAC address. The IP should be leased by the user with this IP address, otherwise the assert will fail.
	 * @param ip
	 * @param chaddr
	 */
	void release(InetAddress ip, MACaddress chaddr){
		assert(isLeasedBy(ip, chaddr));
		removeLease(ip);
	}
	
	/**
	 * Removees the lease from the HashMap. Private because it does not do any checks if this may be removed.
	 * @param ip
	 */
	private void removeLease(InetAddress ip){
		assert(isLeased(ip));
		System.out.println("----- LEASE RELEASED: " + ip.getHostAddress() + " -----");
		
		
		leases.put(ip, null); 
	}	
	
	/**
	 * Returns the Leaser of an ip. Returns null if ip is not leased.
	 * 
	 * Assumes IP is in pool
	 * 
	 * @param ip
	 * @return
	 */
	private LeasedIP getLeaser(InetAddress ip){
		assert(isInPool(ip));
		return leases.get(ip);
	}
	
	/**
	 * Checks if the given ip is leased
	 * 
	 * Assumes that IP is in pool
	 * 
	 * @param ip
	 * @return
	 */
	public boolean isLeased(InetAddress ip){
		assert(isInPool(ip));
		return getLeaser(ip) != null;
	}
	
	/**
	 * Checks if lease of a given IP is expired
	 * 
	 * Assumes IP is in pool
	 * @param ip
	 * @return
	 */
	boolean isLeaseExpired(InetAddress ip){
		assert(isInPool(ip));
		if(!isLeased(ip))
			return false;
		LeasedIP leaser = getLeaser(ip);
		if(leaser.getStartTime() + leaser.getLeaseTime() > getCurrentTimeSeconds())
			return false;
		return true;
	}
	
	/**
	 * Checks if 
	 * @param ip
	 * @param chaddr
	 * @return
	 */
	public boolean isLeasedBy(InetAddress ip, MACaddress chaddr){
		assert(isInPool(ip));
		if(!isLeased(ip))
			return false;
		LeasedIP leaser = getLeaser(ip);
		if(!leaser.getChaddr().equals(chaddr))
			return false;
		return true;
	}
	
	HashMap<InetAddress, LeasedIP> leases = new HashMap<>();
	
	public HashMap<InetAddress, LeasedIP> getLeases() {
		return leases;
	}

	public void setLeases(HashMap<InetAddress, LeasedIP> leases) {
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
			LeasedIP leaser = getLeases().get(ip);
			if( leaser != null && leaser.getChaddr().equals(chaddr)){
				return ip;
			}
		}
		return null;
	}
	
	
	public String poolToString(){
		String str = "";
		for(InetAddress ip : getLeases().keySet()){
				str += ip.getHostAddress()+", ";
		}
		return str;
	}
	
	@Override
	public String toString(){
		String str = "Leases : ";
		for(InetAddress ip : getLeases().keySet()){
			LeasedIP leaser = getLeases().get(ip);
			if( leaser != null){
				str += "| "+ip.getHostAddress()+" - MAC:"+ leaser.getChaddr().toString() + " - StartTime: "+leaser.getStartTime()+" ";
			}
		}
		return str;
	}
	
	public void print(){
		System.out.println(toString());
	}
	
	/* CONFIG */
	final Config config = new Config();
	
	public Config getConfig() {
		return config;
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
