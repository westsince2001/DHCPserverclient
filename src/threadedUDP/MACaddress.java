package threadedUDP;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class MACadress {
	
	public MACadress(byte[] mac) {
		setMac(mac);
	}
	
	byte[] mac;
	
	public byte[] toBytes() {
		return mac;
	}

	public void setMac(byte[] mac) {
		this.mac = mac;
	}
	
	@Override
	public String toString(){
		return Utils.toHexString(toBytes());
	}
	
	
	/* GET ADDRESSES */
	public static MACadress getMacAddressThisComputer() {
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
			return new MACadress(mac_byte);
		} catch (Exception e) {
			return null;
		}

	}
}
