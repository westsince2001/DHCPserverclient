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
import Exceptions.UnknownMessageTypeException;

class UdpClient extends Node {
	
	/* CONSTRUCTOR */
	
	public UdpClient() {
		setClientSocket(null);
		setCurrentClientIP(null);
		currentTransactionID = rand.nextInt((int) Math.pow(2, 32)); // Random transaction id
		setServerID(null);
		macAddress = MACadress.getMacAddressThisComputer();
		setLeaseTime(0);
		resetSecondsElapsedSinceAck();
		setPreviousSentMessage(null);
	}
	
	/* MAIN METHOD */

	public static void main(String args[]) {
		UdpClient client = new UdpClient();
		client.connectToServer();
	}
	
	/* CONNECT TO SERVER */

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
			
			// Extend lease for 15 seconds
			extendValidLeaseFor(15);
			
			// Release resources
			sendReleaseMessage();
			processRelease(null);
			
			// Invalid renewing
			setCurrentClientIP(InetAddress.getByName("0.0.0.1")); // Set invalid IP
			extendInvalidLeaseFor(3); // TODO: moet NAK krijgen!
			
		} catch (Exception e) {
			System.out.println("Error! The resources are being released and the serversocket is being deleted.");
			e.printStackTrace();
			
		// Release resources and closing datagram socket after execution
		}finally{
			System.out.println();
			System.out.println("##### RELEASING RESOURCES AND CLOSING SOCKET #####");
			closeConnection();
		}
	}
	
	/* Helper methods for connectToServer() */
	
	private void closeConnection(){
		// Release resources if possible
		if (getCurrentClientIP() != null){
			processRelease(null);
			try{
				sendReleaseMessage();
			} catch(Exception e2){};
		}
		
		// Close client socket
		if (getClientSocket() != null)
			getClientSocket().close();				
	}
	
	public void extendValidLeaseFor(int nbOfSeconds) throws IOException, InterruptedException, UnknownMessageTypeException{
		// Print
		System.out.println();
		System.out.println("##### CLIENT IS EXTENDING VALID LEASE FOR 15 SECONDS #####");
		
		// Extending lease for nbOfSeconds seconds
		extendLeaseFor(nbOfSeconds);
	}
	
	public void extendInvalidLeaseFor(int nbOfSeconds) throws IOException, InterruptedException, UnknownMessageTypeException{
		// Print
		System.out.println();
		System.out.println("##### CLIENT IS EXTENDING INVALID LEASE #####");
		
		// Extending lease for nbOfSeconds seconds
		extendLeaseFor(nbOfSeconds);
	}
	
	public void extendLeaseFor(int nbOfSeconds) throws IOException, InterruptedException, UnknownMessageTypeException{
		for(int i = 0;i < nbOfSeconds * 100; i++){
			// If the lease should be renewed (after 50% of lease) --> extend the lease
			if (shouldRenew()){
				DHCPMessage receivedMessage;
				
				 // Extend lease
				 DHCPMessage sendMsg = extendLeaseRequestMessage();
				 sendMsg(sendMsg);
				 
				 // Print sending message
				 System.out.println("o Client sends renew request after " + getSecondsElapsedSinceAck() + " seconds.");
				 sendMsg.print();
				
				// Receive message (with same transaction ID). If no valid message received after 10 seconds --> resend previous message.
				 receivedMessage = receiveMessage();
				 
				 // Print answer
				 System.out.println("o Client receives " + receivedMessage.getType());
				 receivedMessage.print();
	
				 // Process answer
				 receivedMessage.getType().process(receivedMessage,this);
			}
			else{
				Thread.sleep(10); // wait 10 milliseconds
			}	 
		}
	}

	
	public void processAndAnswerIncomingMessages() throws IOException, UnknownMessageTypeException{
		// Print
		System.out.println();
		System.out.println("##### CLIENT IS PROCESSING AND ANSWERING INCOMING MESSAGES #####");
		
		// Process and answer incoming messages until the received message not need to be answered anymore
		DHCPMessage answer;
		do{
			// Receive message (with same transaction ID). If no valid message received after 10 seconds --> resend previous message.
			DHCPMessage receiveMessage = receiveMessage();
			
			// Print received message
			System.out.println("o Client receives " + receiveMessage.getType());
			receiveMessage.print();
			
			// Process received message
			receiveMessage.getType().process(receiveMessage, this);
			
			// Reply
			answer = receiveMessage.getType().getAnswer(receiveMessage, this);
			if (answer != null){
				sendMsg(answer);
				
				// Print
				System.out.println("o Client sends " + answer.getType());
				answer.print();
			}
		} while (answer != null);
	}

	
	/* SEND MESSAGES AND RECEIVE MESSAGES */
	
	private void sendMsg(DHCPMessage msg) throws IOException {
		// Message to bytes
		byte[] sendData = msg.encode();
		
		// Make sending packet
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("localhost"), // TODO 10.33.14.246 command line options: localhost of IP: niet recompilen
				1234); // TODO getPort()
		
		// Send message
		getClientSocket().send(sendPacket);
		
		// Set the previous sent message to the just sent message
		setPreviousSentMessage(msg);
	}
	
	
	// Receive Message with transaction ID the same as the transaction ID of this client.
	public DHCPMessage receiveMessage() throws IOException, UnknownMessageTypeException{
		DHCPMessage receivedMsg;
		byte[] receiveData = new byte[576]; // DHCP packet maximum 576 bytes
		DatagramPacket receivePacket = new DatagramPacket(receiveData,receiveData.length);
		
		do{	
			// Receive packet (nothing received after 10 seconds --> message is resent)
			receivePacket = receivePacket(receivePacket);
			
			// Unpack received packet
			byte[] byteMsg = receivePacket.getData();
			receivedMsg = new DHCPMessage(byteMsg);

		} while (receivedMsg.getTransactionID() != getCurrentTransactionID()); // Transaction ID's != --> keep on listening
		
		return receivedMsg;
	}
	
	public DatagramPacket receivePacket(DatagramPacket receivePacket) throws IOException{
		getClientSocket().setSoTimeout(10000); // socket throws exception after 10 seconds waiting for message.
		
		// Listening for message for 10 seconds
		try{
			getClientSocket().receive(receivePacket);
		} 
		
		// If after 10 seconds nothing received--> resend message and listen again
		catch(SocketTimeoutException e){
			System.out.println("o Client is resending previous message");
			getPreviousSentMessage().print();
			sendMsg(getPreviousSentMessage()); // Resend previous message
			return receivePacket(receivePacket); // Listen again
		}
		
		return receivePacket;
	}

	/* READ FROM TEXT FILE */

	private static int getPort() throws IOException {
		Properties pro = new Properties();
		FileInputStream in = new FileInputStream("src/udpconfig.txt");
		pro.load(in);
		String port = pro.getProperty("port");
		int result = Integer.parseInt(port);
		return result;
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
	
	@Override
	DHCPMessage getDiscoverMsg() throws UnknownHostException {
		Opcode op = Opcode.BOOTREQUEST;
		Htype htype = Htype.ETHERNET;
		Hlen hlen = Hlen.INTERNET;
		byte hops = 0;
		int transactionID = getCurrentTransactionID();
		short num_of_seconds = 0; 
		byte[] flags = BROADCAST_FLAG;
		InetAddress clientIP = InetAddress.getByName("0.0.0.0");
		InetAddress yourClientIP = InetAddress.getByName("0.0.0.0");
		InetAddress serverIP = InetAddress.getByName("0.0.0.0");
		InetAddress gatewayIP = InetAddress.getByName("0.0.0.0");
		
		byte[] chaddr = getMacAddress().getBytes();
		byte[] sname = new byte[64];
		byte[] file = new byte[128];

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
	
	@Override
	void processOffer(DHCPMessage msg){
		// do nothing
	}

	// Request

	@Override
	DHCPMessage getNewIPRequestMsg(DHCPMessage msg) throws UnknownHostException {
		// ENKEL REQUEST VOOR NA DISCOVER

		Opcode op = Opcode.BOOTREQUEST;
		Htype htype = Htype.ETHERNET;
		Hlen hlen = Hlen.INTERNET;
		byte hops = 0;
		int transactionID = getCurrentTransactionID();
		short num_of_seconds = 0;
		byte[] flags = BROADCAST_FLAG;
		
		InetAddress clientIP = InetAddress.getByName("0.0.0.0");
		InetAddress yourClientIP = InetAddress.getByName("0.0.0.0");
		InetAddress serverIP = InetAddress.getByName("0.0.0.0");
		InetAddress gatewayIP = InetAddress.getByName("0.0.0.0");
		
		byte[] chaddr = getMacAddress().getBytes();
		byte[] sname = new byte[64];
		byte[] file = new byte[128];
		DHCPOptions options = new DHCPOptions();
		options.addOption(53, MessageType.DHCPREQUEST.getValue());
		options.addOption(50, msg.getYourClientIP().getAddress()); // requested IP address  MUST
		options.addOption(54, msg.getOptions().getOption(54)); // Server identifier MUST
		options.addOption(255);
		
		return new DHCPMessage(op, htype, hlen, hops, transactionID, num_of_seconds, flags, clientIP, yourClientIP, serverIP, gatewayIP,
				chaddr, sname, file, options);
	}
	
	@Override
	DHCPMessage extendLeaseRequestMessage() throws UnknownHostException {
		Opcode op = Opcode.BOOTREQUEST;
		Htype htype = Htype.ETHERNET;
		Hlen hlen = Hlen.INTERNET;
		byte hops = 0;
		int transactionID = getCurrentTransactionID();
		short num_of_seconds = 0;
		byte[] flags = UNICAST_FLAG;
		InetAddress clientIP = getCurrentClientIP();
		InetAddress yourClientIP = InetAddress.getByName("0.0.0.0");
		InetAddress serverIP = InetAddress.getByName("0.0.0.0");
		InetAddress gatewayIP = InetAddress.getByName("0.0.0.0");
		
		byte[] chaddr = getMacAddress().getBytes();
		byte[] sname = new byte[64];
		byte[] file = new byte[128];
	
		
		DHCPOptions options = new DHCPOptions();
		options.addOption(53, MessageType.DHCPREQUEST.getValue());
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
		return null;
	}

	@Override
	DHCPMessage getNakAnswer(DHCPMessage msg) {
		return null;
	}
	
	@Override
	void processNak() throws IOException, UnknownMessageTypeException{
		// Print
		System.out.println();
		System.out.println("##### RECEIVED NAK. TRYING TO GET NEW IP. ######");
		
		// Try new IP address
		sendDiscoveryMsg();
		processAndAnswerIncomingMessages();
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
	
	@Override
	void processRelease(DHCPMessage message){
		// Delete client IP and server ID 
		setCurrentClientIP(null);
		setServerID(null);
		
		// Reset seconds elapsed since ack and lease time
		resetSecondsElapsedSinceAck();
		setLeaseTime(0);
	}

	@Override
	DHCPMessage getReleaseMsg() throws UnknownHostException {		
		Opcode op = Opcode.BOOTREQUEST;
		Htype htype = Htype.ETHERNET;
		Hlen hlen = Hlen.INTERNET;
		byte hops = 0;
		int transactionID = getCurrentTransactionID();
		short num_of_seconds = 0;
		byte[] flags = UNICAST_FLAG;
;
		InetAddress clientIP = getCurrentClientIP();
		InetAddress yourClientIP = InetAddress.getByName("0.0.0.0");
		InetAddress serverIP = InetAddress.getByName("0.0.0.0"); 
		InetAddress gatewayIP = InetAddress.getByName("0.0.0.0");

		byte[] chaddr = getMacAddress().getBytes();
		byte[] sname = new byte[64];
		byte[] file = new byte[128];
		DHCPOptions options = new DHCPOptions();
		options.addOption(53, MessageType.DHCPRELEASE.getValue()); 
		// Requested IP adress MUST NOT
		options.addOption(54, getServerID()); // Server identifier MUST
		options.addOption(255);
		
		return new DHCPMessage(op, htype, hlen, hops, transactionID, num_of_seconds, flags, clientIP, yourClientIP, serverIP, gatewayIP,
				chaddr, sname, file, options);

	}

	@Override
	DHCPMessage getReleaseAnswer(DHCPMessage msg) {
		return null;
	}	
	
	/* VARIABLES */

	DatagramSocket clientSocket;
	private InetAddress currentClientIP;
	private static Random rand = new Random();
	private int currentTransactionID;
	private byte[] serverID;
	private MACadress macAddress;
	private DHCPMessage previousSentMessage;
	
	
	/* GETTERS + SETTERS */
	
	public DatagramSocket getClientSocket() {
		return clientSocket;
	}

	public void setClientSocket(DatagramSocket clientSocket) {
		this.clientSocket = clientSocket;
	}
	
	public MACadress getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(MACadress macAddress) {
		this.macAddress = macAddress;
	}

	private long startLeaseTime;
	private long leaseTime;
	
	
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

	public DHCPMessage getPreviousSentMessage() {
		return previousSentMessage;
	}

	public void setPreviousSentMessage(DHCPMessage previousSentMessage) {
		this.previousSentMessage = previousSentMessage;
	}

	@Override
	void processDiscover(DHCPMessage msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	void processRequest(DHCPMessage msg) {
		// TODO Auto-generated method stub
		
	}

}