package threadedUDP;

import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import DHCPEnum.Opcode;
import DHCPEnum.Options;
import Exceptions.UnknownMessageTypeException;

class DHCPClient extends Node implements Runnable{
	
	/************************************************************ CONSTRUCTOR **************************************************************************/
	
	/**
	 * CONSTRUCTOR
	 * Sets client socket to null, current client ip to null, current transaction ID to random int, 
	 * mac address to mac address of this computer, set server id to 0, lease time to 0, set previousSentMessage to 0,
	 * set the serveraddress to the given (typed) server address and set the serverport to the given (typed) serverport)
	 * 
	 * @throws IOException
	 */
	public DHCPClient(InetAddress serverAddress) throws IOException {
		setClientSocket(null);
		setCurrentClientIP(null);
		currentTransactionID = rand.nextInt((int) Math.pow(2, 32)); // Random transaction id
		macAddress = MACaddress.getMacAddressThisComputer();
		setServerID(0);
		setLeaseTime(0);
		setPreviousSentMessage(null);
		
		this.serverAddress = serverAddress;
		this.serverPort = getConfig().getPort();
	}

	
	/***************************************************************** MAIN METHOD **********************************************************************/

	/**
	 * Connect to server.
	 * 
	 * @param args
	 */
//	public static void main(String args[]){		
//		try {
//			DHCPClient client = new DHCPClient();
//			client.connectToServer();
//		} catch (IOException e) {
//			Utils.printError("No valid Server IP address or no valid port");
//		}
//	}
	
	@Override
	public void run() {
		connectToServer();
	}
	
	
	/************************************************************ CONNECT TO SERVER ***********************************************************************/
	
