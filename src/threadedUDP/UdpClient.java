package threadedUDP;

import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import DHCPEnum.Opcode;
import DHCPEnum.Options;
import Exceptions.UnknownMessageTypeException;

class UDPClient extends Node {
	
	/* CONSTRUCTOR */
	
	public UDPClient() throws IOException {
		setClientSocket(null);
		setCurrentClientIP(null);
		currentTransactionID = rand.nextInt((int) Math.pow(2, 32)); // Random transaction id
		macAddress = MACaddress.getMacAddressThisComputer();
		setServerID(0);
		setLeaseTime(0);
		setPreviousSentMessage(null);
		
		
		// Read from command line
		byte[] readData = new byte[1024];
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		
		System.out.println("Server IP adress (10.33.14.246 for CN server):");
		readData =  inFromUser.readLine().getBytes();
		serverAddress = InetAddress.getByName(new String(readData));		
		
		System.out.println("port (1234 for CN server):");
		readData = inFromUser.readLine().getBytes();
		serverPort = Integer.parseInt(new String(readData));
	}

	
	/* MAIN METHOD */

	public static void main(String args[]){		
		try {
			UDPClient client = new UDPClient();
			client.connectToServer();
		} catch (IOException e) {
			Utils.printError("No valid Server IP address or no valid port");
		}
		
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
			Utils.printError("The datagram socket cannot be constructed!");
			e1.printStackTrace();
		}
		
