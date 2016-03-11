
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
	private InetAddress currentClientIP;
	private int currentTransactionID;
	private byte[] serverID;
	
	
	
	private Long startLeaseTime = null; // null pointer!!!!
	private long leaseTime = 0;
	
	
	public boolean shouldRenew(){
		return getLeaseTime()/2 < getSecondsElapsedSinceAck();
	}

	public long getLeaseTime() {
		return leaseTime;
	}

	public void setLeaseTime(long leaseTime) {
		this.leaseTime = leaseTime;
	}

	public float getSecondsElapsedSinceAck() {
		System.out.println("StartLeaseTime" + startLeaseTime);
		System.out.println("currentTimeMillis" + (long)System.currentTimeMillis());
		System.out.println("difference" + ((long)System.currentTimeMillis() - startLeaseTime));
		return (System.currentTimeMillis() - startLeaseTime)/1000F;
	}

	public void resetSecondsElapsedSinceAck() {
		this.startLeaseTime = System.currentTimeMillis();
	}

	public byte[] getServerID() {
		return serverID;
	}

	public void setServerID(byte[] serverID) {
		this.serverID = serverID;
	}

	public int getCurrentTransactionID() {
		return currentTransactionID;
	}

	public void setCurrentTransactionID(int currentTransactionID) {
		this.currentTransactionID = currentTransactionID;
	}

	public InetAddress getCurrentClientIP() {
		return currentClientIP;
	}

	public void setCurrentClientIP(InetAddress currentClientIP) {
		this.currentClientIP = currentClientIP;
	}

	public static void main(String args[]) throws InterruptedException {
		UdpClient client = new UdpClient();
		client.connectToServer();
	}

	public UdpClient() {
	}

	public void connectToServer() throws InterruptedException {
		try {
			System.out.println("Client started");
			
			// Create datagram socket
			clientSocket = new DatagramSocket();

			// Send discovery message
			sendDiscoveryMsg();
			
			// Answer Incoming messages
			answerIncomingMessages(clientSocket);
			
			// Extend lease during 20 seconds
			extendLeaseFor(20);
			
			// Release resources
			sendReleaseMessage();
			
			// RELEASE TESTEN

			
			/*// Check if NAK
			answer = extendLeaseRequestMessage();
			 answer.print();
			 sendMsg(answer);
			
			 // Receive answer
			 byte[] receiveData = new byte[1024];
			 DatagramPacket receivePacket = new DatagramPacket(receiveData,
			 receiveData.length);
			 System.out.println("client's waiting to receive something");
			 clientSocket.receive(receivePacket);
			 System.out.println("Client received something");
			 byte[] byteMsg = receivePacket.getData();
			 DHCPMessage msg = new DHCPMessage(byteMsg);
			 msg.print(); // debugging purposes
			 getAckAnswer(msg);
			 // TODO moet IP nog verwijderen na rel*/
			
			
			

		// Release.. lijkt te werken maar niet zeker want hoeft geen antwoord te krijgen :/
//			System.out.println("releasing");
//			
//			// Extend lease
//			answer = getReleaseMsg(new DHCPMessage());
//			answer.print();
//			sendMsg(answer);
//
//			// Receive answer
//			byte[] receiveData = new byte[1024];
//			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
//			System.out.println("client's waiting to receive something");
//			clientSocket.receive(receivePacket);
//			System.out.println("Client received something");
//			byte[] byteMsg = receivePacket.getData();
//			msg = new DHCPMessage(byteMsg);
//			msg.print(); // debugging purposes

		} catch (IOException e) {
			System.out.println(e);
			// Moet hier clientsocket nog sluiten? Maar object is in try
			// gemaakt..
		}
	}

	/* Read from txt file */

	private static int getPort() throws IOException {
		Properties pro = new Properties();
		FileInputStream in = new FileInputStream("src/udpconfig.txt");
		pro.load(in);
		String port = pro.getProperty("port");
		int result = Integer.parseInt(port);
		return result;
	}
	
	public void extendLeaseFor(int nbOfSeconds) throws IOException, InterruptedException{
		// TODO nbOfSeconds
		for(int i = 0;i <2; i++){
			if (shouldRenew()){
				System.out.println("seconds elapsed since ack" + getSecondsElapsedSinceAck());
				 // Extend lease
				 DHCPMessage answer = extendLeaseRequestMessage();
				 answer.print();
				 sendMsg(answer);
				
				 // Receive answer
				 byte[] receiveData = new byte[1024];
				 DatagramPacket receivePacket = new DatagramPacket(receiveData,
				 receiveData.length);
				 System.out.println("client's waiting to receive something");
				 clientSocket.receive(receivePacket);
				 System.out.println("Client received something");
				 byte[] byteMsg = receivePacket.getData();
				 try {
					 DHCPMessage msg = new DHCPMessage(byteMsg);
					 msg.print(); // debugging purposes
					 getAckAnswer(msg);
				} catch (UnknownMessageTypeException e) {
					// TODO: handle exception
				}
				
		}
			else{
				Thread.sleep(7000); // voor sewes als release test
			}
		
			
				 
		}
	}
	
	public void answerIncomingMessages(DatagramSocket clientSocket) throws IOException{
		while(true) {
			System.out.println("# client is listening");
			// Receive answer
			byte[] receiveData = new byte[1024];
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			clientSocket.receive(receivePacket);
			System.out.println("# client received message");
			byte[] byteMsg = receivePacket.getData();
			
			
			DHCPMessage answer;
			try{
				DHCPMessage msg = new DHCPMessage(byteMsg);
				msg.print();
				
				// Reply to msg
				answer = msg.getType().getAnswer(msg, this);
			}catch(UnknownMessageTypeException e){
				System.out.println("WARNING: received DHCP message without Type option (53)!");
				answer = null;
			}
			
			if (answer != null) {
				System.out.println("# client generated message");
				answer.print();
				sendMsg(answer);
			}
			else
				break; // Should not reply anymore --> break
		} 
	}

	/* TRANSACTIONS */

	// Discovery message

	void sendDiscoveryMsg() throws IOException{
		System.out.println("##### SEND DISCOVERY MESSAGE ######");
		DHCPMessage msg = getDiscoverMsg();
		msg.print(); // print message
		sendMsg(msg);
	}
	
	
	// fields gecontroleerd
	@Override
	DHCPMessage getDiscoverMsg() throws UnknownHostException {
		Opcode op = Opcode.BOOTREQUEST;
		Htype htype = Htype.ETHERNET;
		Hlen hlen = Hlen.INTERNET;
		byte hops = 0; // 0 voor discover
		Random rand = new Random();
		int transactionID = rand.nextInt((int) Math.pow(2, 32)); // Random transaction id
		short num_of_seconds = 0; 
		byte[] flags = BROADCAST_FLAG;
		InetAddress clientIP = InetAddress.getByName("0.0.0.0");
		InetAddress yourClientIP = InetAddress.getByName("0.0.0.0");
		InetAddress serverIP = InetAddress.getByName("0.0.0.0");
		InetAddress gatewayIP = InetAddress.getByName("0.0.0.0");
		
		byte[] chaddr = getMacAddress();
		byte[] sname = new byte[64]; // TODO: wrs 0 voor discover
		byte[] file = new byte[128]; // TODO: wrs 0 voor discover

		DHCPOptions options = new DHCPOptions();
		options.addOption(53, MessageType.DHCPDISCOVER.getValue());
		options.addOption(255);
		
		DHCPMessage answer = new DHCPMessage(op, htype, hlen, hops, transactionID, num_of_seconds, flags, clientIP, yourClientIP, serverIP, gatewayIP, chaddr, sname, file, options);

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
		DHCPMessage answer = getNewIPRequestMsg(msg);
		return answer;
	}

	// Request

	// gecontroleerd fields
	@Override
	DHCPMessage getNewIPRequestMsg(DHCPMessage msg) throws UnknownHostException {
		// ENKEL REQUEST VOOR NA DISCOVER

		Opcode op = Opcode.BOOTREQUEST;
		Htype htype = Htype.ETHERNET;
		Hlen hlen = Hlen.INTERNET;
		byte hops = 0; // 0 voor request
		int transactionID = msg.getTransactionID();
		short num_of_seconds = 0; /* TODO: NOG DOEN */
		byte[] flags = BROADCAST_FLAG;
		
		InetAddress clientIP = InetAddress.getByName("0.0.0.0");
		InetAddress yourClientIP = InetAddress.getByName("0.0.0.0");
		InetAddress serverIP = InetAddress.getByName("0.0.0.0");
		InetAddress gatewayIP = InetAddress.getByName("0.0.0.0");
		
		byte[] chaddr = getMacAddress();
		byte[] sname = new byte[64]; // TODO:
		byte[] file = new byte[128]; // TODO:
		DHCPOptions options = new DHCPOptions();
		options.addOption(53, MessageType.DHCPREQUEST.getValue());
		options.addOption(50, msg.getYourClientIP().getAddress()); // requested IP address  MUST
		options.addOption(54, msg.getOptions().getOption(54)); // Server identifier MUST
		options.addOption(255);
		
		return new DHCPMessage(op, htype, hlen, hops, transactionID, num_of_seconds, flags, clientIP, yourClientIP, serverIP, gatewayIP,
				chaddr, sname, file, options);
	}
	
	
	// gecontroleerd fields
	@Override
	DHCPMessage extendLeaseRequestMessage() throws UnknownHostException {
		Opcode op = Opcode.BOOTREQUEST;
		Htype htype = Htype.ETHERNET;
		Hlen hlen = Hlen.INTERNET;
		byte hops = 0; // 0 voor request
		System.out.println("current transaction ID" + getCurrentTransactionID());
		int transactionID = getCurrentTransactionID();
		short num_of_seconds = 0; /* TODO: NOG DOEN */
		byte[] flags = UNICAST_FLAG;
		System.out.println("getCurrentClientIP" + getCurrentClientIP());
		
		InetAddress clientIP = getCurrentClientIP();
		InetAddress yourClientIP = InetAddress.getByName("0.0.0.0");
		InetAddress serverIP = InetAddress.getByName("0.0.0.0");
		InetAddress gatewayIP = InetAddress.getByName("0.0.0.0");

		byte[] chaddr = getMacAddress();
		byte[] sname = new byte[64]; // TODO:
		byte[] file = new byte[128]; // TODO:
		DHCPOptions options = new DHCPOptions();
		options.addOption(53, MessageType.DHCPREQUEST.getValue());
		//options.addOption(50, getCurrentClientIP().getAddress()); // MAG NIET BIJ RENEWING, ENKEL BIJ SELECTING EN REBOOTING!!!!!!!!
		//options.addOption(54, getServerID()); //mag alleen na SELECT!
		options.addOption(255);
		
		return new DHCPMessage(op, htype, hlen, hops, transactionID, num_of_seconds, flags, clientIP, yourClientIP, serverIP, gatewayIP, chaddr, sname, file, options);

	}

	@Override
	DHCPMessage getRequestAnswer(DHCPMessage msg) {
		System.out.println("Client received DHCP_REQUEST but shouldn't process it.");
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
		// Vanaf ack is jou IP address
		
		// Atributen opslaan
		setCurrentClientIP(msg.getYourClientIP()); // dummy: mag geen state veranderen in getter
		setCurrentTransactionID(msg.getTransactionID());
		setServerID(msg.getOptions().getOption(54));
		
		// Reset start time ack
		resetSecondsElapsedSinceAck();
		setLeaseTime(Utils.fromBytes(msg.getOptions().getOption(51)));
		
		
		
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
	
	public void sendReleaseMessage() throws IOException{
		System.out.println("release");
		DHCPMessage answer = getReleaseMsg(); // TODO: ip uitschakelen
		answer.print();
		sendMsg(answer);
	}

	@Override
	DHCPMessage getReleaseMsg() throws UnknownHostException {
		// msg may be empty
		
		Opcode op = Opcode.BOOTREQUEST;
		Htype htype = Htype.ETHERNET;
		Hlen hlen = Hlen.INTERNET;
		byte hops = 0; // 0 volgens assistent
		int transactionID = getCurrentTransactionID();
		short num_of_seconds = 0;
		byte[] flags = UNICAST_FLAG;
;
		InetAddress clientIP = getCurrentClientIP();
		InetAddress yourClientIP = InetAddress.getByName("0.0.0.0");
		InetAddress serverIP = InetAddress.getByName("0.0.0.0"); 
		InetAddress gatewayIP = InetAddress.getByName("0.0.0.0");

		byte[] chaddr = getMacAddress();
		byte[] sname = new byte[64]; // 0 volgens assistent
		byte[] file = new byte[128]; // 0 volgens assistent
		DHCPOptions options = new DHCPOptions();
		options.addOption(53, MessageType.DHCPRELEASE.getValue());
		// options.addOption(50, getCurrentClientIP().getAddress()); // Requested IP adress MUST NOT
		options.addOption(54, getServerID()); // Server identifier MUST
		options.addOption(255);
		
		return new DHCPMessage(op, htype, hlen, hops, transactionID, num_of_seconds, flags, clientIP, yourClientIP, serverIP, gatewayIP,
				chaddr, sname, file, options);

	}

	@Override
	DHCPMessage getReleaseAnswer(DHCPMessage msg) {
		// TODO Auto-generated method stub
		return null;
	}

	/* SEND MESSAGE */
	
	private void sendMsg(DHCPMessage msg) throws IOException {
		byte[] sendData = msg.encode();
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("localhost"), // TODO 10.33.14.246 command line options: localhost of IP: niet recompilen
				1234);
		clientSocket.send(sendPacket);
		System.out.println("# client sent message");
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