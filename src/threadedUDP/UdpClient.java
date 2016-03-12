
package threadedUDP;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Random;

import com.sun.org.apache.bcel.internal.generic.GETFIELD;

import DHCPEnum.Hlen;
import DHCPEnum.Htype;
import DHCPEnum.Opcode;

class UdpClient extends Node {
	
	public UdpClient() {
		setClientSocket(null);
		setCurrentClientIP(null);
		currentTransactionID = rand.nextInt((int) Math.pow(2, 32)); // Random transaction id
		setServerID(null);
		macAddress = getMacAddressThisComputer();
		setLeaseTime(0);
		resetSecondsElapsedSinceAck();
	}
	

	DatagramSocket clientSocket;
	private InetAddress currentClientIP;
	private static Random rand = new Random();
	private int currentTransactionID; // Random transaction id
	private byte[] serverID;
	private byte[] macAddress;
	
	
	
	public DatagramSocket getClientSocket() {
		return clientSocket;
	}

	public void setClientSocket(DatagramSocket clientSocket) {
		this.clientSocket = clientSocket;
	}
	
	public byte[] getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(byte[] macAddress) {
		this.macAddress = macAddress;
	}

	private Long startLeaseTime = null; // TODO null pointer!!!!
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

	public void connectToServer() {
		System.out.println("###########################");
		System.out.println("#                         #");
		System.out.println("#   DHCP CLIENT STARTED   #");
		System.out.println("#      Tuur Van Daele     #");
		System.out.println("#      Thomas Verelst     #");
		System.out.println("#                         #");
		System.out.println("###########################");
		System.out.println();
		
		// Create datagram socket
		try {
			setClientSocket(new DatagramSocket());
		} catch (SocketException e1) {
			System.out.println("Error! The datagram socket cannot be constructed!");
			e1.printStackTrace();
		}
		
		try {
			// Send discovery message
			sendDiscoveryMsg();
			
			// Answer and process Incoming messages
			processAndAnswerIncomingMessages();
			
			// Extending lease for 15 seconds
			extendLeaseFor(15);
			
			// Release resources
			sendReleaseMessage();
			release();
			
			
			
			// RELEASE TESTEN

			
			/*// Check if NAK
			answer = extendLeaseRequestMessage();
			 answer.print();
			 sendMsg(answer);
			
			 // Receive answer
			 byte[] receiveData = new byte[576]; // DHCP message maximum 576 bytes
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
//			byte[] receiveData = new byte[576]; // DHCP packet maximum 576 bytes
//			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
//			System.out.println("client's waiting to receive something");
//			clientSocket.receive(receivePacket);
//			System.out.println("Client received something");
//			byte[] byteMsg = receivePacket.getData();
//			msg = new DHCPMessage(byteMsg);
//			msg.print(); // debugging purposes

		} catch (Exception e) {
			System.out.println("Error! The resources are released and the connection is now closing.");
			if (getCurrentClientIP() != null){
				try{
					sendReleaseMessage();
				} catch(Exception e2){};
				release();
			}
			if (getClientSocket() != null)
				getClientSocket().close();
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
	
	public void extendLeaseFor(int nbOfSeconds) throws IOException, InterruptedException, UnknownMessageTypeException{
		System.out.println();
		System.out.println("##### CLIENT IS EXTENDING LEASE FOR 15 SECONDS #####");
		for(int i = 0;i < 1500; i++){
			// If the lease should be renewed (after 50% of lease) --> extend the lease
			if (shouldRenew()){
				DHCPMessage receivedMessage;
				do{
					 // Extend lease
					 DHCPMessage sendMsg = extendLeaseRequestMessage();
					 sendMsg(sendMsg);
					 
					 // Print sending message
					 System.out.println("o Client sends renew request after " + getSecondsElapsedSinceAck() + " seconds.");
					 sendMsg.print();
					
					 // Receive answer (with same transactionID)
					 receivedMessage = receiveMessage();
				} while (receivedMessage == null); // If no message received --> resend renewing message
				 
				 // Print answer
				 System.out.println("o Client receives " + receivedMessage.getType());
				 receivedMessage.print();
	
				 // Process answer
				 receivedMessage.getType().process(receivedMessage,this);
			}
			else{
				Thread.sleep(10);
			}
		
			
				 
		}
	}
	
	public boolean hasPassedTenSecondsSince(long startTime){
		long currentTime = System.currentTimeMillis();
		return startTime + 100000 <= currentTime; // in milliseconds
	}
	
	// Receive Message with transaction ID the same as the transaction ID of this client.
	public DHCPMessage receiveMessage() throws IOException, UnknownMessageTypeException{
		long startTime = System.currentTimeMillis();
		DHCPMessage receivedMsg;
		while(true){	
			// Receive packet
			byte[] receiveData = new byte[576]; // DHCP packet maximum 576 bytes
			DatagramPacket receivePacket = new DatagramPacket(receiveData,
			receiveData.length);
			getClientSocket().receive(receivePacket);
			
			// Unpack received packet
			byte[] byteMsg = receivePacket.getData();
			receivedMsg = new DHCPMessage(byteMsg);
			
			// If the transactionID of the received message equals the transaction ID of this client, return the received message
			if (receivedMsg.getTransactionID() == getCurrentTransactionID()){
				return receivedMsg;
			}
			
			// If the client hasn't received any valid (same transaction ID) message in 10 seconds, return null
			if (hasPassedTenSecondsSince(startTime)){
				return null;
			}
		}
	}
	
	public void processAndAnswerIncomingMessages() throws IOException, UnknownMessageTypeException{
		// Print
		System.out.println();
		System.out.println("##### CLIENT IS PROCESSING AND ANSWERING INCOMING MESSAGES #####");
		
		// Process and answer incoming messages until the received message not need to be answered anymore
		while(true) {
			// Receive message (with same transaction ID)
			DHCPMessage receiveMessage = receiveMessage();
			
			// If no message received --> break
			if (receiveMessage == null){
				break;
			}
				
			// Print received message
			System.out.println("o Client receives " + receiveMessage.getType());
			receiveMessage.print();
			
			// Process received message
			receiveMessage.getType().process(receiveMessage, this);
			
			// Reply
			DHCPMessage answer = receiveMessage.getType().getAnswer(receiveMessage, this);
			if (answer != null){
				sendMsg(answer);
				
				// Print
				System.out.println("o Client sends " + answer.getType()); // TODO
				answer.print();
			}else{
				break; // No need to answer no more (answer == null) --> break
			}
		}
	}

	/* TRANSACTIONS */

	// Discovery message

	void sendDiscoveryMsg() throws IOException{
		// Create discovery message
		DHCPMessage msg = getDiscoverMsg();
		
		// Send discovery message
		sendMsg(msg);
		
		// Printing
		System.out.println();
		System.out.println("##### CLIENT HAS JUST SENT DISCOVERY MESSAGE #####");
		msg.print(); // print message content	
	}
	
	
	// fields gecontroleerd
	@Override
	DHCPMessage getDiscoverMsg() throws UnknownHostException {
		Opcode op = Opcode.BOOTREQUEST;
		Htype htype = Htype.ETHERNET;
		Hlen hlen = Hlen.INTERNET;
		byte hops = 0; // 0 voor discover
		int transactionID = getCurrentTransactionID();
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
		int transactionID = getCurrentTransactionID();
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
		int transactionID = getCurrentTransactionID();
		short num_of_seconds = 0; /* TODO: NOG DOEN */
		byte[] flags = UNICAST_FLAG;
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
		return null;
	}
	
	@Override
	void processAck(DHCPMessage msg){
		setCurrentClientIP(msg.getYourClientIP());
		setServerID(msg.getOptions().getOption(54));
		
		// Reset start time ack
		resetSecondsElapsedSinceAck();
		setLeaseTime(Utils.fromBytes(msg.getOptions().getOption(51)));
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
		
		// Create release message
		DHCPMessage releaseMessage = getReleaseMsg(); 
		
		// Send release message
		sendMsg(releaseMessage);
		
		// Print
		System.out.println();
		System.out.println("##### CLIENT HAS JUST RELEASED RESOURCES #####");
		releaseMessage.print();
	}
	
	void release(){
		// Delete client IP and server ID 
		setCurrentClientIP(null);
		setServerID(null);
		
		// Reset seconds elapsed since ack and lease time
		resetSecondsElapsedSinceAck();
		setLeaseTime(0);
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
		// Message to bytes
		byte[] sendData = msg.encode();
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("localhost"), // TODO 10.33.14.246 command line options: localhost of IP: niet recompilen
				1234);
		
		// Send message
		getClientSocket().send(sendPacket);
	}

	/* GET ADDRESSES */
	public static byte[] getMacAddressThisComputer() { // http://www.mkyong.com/java/how-to-get-mac-address-in-java/
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