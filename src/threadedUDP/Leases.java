package threadedUDP;

import java.net.InetAddress;
import java.util.HashMap;

public class Leases {
	HashMap<InetAddress, Leaser> leases = new HashMap<>();
	
	void addNewIP(InetAddress ip){
		leases.put(ip, null);
	}
	
	void leaseIP(InetAddress ip, MACaddress chaddr){
		//int startTime = System.currentTimeMillis();
		
		
		leases.put(ip, new Leaser(chaddr, 0, 0)); // TODO timing
	}
	
	void extendLease(InetAddress ip){ 
		// TODO
		System.out.println("extending lease not implemented yet");
	}
	
	void removeLease(InetAddress ip){
		leases.put(ip, null); 
	}	
	
	public Leaser getLeaser(InetAddress ip){
		return leases.get(ip);
	}
	
	public boolean isLeased(InetAddress ip){ // TODO checken of dit ook werkt als ip niet in leases voorkomt!
		return getLeaser(ip) != null;
	}
	
	public boolean isLeasedBy(InetAddress ip, MACaddress chaddr){
		if(!isLeased(ip))
			return false;
		Leaser leaser = getLeaser(ip);
		if(!leaser.getChaddr().equals(chaddr))
			return false;
		return true;
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

	public InetAddress getIPbyMAC(MACaddress chaddr) {
		for(InetAddress ip : getLeases().keySet()){
			Leaser leaser = getLeases().get(ip);
			if( leaser != null && leaser.getChaddr().equals(chaddr)){
				return ip;
			}
		}
		return null;
	}
}