	/**
	 * First start lease (send discovery message). The current IP lease is extended for 15 seconds (renew if necessary). After this 15 seconds, the IP is released.
	 * Then the client tries to renew an illegal IP, therefore the client gets a NAK message.
	 * After a NAK, the client send a discovery messsage automatically to get a new valid IP address.
	 */
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
			//Timer timer = new Timer();
			//timer.schedule(new clientReleaseTimer(this), 15*1000); // IP will be released after 15 seconds (before this time, the current IP lease is extended)
			//timer.schedule(new clientInvalidRenewTimer(this), 50*1000); // Will try to extend invalid IP after 50 seconds, client will receive a NAK
			//after this a new IP will automatically be leased and extended (NAK --> send discovery message to get new IP)
			
			
			// Answer and process Incoming messages
			processAndAnswerIncomingMessages();
			
		} catch (Exception e) {
			Utils.printError("The resources are being released and the serversocket is being deleted.");
			e.printStackTrace();
			
		// Release resources and closing datagram socket after execution
		}finally{
			System.out.println("\n##### RELEASING RESOURCES AND CLOSING SOCKET #####");
			closeConnection();
		}
	}
	
	/************************************************************ Helper methods for connectToServer() **********************************************************/
	
	/**
	 * Close the connection: release resources if necessary and close the client socket.
	 * 
	 */
	private void closeConnection(){
		// Release resources if possible
		if (getCurrentClientIP() != null){
			processRelease(null);
			try{
				releaseLease();
			} catch(IOException e1){
				Utils.printError("IO Exception while closing connection!");
			};
		}
		
		// Close client socket
		if (getClientSocket() != null)
			getClientSocket().close();				
	}
	
	/**
	 * Process and answer (if necesarry) incoming messages.
	 * 
	 * @throws IOException
	 * @throws UnknownMessageTypeException
	 */
	public void processAndAnswerIncomingMessages() throws IOException, UnknownMessageTypeException{
		// Print
		System.out.println("\n##### CLIENT IS PROCESSING AND ANSWERING INCOMING MESSAGES #####");
		
		// Process and answer incoming messages until the received message not need to be answered anymore
		DHCPMessage answer;
		while(true){
			// Receive message (with same transaction ID). If no valid message received after 10 seconds --> resend previous message.
			DHCPMessage receiveMessage = receiveMessage();
			
			assert (receiveMessage != null);
			
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
	
	/**
	 * Send a discovery message
	 * 
	 * @throws IOException
	 */
	void startLease() throws IOException{
		// Create discovery message
		DHCPMessage msg = getDiscoverMsg();
		
		// Send discovery message
		sendMsg(msg);
		
		// Printing
		System.out.println("\n##### CLIENT HAS JUST SENT DISCOVERY MESSAGE #####");
		msg.print(); // print message content	
	}
	
	/** Renew the current IP (getCurrentClientIP()), so send a renew message.
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws UnknownMessageTypeException
	 */
	public void renewLease() throws IOException, InterruptedException, UnknownMessageTypeException {
		
		// If client has no IP now, no need to renew (happens after releasing)
		if (getCurrentClientIP() == null){
			return;
		}
		// print
		System.out.println("\n##### CLIENT RENEWS LEASE #####");

		// Extend lease
		DHCPMessage sendMsg = extendLeaseRequestMessage();
		sendMsg(sendMsg);

		// Print sending message
		System.out.println("o Client sends renew request");
		sendMsg.print();
	}
	
	/**
	 * Send a release message.
	 * 
	 * @throws IOException
	 */
	public void releaseLease() throws IOException{
		// Create release message
		DHCPMessage releaseMessage = getReleaseMsg(); 
		
		// Send release message
		sendMsg(releaseMessage);
		processRelease(null);
		
		// Print
		System.out.println("\n##### CLIENT HAS RELEASED LEASE #####");
		releaseMessage.print();
	}
	
	
	/************************************************* SEND MESSAGES AND RECEIVE MESSAGES **********************************************************************/
	
	/**
	 * Send the given message.
	 * Set "previous sent message" to the just sent message.
	 * 
	 * @param msg
	 * @throws IOException
	 */
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
	
	/**
	 * 
	 * Receive message with transaction ID the same as the transaction ID of this client.
	 * If no message received after 10 seconds and previous sent message is no release message --> sent message again and keep on listening. 
	 * 
	 * @return
	 * @throws IOException
	 * @throws UnknownMessageTypeException
	 */
	public DHCPMessage receiveMessage() throws IOException, UnknownMessageTypeException{
		DHCPMessage receivedMsg;
		byte[] receiveData = new byte[576]; // DHCP packet maximum 576 bytes
		DatagramPacket receivePacket = new DatagramPacket(receiveData,receiveData.length);
		do{	
			// Receive packet (nothing received after 10 seconds and last sent message is no release message --> last sent message is resent )
			receivePacket = receivePacket(receivePacket);
			
			assert(receivePacket != null);
			
			// Unpack received packet
			byte[] byteMsg = receivePacket.getData();
			receivedMsg = new DHCPMessage(byteMsg);

		} while (receivedMsg.getTransactionID() != getCurrentTransactionID()); // Transaction ID's != --> keep on listening
		
		return receivedMsg;
	}
	
	/**
	 * Receive a packet.
	 * If the client has received nothing for 10 seconds and the previous sent message is a release message, send the message again and keep on listening. 
	 * 
	 * @param receivePacket
	 * @return
	 * @throws IOException
	 */
	public DatagramPacket receivePacket(DatagramPacket receivePacket) throws IOException{
		
		// Listening for message for 10 seconds
		getClientSocket().setSoTimeout(10000); // socket throws exception after 10 seconds waiting for message.
		try{
			getClientSocket().receive(receivePacket);
			//System.out.println("received something");
			return receivePacket;
		} 
		
		// If after 10 seconds no response received && previous message is no release message --> resend message and listen again.
		catch(SocketTimeoutException e){
				if(getPreviousSentMessage().getType() != MessageType.DHCPRELEASE){
					System.out.println("o Client is resending previous message");
					getPreviousSentMessage().print();
					sendMsg(getPreviousSentMessage()); // Resend previous message
				}
				return receivePacket(receivePacket); // Listen again
		}
		

	}
	
	/********************************************************************** TRANSACTIONS ************************************************************************/

	// Discovery message	
	
	/**
	 * Create discovery message
	 * */
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
	/**
	 * Create discovery answer
	 */
	@Override
	public DHCPMessage getDiscoverAnswer(DHCPMessage msg) {
		System.out.println("Client received DHCP_DISCOVER but shouldn't process it.");
		return null;
	}
	
	/**
	 * Process discovery message
	 */
	@Override
	void processDiscover(DHCPMessage msg) {
		
	}

	// Offer
	
	/**
	 * Create offer message
	 * 
	 */
	@Override
	DHCPMessage getOfferMsg(DHCPMessage msg) throws UnknownHostException {
		System.out.println("Clients cannot send DHCP_OFFER.");
		return null;
	}
	
	/**
	 * Create offer message.
	 */
	@Override
	DHCPMessage getOfferAnswer(DHCPMessage msg) throws UnknownHostException {
		DHCPMessage answer = getNewIPRequestMsg(msg);
		return answer;
	}
	
	/**
	 * Process offer message
	 */
	@Override
	void processOffer(DHCPMessage msg){
		// do nothing
	}

	// Request
	
	/**
	 * Create request message for new IP.
	 */
	@Override
	DHCPMessage getNewIPRequestMsg(DHCPMessage msg) throws UnknownHostException {
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
	
	/**
	 * Create renew message.
	 */
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
	
	/**
	 * Create request answer.
	 */
	@Override
	DHCPMessage getRequestAnswer(DHCPMessage msg) {
		System.out.println("Client received DHCP_REQUEST but shouldn't process it.");
		return null;
	}
	
	/**
	 * Process incoming request message
	 */
	@Override
	void processRequest(DHCPMessage msg) {
		// do nothing
	}

	// Acknowledge

	/**
	 * Create acknowledge message
	 */
	@Override
	DHCPMessage getAckMsg(DHCPMessage msg) throws UnknownHostException {
		return null;
	}

	/**
	 * Create acknowledge answer
	 */
	@Override
	DHCPMessage getAckAnswer(DHCPMessage msg) {
		return null;
	}
	
	/**
	 * Process incoming acknowledge message
	 */
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
	
	/**
	 * Renew lease after number of seconds.
	 * 
	 * @param number_of_seconds
	 */
	private void renewLeaseAfter(long number_of_seconds){
		Timer timer = new Timer();
		timer.schedule(new clientRenewTimer(this), number_of_seconds*1000);
	}

	// Not Acknowledge
	
	/**
	 * Create not acknowledge message.
	 */
	@Override
	DHCPMessage getNakMsg(DHCPMessage msg) throws UnknownHostException {
		return null;
	}

	/**
	 * Create not acknowledge answer
	 */
	@Override
	DHCPMessage getNakAnswer(DHCPMessage msg) {
		return null;
	}
	
	/**
	 * Process not acknowledge message.
	 */
	@Override
	void processNak() throws IOException, UnknownMessageTypeException{
		// Print
		System.out.println("\n##### RECEIVED NAK. TRYING TO GET NEW IP. ######");
		
		// Try new IP address
		startLease();
		processAndAnswerIncomingMessages();
	}

	// Release
	
	/**
	 * Process incoming release message.
	 */
	@Override
	void processRelease(DHCPMessage message){
		// Delete client IP and server ID 
		setCurrentClientIP(null);
		setServerID(0);
		
		// Reset seconds elapsed since ack and lease time
		setLeaseTime(0);
	}

	/**
	 * Create release message.
	 */
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

	/**
	 * Create release answer.
	 */
	@Override
	DHCPMessage getReleaseAnswer(DHCPMessage msg) {
		return null;
	}	
	
	/********************************************************************** VARIABLES **************************************************************************/

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

	/********************************************************************* GETTERS + SETTERS ******************************************************************/

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
		//return (getLeaseTime()/2);
		return 5; // hardcoded because server lease time is 86400
		
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
	
	/*********************************************************************** CONFIG ********************************************************************/
	
	final Config config = new Config();

	public Config getConfig() {
		return config;
	}
}

/**
 * Using timer schedule to renew the IP.
 *
 */
class clientRenewTimer extends TimerTask {
	private DHCPClient client;
	
    public clientRenewTimer(DHCPClient client) {
    	this.client = client;
	}

    @Override
	public void run() {
    	try {
    		client.renewLease();
		} catch (Exception e) {
			Utils.printError("Exception during lease renewing!");
			e.printStackTrace();
		}
    }
}


/************************************************************** EVENTS to demonstrate client working **************************************************************/

/**
 * Using timer scheduler to release.
 *
 */
class clientReleaseTimer extends TimerTask {
	private DHCPClient client;
	
    public clientReleaseTimer(DHCPClient client) {
    	this.client = client;
	}

    @Override
	public void run() {
    	try {
			client.releaseLease();
		} catch (Exception e) {
			Utils.printError("Exception during lease releasing!");
			e.printStackTrace();
		}
    }
}

/**
 * Using timer scheduler to renew for an invalid IP.
 *
 */
class clientInvalidRenewTimer extends TimerTask {
	private DHCPClient client;
	
    public clientInvalidRenewTimer(DHCPClient client) {
    	this.client = client;
	}

    @Override
	public void run() {
    	try {
    		System.out.println("\n##### CLIENT IS ILLEGALLY RENEWING AN IP (should receive NAK!)#####");
    		
    		// Renew an invalid lease
    		client.setCurrentClientIP(InetAddress.getByName("255.1.1.1")); // Set invalid IP
    		client.renewLease();
    		
		} catch (Exception e) {
			Utils.printError("Exception during lease renewing!");
			e.printStackTrace();
		}
    }
}