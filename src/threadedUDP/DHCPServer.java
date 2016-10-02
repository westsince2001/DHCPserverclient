package threadedUDP;

import java.io.*;
import java.net.*;

import DHCPEnum.Htype;
import DHCPEnum.Opcode;
import DHCPEnum.Options;

public class DHCPServer extends Node {
	
	/************************************ CONSTRUCTOR *******************************/

	public DHCPServer() throws UnknownHostException {
		setServerSocket(null);
		this.serverID = 253;
		
		getLeases().addNewIPList(config.getPool());
	}
	
	/*************************************** MAIN METHOD *******************************/
	
	public static void main(String[] args) throws IOException {
		try{
			DHCPServer server = new DHCPServer();
			server.startServer();
		} catch(UnknownHostException e){
			Utils.printError("Cannot make new server!");
			e.getStackTrace();
		}
	}
	
	/******************************* START SERVER METHOD *******************************/
	
	public void startServer() {
		System.out.println("###########################");
		System.out.println("#                         #");
		System.out.println("#   DHCP SERVER STARTED   #");
		System.out.println("#      Tuur Van Daele     #");
		System.out.println("#      Thomas Verelst     #");
		System.out.println("#                         #");
		System.out.println("###########################");
		System.out.println();
		System.out.println("///// Server is listening /////");
		
		// Create datagram socket
		try {
			setServerSocket(new DatagramSocket(getConfig().getPort()));
		} catch (Exception e) {
			Utils.printError("The datagram socket cannot be constructed!");
			e.printStackTrace();
		}

		try {
			// Listen for clients and serve them (in different threads).
			while (true) {
				// Receive data
				DatagramPacket receivePacket = receivePacket();
				
				// Serve client (in THREAD)
				serveClient(receivePacket);
			}
		}
		catch(IOException e){
			Utils.printError("The serversocket is being deleted.");
			e.printStackTrace();
			
		// Release resources after execution
		} finally {
			System.out.println("///// SERVER RELEASES RESOURCES ///// (server)");
			exit(serverSocket);
		}
	}
	
	/************************* HELPER METHODS FOR startServer() *******************************/

	// Server listens until receives packet
	
	/**
	 * Wait until packet is received and then return the message 
	 * 
	 * @return
	 * @throws IOException
	 */
	public DatagramPacket receivePacket() throws IOException{
		byte[] receiveData = new byte[576]; // DHCP packet maximum 576 bytes
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
//		
//		// print
//		System.out.println();
//		System.out.println("///// server is listening ///// (Server)");
		
		// Listening until receives packet
		serverSocket.receive(receivePacket);
		
		return receivePacket;
	}
	
	
	/**
	 * Closes socket when server is ended
	 * 
	 * @param serverSocket
	 */
	public void exit(DatagramSocket serverSocket) {
		if (serverSocket != null) {
			serverSocket.close();
		}
	}
	
	/**
	 * Serves client. This will create a new thread and ask the handler to process the DatagramPacket receivePacket
	 * 
	 * @param receivePacket
	 */
	public void serveClient(DatagramPacket receivePacket){
		if (receivePacket != null){
			Handler h = new Handler(this, serverSocket, receivePacket);
			Thread thread = new Thread(h);
			thread.start(); // run method run in handler
		}
	}
	
	/*************************$***************** TRANSACTIONS ****************************************/
	
	 /* Discovery */
	
	/**
	 * Returns a discovery message. Since the server should not send DISCOVER messages, it will return null;
	 */
	@Override
	DHCPMessage getDiscoverMsg() throws UnknownHostException {
		System.out.println("Server should send to DHCP DISCOVER!");
		return null;
	}
	
	/**
	 * Returns the answer on the incoming discovery message
	 * 
	 * @param DHCPMessage msg
	 * 				The discover message that is has been received
	 */
	@Override
	DHCPMessage getDiscoverAnswer(DHCPMessage msg) throws UnknownHostException {
		assert(msg.getType() == MessageType.DHCPDISCOVER);
		return getOfferMsg(msg);
	}
	
	/* Offer */
	
	/**
	 * Returns an offer message, generated as reply  to the given DHCPMessage (which should be a DISCOVER).
	 * 
	 * If no IP's are available, null will be returned, because no offer message should be send to the client
	 */
	@Override
	DHCPMessage getOfferMsg(DHCPMessage msg) throws UnknownHostException {
		assert(msg.getType() == MessageType.DHCPDISCOVER);
		
		InetAddress offeredIP = getLeases().getNextAvailableIP(); 
		if(offeredIP == null){
			System.out.println("# WARNING: no more available IP's!");
			return null;
		}	
		
		Opcode op = Opcode.BOOTREPLY;
		int transactionID = msg.getTransactionID();
		byte[] flags = BROADCAST_FLAG;
		InetAddress clientIP = InetAddress.getByName("0.0.0.0");
		InetAddress yourClientIP = offeredIP;
		InetAddress serverIP = getServerIP();
		
		MACaddress chaddr = msg.getChaddr();

		DHCPOptions options = new DHCPOptions();
		options.addOption(Options.MESSAGE_TYPE, MessageType.DHCPOFFER.getValue());
		options.addOption(Options.SERVER_ID, getServerID());
		options.addOption(Options.LEASE_TIME, Leases.LEASE_TIME);
		options.addOption(255);
		
		return new DHCPMessage(op, transactionID, flags, clientIP, yourClientIP, serverIP, chaddr, options);
	}

