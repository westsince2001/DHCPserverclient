package threadedUDP;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Random;

import DHCPEnum.Hlen;
import DHCPEnum.Htype;
import DHCPEnum.Opcode;

class UdpClient extends Node {

	DatagramSocket clientSocket;

	public static void main(String args[]) {
		UdpClient client = new UdpClient();
		client.connectToServer();
	}

	public UdpClient() {
	}

	public void connectToServer() {
		try {
			System.out.println("client started");
			clientSocket = new DatagramSocket();

			// Send DHCP_Discover
			DHCPMessage msg = getDiscoverMsg();
			System.out.println("Client generated discover:");
			msg.print();
			sendMsg(msg);

			// Answer incoming messages
			while (true) { // while (msg != null), achteraf connectie sluiten

				// Receive answer
				byte[] receiveData = new byte[1024];
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				System.out.println("client's waiting to receive something");
				clientSocket.receive(receivePacket);
				System.out.println("Client received something");

				// Reply to answer
				byte[] byteMsg = receivePacket.getData();
				msg = new DHCPMessage(byteMsg);
				msg.print(); // dummy for debugging
				DHCPMessage answer = msg.getType().getAnswer(msg, this);
				System.out.println("Client generated answer:");
				if(answer != null){
					answer.print();
					sendMsg(answer);
				}else{
					System.out.println("Answer is null!");
				}
				
			}
		} catch (IOException e) {
			System.out.println(e);
			// Moet hier clientsocket nog sluiten? Maar object is in try
			// gemaakt..
		}
	}

	private static int getPort() throws IOException {
		Properties pro = new Properties();
		FileInputStream in = new FileInputStream("src/udpconfig.txt");
		pro.load(in);
		String port = pro.getProperty("port");
		int result = Integer.parseInt(port);
		return result;
	}

	/* TRANSACTIONS */

	// Discover

	@Override
	DHCPMessage getDiscoverMsg() throws UnknownHostException {
		Opcode op = Opcode.BOOTREQUEST;
		Htype htype = Htype.ETHERNET;
		Hlen hlen = Hlen.INTERNET;
		byte hops = 0; // 0 voor discover
		Random rand = new Random();
		int transactionID = rand.nextInt((int) Math.pow(2, 32)); // Random transaction id
													// tussen 0 en 2^32
		short num_of_seconds = 0; /* TODO: overnemen uit msg ? */
		byte[] flags = new byte[] { 0x0, 0x0 }; /*
												 * TODO: flags moet nog naar
												 * broadcast
												 */
		InetAddress clientIP = InetAddress.getByName("0.0.0.0"); // 0 voor
																	// discover
		InetAddress yourClientIP = InetAddress.getByName("0.0.0.0"); // 0 voor
																		// discover
		InetAddress serverIP = InetAddress.getByName("0.0.0.0"); // 0 voor
																	// discover
		InetAddress gatewayIP = InetAddress.getByName("0.0.0.0"); // 0 voor
																	// discover
		byte[] chaddr = getMacAddress();
		byte[] sname = new byte[64]; // TODO: wrs 0 voor discover
		byte[] file = new byte[128]; // TODO: wrs 0 voor discover
		
		DHCPOptions options = new DHCPOptions();
		options.addOption(53, new byte[] { (byte) MessageType.DHCPDISCOVER.getValue() });
		
		DHCPMessage answer = new DHCPMessage(op, htype, hlen, hops, transactionID, num_of_seconds, flags, clientIP, yourClientIP, serverIP, gatewayIP, chaddr,
				sname, file, options);

		return answer;
	}

	@Override
	public DHCPMessage getDiscoverAnswer(DHCPMessage msg) {
		System.out.println("Client received DHCP_DISCOVER but shouldn't process it.");
		return null;
	}

	// Offer

	@Override
	DHCPMessage getOfferMsg(DHCPMessage msg) throws UnknownHostException {
		System.out.println("Clients cannot send DHCP_OFFER.");
		return null;
	}

	@Override
	DHCPMessage getOfferAnswer(DHCPMessage msg) throws UnknownHostException {
		DHCPMessage answer = getRequestMsg(msg);
		return answer;
	}

	// Request

	@Override
	DHCPMessage getRequestMsg(DHCPMessage msg) throws UnknownHostException {
		// ENKEL REQUEST VOOR NA DISCOVER

		Opcode op = Opcode.BOOTREQUEST;
		Htype htype = Htype.ETHERNET;
		Hlen hlen = Hlen.INTERNET;
		byte hops = 0; // 0 voor request
		int transactionID = msg.getTransactionID();
		short num_of_seconds = 0; /* TODO: NOG DOEN */
		byte[] flags = new byte[] { 0x0, 0x0 }; /*
												 * TODO: flags moet nog naar
												 * broadcast
												 */
		InetAddress clientIP = msg.getYourClientIP(); // 0 or
																	// client's
																	// network
																	// address
		InetAddress yourClientIP = InetAddress.getByName("0.0.0.0"); // 0 voor
																		// request
		InetAddress serverIP = msg.getServerIP(); // 0 voor
																	// request
		InetAddress gatewayIP = msg.getGatewayIP(); // 0 voor
																	// request
		byte[] chaddr = getMacAddress();
		byte[] sname = new byte[64]; // TODO: 
		byte[] file = new byte[128]; // TODO: 
		DHCPOptions options = new DHCPOptions();
		options.addOption(53, new byte[] { (byte) MessageType.DHCPREQUEST.getValue() });
		options.addOption(50, msg.getYourClientIP().getAddress()); // requested IP adress
		//options.addOption(54, msg.getOptions().getOption(54)); server identifier - niet noodzakelijk maar mss wel aan te raden?
		
		
		DHCPMessage requestMsg = new DHCPMessage(op, htype, hlen, hops, transactionID, num_of_seconds, flags, clientIP, yourClientIP, serverIP, gatewayIP,
				chaddr, sname, file, options);

		return requestMsg;
	}

	@Override
	DHCPMessage getRequestAnswer(DHCPMessage msg) {
		// TODO Auto-generated method stub
		return null;
	}

	// Acknowledge

	@Override
	DHCPMessage getAckMsg(DHCPMessage msg) throws UnknownHostException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	DHCPMessage getAckAnswer(DHCPMessage msg) {
		// TODO Auto-generated method stub
		return null;
	}

	// Not Acknowledge

	@Override
	DHCPMessage getNakMsg(DHCPMessage msg) throws UnknownHostException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	DHCPMessage getNakAnswer(DHCPMessage msg) {
		// TODO Auto-generated method stub
		return null;
	}

	// Release

	@Override
	DHCPMessage getReleaseMsg(DHCPMessage msg) throws UnknownHostException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	DHCPMessage getReleaseAnswer(DHCPMessage msg) {
		// TODO Auto-generated method stub
		return null;
	}

	/* SEND MESSAGE */

	private void sendMsg(DHCPMessage msg) throws IOException {
		byte[] sendData = msg.encode();
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("10.33.14.246"), // TODO
				1234);
		clientSocket.send(sendPacket);
	}

	/* GET ADDRESSES */
	public static byte[] getMacAddress() { // http://www.mkyong.com/java/how-to-get-mac-address-in-java/
		try {
			String mac = null;
			InetAddress ip = InetAddress.getLocalHost();

			Enumeration e = NetworkInterface.getNetworkInterfaces();

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
			return Arrays.copyOfRange(mac_byte, 0, 16);
		} catch (Exception e) {
			return null;// dummy
		}

	}

}