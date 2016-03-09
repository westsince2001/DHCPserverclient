import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;


public class Experiment {
	
	public static void main(String[] args) {
		System.out.println(getMacAddress());
	}
	
	public static byte[] getMacAddress(){
		InetAddress ip;
		try {
			ip = InetAddress.getLocalHost();
			NetworkInterface network = NetworkInterface.getByInetAddress(ip);
			return network.getHardwareAddress();
			
		}catch (UnknownHostException | SocketException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		return null; // TODO 
		}
	}
}
