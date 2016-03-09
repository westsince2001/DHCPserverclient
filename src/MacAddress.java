import java.net.*;
import java.util.Enumeration;

public class MacAddress{
	public static void main(String[] args) throws SocketException, UnknownHostException {
		String mac = null;
		InetAddress ip = InetAddress.getLocalHost();

		Enumeration e = NetworkInterface.getNetworkInterfaces();

		while(e.hasMoreElements()) {

		NetworkInterface n = (NetworkInterface) e.nextElement();
		Enumeration<InetAddress> ee = n.getInetAddresses();
		while(ee.hasMoreElements()) {
		InetAddress i = (InetAddress) ee.nextElement();
		if(!i.isLoopbackAddress() && !i.isLinkLocalAddress() && i.isSiteLocalAddress()) {
		ip = i;
		}
		}
		}

		NetworkInterface network = NetworkInterface.getByInetAddress(ip);
		byte[] mac_byte = network.getHardwareAddress();

		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < mac_byte.length; i++) {
		sb.append(String.format("%02X%s", mac_byte[i], (i < mac_byte.length -1) ? "-" : ""));
		}
		mac = sb.toString();
		System.out.println(mac);
	}
}