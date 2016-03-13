package threadedUDP;

import java.net.InetAddress;
import java.util.HashMap;

public class Leases {
	HashMap<InetAddress, Leaser> leases = new HashMap<>();
	
	void addNewIP(InetAddress ip){
		leases.put(ip, null);
	}
	
	void leaseIP(InetAddress ip, MACadress chaddr){
		leases.put(ip, new Leaser(chaddr, 0, 0)); // TODO timing
	}
	
	void removeLease(InetAddress ip){
		leases.put(ip, null); 
	}
	
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
	
	@Override
	public String toString(){
		String str = "";
		for(InetAddress ip : getLeases().keySet()){
			Leaser leaser = getLeases().get(ip);
			if( leaser != null){
				str += "| "+ip.getHostAddress()+" - MAC:"+" - StartTime: "+leaser.getStartTime()+" ";
			}
		}
		return str;
	}
	
	public void print(){
		System.out.println(toString());
	}
}