		try {
			// Send discovery message
			startLease();
			
			// Some preprogrammed events to demonstrate client workings
			Timer timer = new Timer();
			timer.schedule(new clientReleaseTimer(this), 25*1000); // IP will be released after 25 seconds (during this time, the current IP lease is extended)
			timer.schedule(new clientInvalidRenewTimer(this), 30*1000); // Will try to extend invalid IP after 30 seconds, client will receive a NAK
			//after this a new IP will automatically be leased and extended
			
			
			// Answer and process Incoming messages
			processAndAnswerIncomingMessages();
			
		} catch (Exception e) {
			Utils.printError("The resources are being released and the serversocket is being deleted.");
			e.printStackTrace();
			
		// Release resources and closing datagram socket after execution
		}finally{
			System.out.println();
			System.out.println("##### RELEASING RESOURCES AND CLOSING SOCKET #####");
			closeConnection();
		}
	}
	
	/* Helper methods for connectToServer() */

	public void processAndAnswerIncomingMessages() throws IOException, UnknownMessageTypeException{
		// Print
		System.out.println();
		System.out.println("##### CLIENT IS PROCESSING AND ANSWERING INCOMING MESSAGES #####");
		
		// Process and answer incoming messages until the received message not need to be answered anymore
		DHCPMessage answer;
		while(true){
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
		}
	}
	
	void startLease() throws IOException{
		// Create discovery message
		DHCPMessage msg = getDiscoverMsg();
		
		// Send discovery message
		sendMsg(msg);
		
		// Printing
		System.out.println();
		System.out.println("##### CLIENT HAS JUST SENT DISCOVERY MESSAGE #####");
		msg.print(); // print message content	
	}
	
	// Renew the current IP (getCurrentClientIP())
	public void renewLease() throws IOException, InterruptedException, UnknownMessageTypeException {
		if (getCurrentClientIP() == null) // If client has no IP now, no need to
											// renew (happens after releasing)
			return;

		System.out.println("##### CLIENT RENEWS LEASE #####");
		DHCPMessage receivedMessage;

		// Extend lease
		DHCPMessage sendMsg = extendLeaseRequestMessage();
		sendMsg(sendMsg);

		// Print sending message
		System.out.println("o Client sends renew request");
		sendMsg.print();

		// Receive message (with same transaction ID). If no valid message
		// received after 10 seconds --> resend previous message.
		receivedMessage = receiveMessage();

		// Print answer
		System.out.println("o Client receives " + receivedMessage.getType());
		receivedMessage.print();

		// Process answer
		receivedMessage.getType().process(receivedMessage, this);
	}

	public void releaseLease() throws IOException{
		// Create release message
		DHCPMessage releaseMessage = getReleaseMsg(); 
		
		// Send release message
		sendMsg(releaseMessage);
		processRelease(null);
		
		// Print
		System.out.println();
		System.out.println("##### CLIENT HAS RELEASED LEASE #####");
		releaseMessage.print();
	}
	
	private void closeConnection(){
		// Release resources if possible
		if (getCurrentClientIP() != null){
			processRelease(null);
			try{
				releaseLease();
			} catch(Exception e2){};
		}
		
		// Close client socket
		if (getClientSocket() != null)
			getClientSocket().close();				
	}
	
	
	/* SEND MESSAGES AND RECEIVE MESSAGES */
	
	private void sendMsg(DHCPMessage msg) throws IOException {
		// Message to bytes
		byte[] sendData = msg.encode();
		
		// Make sending packet
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress,
				serverPort);
		
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
			//System.out.println("received something");
		} 
		
		// If after 10 seconds no response received--> resend message and listen again. In case client last message was release, do nothing
		catch(SocketTimeoutException e){
			if(getPreviousSentMessage().getType() != MessageType.DHCPRELEASE){
				System.out.println("o Client is resending previous message");
				getPreviousSentMessage().print();
				sendMsg(getPreviousSentMessage()); // Resend previous message
				return receivePacket(receivePacket); // Listen again
			}
			
		}
		
		return receivePacket;
	}
	
	/* TRANSACTIONS */

	// Discovery message	
	@Override
	DHCPMessage getDiscoverMsg() throws UnknownHostException {
		Opcode op = Opcode.BOOTREQUEST;
		int transactionID = getCurrentTransactionID();
		byte[] flags = BROADCAST_FLAG;
		InetAddress clientIP = InetAddress.getByName("0.0.0.0");
		InetAddress yourClientIP = InetAddress.getByName("0.0.0.0");
		InetAddress serverIP = InetAddress.getByName("0.0.0.0");
		
		MACaddress chaddr = getMacAddress();

		DHCPOptions options = new DHCPOptions();
		options.addOption(53, MessageType.DHCPDISCOVER.getValue(), 1);
		options.addOption(255);
		
		DHCPMessage answer = new DHCPMessage(op, transactionID, flags, clientIP, yourClientIP, serverIP, chaddr, options);

		return answer;
	}

	@Override
	public DHCPMessage getDiscoverAnswer(DHCPMessage msg) {
		System.out.println("Client received DHCP_DISCOVER but shouldn't process it.");
		return null;
	}
	
	@Override
	void processDiscover(DHCPMessage msg) {
		
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
		int transactionID = getCurrentTransactionID();
		byte[] flags = BROADCAST_FLAG;
		
		InetAddress clientIP = InetAddress.getByName("0.0.0.0");
		InetAddress yourClientIP = InetAddress.getByName("0.0.0.0");
		InetAddress serverIP = InetAddress.getByName("0.0.0.0");
		MACaddress chaddr = getMacAddress();

		DHCPOptions options = new DHCPOptions();
		options.addOption(Options.MESSAGE_TYPE, MessageType.DHCPREQUEST.getValue());
		options.addOption(Options.REQUESTED_IP, msg.getYourClientIP().getAddress()); // requested IP address  MUST
		options.addOption(Options.SERVER_ID, msg.getOptions().getOption(54)); // Server identifier MUST
		options.addOption(Options.END, null);
		
		return new DHCPMessage(op, transactionID, flags, clientIP, yourClientIP, serverIP, chaddr, options);
	}
	
	@Override
	DHCPMessage extendLeaseRequestMessage() throws UnknownHostException {
		Opcode op = Opcode.BOOTREQUEST;
		int transactionID = getCurrentTransactionID();
		byte[] flags = UNICAST_FLAG;
		InetAddress clientIP = getCurrentClientIP();
		assert(clientIP != null);
		InetAddress yourClientIP = InetAddress.getByName("0.0.0.0");
		InetAddress serverIP = InetAddress.getByName("0.0.0.0");
		
		MACaddress chaddr = getMacAddress();	
		
		DHCPOptions options = new DHCPOptions();
		options.addOption(53, MessageType.DHCPREQUEST.getValue(), 1);
		options.addOption(255);
		
		return new DHCPMessage(op, transactionID, flags, clientIP, yourClientIP, serverIP, chaddr, options);

	}

	@Override
	DHCPMessage getRequestAnswer(DHCPMessage msg) {
		System.out.println("Client received DHCP_REQUEST but shouldn't process it.");
		return null;
	}
	
	@Override
	void processRequest(DHCPMessage msg) {
		// do nothing
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
		assert(msg.getYourClientIP() != null);
		setCurrentClientIP(msg.getYourClientIP());
		assert(msg.getOptions().isSet(Options.SERVER_ID));
		setServerID(Utils.fromBytes(msg.getOptions().getOption(Options.SERVER_ID)));
		
		// Reset start time ack
		setLeaseTime(Utils.fromBytes(msg.getOptions().getOption(Options.LEASE_TIME)));
		renewLeaseAfter(getRenewTime());
	}
	
	private void renewLeaseAfter(long number_of_seconds){
		Timer timer = new Timer();
		timer.schedule(new clientRenewTimer(this), number_of_seconds*1000);
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
		startLease();
		processAndAnswerIncomingMessages();
	}

	// Release
	
	@Override
	void processRelease(DHCPMessage message){
		// Delete client IP and server ID 
		setCurrentClientIP(null);
		setServerID(0);
		
		// Reset seconds elapsed since ack and lease time
		setLeaseTime(0);
	}

	@Override
	DHCPMessage getReleaseMsg() throws UnknownHostException {		
		Opcode op = Opcode.BOOTREQUEST;
		int transactionID = getCurrentTransactionID();
		byte[] flags = UNICAST_FLAG;
;
		InetAddress clientIP = getCurrentClientIP();
		InetAddress yourClientIP = InetAddress.getByName("0.0.0.0");
		InetAddress serverIP = InetAddress.getByName("0.0.0.0"); 

		MACaddress chaddr = getMacAddress();
		DHCPOptions options = new DHCPOptions();
		options.addOption(Options.MESSAGE_TYPE, MessageType.DHCPRELEASE.getValue()); 
		// Requested IP adress MUST NOT
		options.addOption(Options.SERVER_ID, getServerID()); // Server identifier MUST
		options.addOption(255);
		
		return new DHCPMessage(op, transactionID, flags, clientIP, yourClientIP, serverIP, chaddr, options);

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
	private int serverID;
	private MACaddress macAddress;
	private DHCPMessage previousSentMessage;
	final InetAddress serverAddress;
	final int serverPort;
	private long leaseTime;

	/* GETTERS + SETTERS */

	public DatagramSocket getClientSocket() {
		return clientSocket;
	}

	public void setClientSocket(DatagramSocket clientSocket) {
		this.clientSocket = clientSocket;
	}
	
	public MACaddress getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(MACaddress macAddress) {
		this.macAddress = macAddress;
	}
	
	public long getRenewTime(){
		try {
			if (serverAddress.equals(InetAddress.getByName("10.33.14.246"))){ // dummy because server lease time is 86400 seconds
				return (long) (getLeaseTime()* 0.00005787);
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return (getLeaseTime()/2);
	}
		
	public long getLeaseTime() { // Return the lease time in SECONDS.
		return leaseTime;
	}

	public void setLeaseTime(long leaseTime) {
		this.leaseTime = leaseTime;
	}

	public int getServerID() {
		return serverID;
	}

	public void setServerID(int serverID) {
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
	
	/* CONFIG */
	
	final Config config = new Config();

	public Config getConfig() {
		return config;
	}
}

class clientRenewTimer extends TimerTask {
	private UDPClient client;
	
    public clientRenewTimer(UDPClient client) {
    	this.client = client;
	}

    @Override
	public void run() {
    	try {
    		client.renewLease();
		} catch (Exception e) {
			//Utils.printError("Exception during lease renewing!");
			//e.printStackTrace();
		}
    }
}


/* EVENTS to demonstrate client working */

class clientInvalidRenewTimer extends TimerTask {
	private UDPClient client;
	
    public clientInvalidRenewTimer(UDPClient client) {
    	this.client = client;
	}

    @Override
	public void run() {
    	try {
    		System.out.println("\n ##### CLIENT IS ILLEGALLY RENEWING AN IP (should receive NAK!)#####");
    		
    		// Renew an invalid lease
    		client.setCurrentClientIP(InetAddress.getByName("255.1.1.1")); // Set invalid IP
    		client.renewLease();
    		
		} catch (Exception e) {
			//Utils.printError("Exception during lease renewing!");
			//e.printStackTrace();
		}
    }
}

class clientReleaseTimer extends TimerTask {
	private UDPClient client;
	
    public clientReleaseTimer(UDPClient client) {
    	this.client = client;
	}

    @Override
	public void run() {
    	try {
			client.releaseLease();
		} catch (Exception e) {
			//Utils.printError("Exception during lease releasing!");
			//e.printStackTrace();
		}
    }
}