	/**
	 * Returns the answer to an offer message. Since the server should not answer offer messages, this function will return null
	 */
	@Override
	DHCPMessage getOfferAnswer(DHCPMessage msg) throws UnknownHostException {
		System.out.println("Server should not reply to DHCP OFFER!");
		return null;
	}
	
	/* Request */

	/**
	 * Returns a new IP request message. Since the server should not send DHCP request messages, it will return null.
	 */
	@Override
	DHCPMessage getNewIPRequestMsg(DHCPMessage msg) throws UnknownHostException {
		System.out.println("Server should not generate IP REQUEST message!");
		return null;
	}
	
	/**
	 * Returns an extend IP request message. Since the server should not send DHCP request messages, nothing should be done.
	 */
	@Override
	DHCPMessage extendLeaseRequestMessage()
			throws UnknownHostException {
		System.out.println("Server should not generate LEASE EXTEND REQUEST message!");
		return null;
	}

	/**
	 * Returns a reply to the given DHCPMessage (which should be a REQUEST).
	 * 
	 * If the received message is a new IP request message or a valid IP extend request, it will return an ACK message
	 * Otherwise, it will return a NAK message
	 */
	@Override
	DHCPMessage getRequestAnswer(DHCPMessage msg) throws UnknownHostException {
		assert(msg.getType() == MessageType.DHCPREQUEST);
		
		if(isValidIPrequest(msg)){
			InetAddress requestedIP = InetAddress.getByAddress(msg.getOption(Options.REQUESTED_IP));
			
			getLeases().leaseIP(requestedIP, msg.getChaddr());
			
			return getAckMsg(msg);
		}
		
		if(isValidIPextend(msg)){
			InetAddress requestedIP = msg.getClientIP();
			
			getLeases().extendLease(requestedIP, msg.getChaddr());
			
			return getAckMsg(msg);
		}
		
		// OTHERWISE
		return getNakMsg(msg);
	}	
		
	/**
	 * Checks if given REQUEST message is a valid new IP request message
	 * 
	 * @param msg
	 * @return
	 * @throws UnknownHostException
	 */
	public boolean isValidIPrequest(DHCPMessage msg) throws UnknownHostException{
		assert(msg.getType() == MessageType.DHCPREQUEST);
		// NEW IP LEASE
	
		// Check fields
		
		if(msg.getOpcode() != Opcode.BOOTREQUEST)
		 	return false;
		if(msg.htype != Htype.ETHERNET)
			return false;
		if(!msg.getYourClientIP().equals(InetAddress.getByName("0.0.0.0")))
			return false;
		if(!msg.getServerIP().equals( InetAddress.getByName("0.0.0.0")))
			return false;
		if(!msg.getGatewayIP().equals(InetAddress.getByName("0.0.0.0")))
			return false;
		if(!msg.getClientIP().equals(InetAddress.getByName("0.0.0.0"))) // For an IP request, the client IP must be empty!
			return false;
		
		
		// Check options
		
		if(!msg.getOptions().isSet(Options.REQUESTED_IP))
			return false;
		if(!msg.getOptions().isSet(Options.SERVER_ID)) // server identifier set
			return false;
		if(Utils.fromBytes(msg.getOption(Options.SERVER_ID)) != this.getServerID()) // server identifier is this server
			return false;
		
		// Check if IP is in pool
		InetAddress requestedIP = InetAddress.getByAddress(msg.getOption(Options.REQUESTED_IP));
		if(!getLeases().isInPool(requestedIP)){
			System.out.println("WARNING: requested IP "+ requestedIP.getHostAddress() +" not in pool!");
			return false;
		}
			
		
		// Check if IP not leased yet
		if(getLeases().isLeased(requestedIP)){
			System.out.println("WARNING: requested IP "+ requestedIP.getHostAddress() +" already leased!!");
			return false;
		}
		
		return true;
	}
	
