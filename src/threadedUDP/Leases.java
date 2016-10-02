package threadedUDP;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
		return poolMap.keySet();
		
	}
	
	/**
	 * Adds a new IP to the pool
	 * 
	 * Does nothing if IP is already in pool
	 * @param ip the IP to be added
	 */
	void addNewIP(InetAddress ip){
		if(!isInPool(ip)){
			poolMap.put(ip, null);
			System.out.println("Added new IP. New IP Pool: "+availableIpToString());
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
		return getPoolMap().containsKey(ip);
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
		assert(isInPool(ip));
		assert(!isLeased(ip));
		
		
		long startTime = getCurrentTimeSeconds(); // start time in seconds
		poolMap.put(ip, new LeasedIP(chaddr, startTime, LEASE_TIME));
		
		Timer timer = new Timer();
		timer.schedule(new checkLeaseExpiration(this, ip, chaddr), LEASE_TIME*1000);
	}
	
	/**
	 * Extends a given IP for the client with the given MAC address. The IP should be leased by the user with this IP address, otherwise the assert will fail.
	 * @param ip
	 * @param chaddr
	 */
	void extendLease(InetAddress ip, MACaddress chaddr){ 
		assert(isInPool(ip));
		assert(isLeasedBy(ip, chaddr));
		
		LeasedIP leaser = getLeaser(ip);
		leaser.setStartTime(getCurrentTimeSeconds());
		
		Timer timer = new Timer();
		timer.schedule(new checkLeaseExpiration(this, ip, chaddr), LEASE_TIME*1000);
	}
	
	/**
	 * Releases a given IP for the client with the given MAC address.
	 * 
	 * If the user has not leased this IP address, nothing will happen.
	 * @param ip
	 * @param chaddr
	 */
	void release(InetAddress ip, MACaddress chaddr){
		if(isLeasedBy(ip, chaddr)){
			removeLease(ip);
			print();
			System.out.println("  Available IP's: " + availableIpToString());
		}
	}
	
	/**
	 * Removees the lease from the HashMap. Private because it does not do any checks if this may be removed.
	 * @param ip
	 */
	private void removeLease(InetAddress ip){
		if(isLeased(ip)){
			poolMap.put(ip, null);
		} 
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
		return poolMap.get(ip);
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
	 * Checks if the given IP address is leased by the given MAC address.
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
	
	/************************************************* VARIABLES **********************************************************/
	
	HashMap<InetAddress, LeasedIP> poolMap = new HashMap<>();
	
	public HashMap<InetAddress, LeasedIP> getPoolMap() {
		return poolMap;
	}
	
	/************************************************** GETTERS AND SETTERS ****************************************************/

	public void setPoolMap(HashMap<InetAddress, LeasedIP> leases) {
		this.poolMap = leases;
	}
	
	InetAddress getNextAvailableIP(){
		for(InetAddress ip : getPoolMap().keySet()){
			if( getPoolMap().get(ip) == null){
				return ip;
			}
		}
		return null;
	}
	
	public InetAddress getIPbyMAC(MACaddress chaddr) {
		for(InetAddress ip : getPoolMap().keySet()){
			LeasedIP leaser = getPoolMap().get(ip);
			if( leaser != null && leaser.getChaddr().equals(chaddr)){
				return ip;
			}
		}
		return null;
	}
	
	/********************************************* TO STRING ***************************************************/
	
	/**
	 * Convert the available ip's to string.
	 * 
	 * @return
	 */
	public String availableIpToString(){
		String str = "";
		for(InetAddress ip : getPoolMap().keySet()){
			if (!isLeased(ip))
				str += ip.getHostAddress()+", ";
		}
		return str;
	}
	
	/**
	 * Convert this object to string
	 */
	@Override
	public String toString(){
		String str = "o Leases: ";
		for(InetAddress ip : getPoolMap().keySet()){
			LeasedIP leaser = getPoolMap().get(ip);
			if( leaser != null){
				Date date = new Date(leaser.getStartTime()*1000);
				SimpleDateFormat dt = new SimpleDateFormat("hh:mm:ss");
				str += ip.getHostAddress()+ " [" + leaser.getChaddr().toString() + " - " + dt.format(date)+"] | ";
			}
		}
		return str;
	}
	
	/**
	 * Print this object
	 */
	public void print(){
		System.out.println(toString());
	}
	
	/******************************************************CONFIG ******************************************************/
	final Config config = new Config();
	
	public Config getConfig() {
		return config;
	}
}

/**
 * Timer will be executed to check if lease is expired. If lease is expired, it will be removed from the leases.
 *
 */
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
    	  System.out.println("\n----- LEASE EXPIRED: " + ip.getHostAddress() + " -----");
    	  leases.release(ip, chaddr);
      }
    }
}
