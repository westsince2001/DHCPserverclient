package threadedUDP;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Arrays;
import java.util.Enumeration;

public class MACaddress {
	
	public MACaddress(byte[] mac) {
		setMac(mac);
	}
	
	byte[] mac;
	
	public byte[] toBytes() {
		return mac;
	}

	public void setMac(byte[] mac) {
		this.mac =Arrays.copyOfRange(mac, 0, 16); // be sure it's always 16 bytes
	}
	
	@Override
	public String toString(){
		return Utils.toHexString(toBytes());
	}
	
	public boolean equals(MACaddress m){
		return Arrays.equals(m.toBytes(), this.toBytes());
	}
	
	
	/* GET ADDRESSES */
	public static MACaddress getMacAddressThisComputer() {
		try {
			InetAddress ip = InetAddress.getLocalHost();

			Enumeration<?> e = NetworkInterface.getNetworkInterfaces();

			while (e.hasMoreElements()) {
				NetworkInterface n = (NetworkInterface) e.nextElement();
				Enumeration<InetAddress> ee = n.getInetAddresses();
				while (ee.hasMoreElements()) {
					InetAddress i = (InetAddress) ee.nextElement();
					if (!i.isLoopbackAddress() && !i.isLinkLocalAddress() && i.isSiteLocalAddress()) {
						ip = i;
					}
				}
			}

			NetworkInterface network = NetworkInterface.getByInetAddress(ip);
			byte[] mac_byte = network.getHardwareAddress();
			return new MACaddress(mac_byte);
		} catch (Exception e) {
			return null;
		}

	}
}