	/**
	 * Checks if given REQUEST message is a valid IP extend request message
	 * 
	 * @param msg
	 * @return
	 * @throws UnknownHostException
	 */
	public boolean isValidIPextend(DHCPMessage msg) throws UnknownHostException{
		assert(msg.getType() == MessageType.DHCPREQUEST);
		// EXTEND IP LEASE
		
		// Check fields
		
		if(msg.getOpcode() != Opcode.BOOTREQUEST)
		 	return false;
		if(msg.htype != Htype.ETHERNET)
			return false;
		if(!msg.getYourClientIP().equals(InetAddress.getByName("0.0.0.0")))
			return false;
		if(!msg.getServerIP().equals(InetAddress.getByName("0.0.0.0")))
			return false;
		if(!msg.getGatewayIP().equals(InetAddress.getByName("0.0.0.0")))
			return false;
		if(msg.getClientIP().equals(InetAddress.getByName("0.0.0.0"))) // For an IP extend, the client IP must be set!
			return false;
		

		// Check options
		
		if(msg.getOptions().isSet(Options.REQUESTED_IP)) // requested ip may not be set
			return false;
		if(msg.getOptions().isSet(Options.SERVER_ID)) // server identifier may not be set
			return false;
		
		// Check if IP is in pool
		InetAddress requestedIP = msg.getClientIP();
		if(!getLeases().isInPool(requestedIP)){
			System.out.println("WARNING: requested IP "+ requestedIP.getHostAddress() +" not in pool!");
			return false;
		}
		
		// Check if IP is already leased to this client
		if(!getLeases().isLeasedBy(requestedIP, msg.getChaddr())){
			System.out.println("WARNING: requested IP "+ requestedIP.getHostAddress() +" not leased to this client!");
			return false;
		}
		
		return true;
	}

	/* ACKNOWLEDGE */
	
	/**
	 *  Returns an ACK message as reply to the given DHCPMessage
	 */
	@Override
	DHCPMessage getAckMsg(DHCPMessage msg) throws UnknownHostException {
		Opcode op = Opcode.BOOTREPLY;
		int transactionID = msg.getTransactionID();
		byte[] flags = UNICAST_FLAG;
		InetAddress clientIP = InetAddress.getByName("0.0.0.0");
		InetAddress yourClientIP = getLeases().getIPbyMAC(msg.getChaddr());
		assert(yourClientIP != null);
		
		InetAddress serverIP = getServerIP();
		
		MACaddress chaddr = msg.getChaddr();

		DHCPOptions options = new DHCPOptions();
		options.addOption(Options.MESSAGE_TYPE, MessageType.DHCPACK.getValue());
		options.addOption(Options.SERVER_ID, getServerID());
		options.addOption(Options.LEASE_TIME, 10);
		options.addOption(255);
		
		return new DHCPMessage(op, transactionID, flags, clientIP, yourClientIP, serverIP, chaddr, options);		
	}
	
	/**
	 * Returns a message which should answer an ACK. Since the server should not answer to ACK messages, null will be returned.
	 */
	@Override
	DHCPMessage getAckAnswer(DHCPMessage msg) {
		System.out.println("Server should not answer ACK message!");
		return null;
	}
	
	/* NOT ACKNOWLEDGE */
	
	/**
	 *  Returns an NAK message. as response to the given DHCPMessage
	 */
	@Override
	DHCPMessage getNakMsg(DHCPMessage msg) throws UnknownHostException {
		Opcode op = Opcode.BOOTREPLY;
		int transactionID = msg.getTransactionID();
		byte[] flags = UNICAST_FLAG;
		InetAddress clientIP = InetAddress.getByName("0.0.0.0");
		InetAddress yourClientIP = InetAddress.getByName("0.0.0.0");
		InetAddress serverIP = InetAddress.getByName("0.0.0.0");
		
		MACaddress chaddr = msg.getChaddr();

		DHCPOptions options = new DHCPOptions();
		options.addOption(Options.MESSAGE_TYPE, MessageType.DHCPNAK.getValue());
		options.addOption(Options.SERVER_ID, getServerID());
		options.addOption(255);
		
		return new DHCPMessage(op, transactionID, flags, clientIP, yourClientIP, serverIP, chaddr, options);		
	}

	/**
	 *  Returns the answer to a NAK message.
	 */
	@Override
	DHCPMessage getNakAnswer(DHCPMessage msg) {
		System.out.println("Server should not answer ACK message!");
		return null;
	}
	
	/* RELEASE */

	/**
	 * Returns a RELEASE message. Since the server should not send DHCP request messages, it will return null.
	 */
	@Override
	DHCPMessage getReleaseMsg() throws UnknownHostException {
		System.out.println("Server should not send RELEASE message!");
		return null;
	}
	
	/**
	 * Returns the answer to a RELEASE message. Since a release message should not be answered, it will return null.
	 */
	@Override
	DHCPMessage getReleaseAnswer(DHCPMessage msg) {		
		return null;
	}
	
	@Override
	void processRelease (DHCPMessage msg){
		getLeases().release(msg.getClientIP(), msg.getChaddr());
	}
	
	/************************************* GETTERS AND SETTERS ********************************************/
	
	/* VARIABLES */
	DatagramSocket serverSocket;
	final int serverID;	
	
	public DatagramSocket getServerSocket() {
		return serverSocket;
	}

	public void setServerSocket(DatagramSocket serverSocket) {
		this.serverSocket = serverSocket;
	}
	
	public int getServerID() {
		return serverID;
	}
	
	private InetAddress getServerIP() throws UnknownHostException {
		return InetAddress.getByName("localhost");
	}
	
	/* CONFIG */
	
	public Config getConfig() {
		return config;
	}

	final Config config = new Config();
	
	/* LEASING */
	
	Leases leases = new Leases();

	public Leases getLeases() {
		return leases;
	}

	public void setLeases(Leases leases) {
		this.leases = leases;
	}
	
}