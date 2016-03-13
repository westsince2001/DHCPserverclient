package threadedUDP;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;

import DHCPEnum.Hlen;
import DHCPEnum.Htype;
import DHCPEnum.Opcode;
import DHCPEnum.Options;

public class UdpServer extends Node {
	
	/* CONSTRUCTOR */
	
	public UdpServer() throws UnknownHostException {
		setServerSocket(null);
		
		this.serverID = 456; // TODO eventueel random
		
		getLeases().addNewIP(InetAddress.getByName("1.1.1.1"));
		getLeases().addNewIP(InetAddress.getByName("2.2.2.2"));
	}
	
	/* MAIN METHOD */
	
	public static void main(String[] args) {
		try{
			UdpServer server = new UdpServer();
			server.startServer();
		} catch(UnknownHostException e){
			System.out.println("Error: cannot make new server");
			e.getStackTrace();
		}
	}

	/* START SERVER */
	
	public void startServer() {
		System.out.println("###########################");
		System.out.println("#                         #");
		System.out.println("#   DHCP SERVER STARTED   #");
		System.out.println("#      Tuur Van Daele     #");
		System.out.println("#      Thomas Verelst     #");
		System.out.println("#                         #");
		System.out.println("###########################");
		System.out.println();
		
		// Create datagram socket
		try {
			setServerSocket(new DatagramSocket(getPort()));
		} catch (Exception e) {
			System.out.println("Error! The datagram socket cannot be constructed!");
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
			System.out.println("Error! The serversocket is being deleted.");
			e.printStackTrace();
			
		// Release resources after execution
		} finally {
			System.out.println("///// SERVER RELEASES RESOURCES ///// (server)");
			exit(serverSocket);
		}
	}

	/* HELPER METHODS FOR startServer() */
	
	// Server listens until receives packet
	public DatagramPacket receivePacket() throws IOException{
		byte[] receiveData = new byte[576]; // DHCP packet maximum 576 bytes
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		
		// print
		System.out.println();
		System.out.println("///// server is listening ///// (Server)");
		
		// Listening until receives packet
		serverSocket.receive(receivePacket);
		
		// print
		System.out.println();
		System.out.println("///// server creates thread ///// (Server)");
		
		return receivePacket;
	}
	
	// Close socket
	public void exit(DatagramSocket serverSocket) {
		if (serverSocket != null) {
			serverSocket.close();
		}
	}
	
	// Serve client (in thread)
	public void serveClient(DatagramPacket receivePacket){
		if (receivePacket != null){
			Handler h = new Handler(this, serverSocket, receivePacket);
			Thread thread = new Thread(h);
			thread.start(); // run method run in handler
		}
	}
	
	/* READ FROM TXT FILE */

	private static int getPort() throws IOException {
		return 1234;
		//		Properties pro = new Properties();
//		FileInputStream in = new FileInputStream("src/udpconfig.txt");
//		pro.load(in);
//		String port = pro.getProperty("port");
//		int result = Integer.parseInt(port);
//		return result;
	}


	/* TRANSACTIONS */
	
	// Discovery
	
	@Override
	DHCPMessage getDiscoverMsg() throws UnknownHostException {
		System.out.println("Server should send to DHCP DISCOVER!");
		return null;
	}

	@Override
	DHCPMessage getDiscoverAnswer(DHCPMessage msg) throws UnknownHostException {
		return getOfferMsg(msg);
	}
	
	@Override
	void processDiscover(DHCPMessage msg) {
		
	}
	
	// Offer
	
	@Override
	DHCPMessage getOfferMsg(DHCPMessage msg) throws UnknownHostException {

		InetAddress offeredIP = getNextAvailableIP(); 
		System.out.println("- Offered IP: "+ offeredIP);		
		
		Opcode op = Opcode.BOOTREPLY;
		int transactionID = msg.getTransactionID();
		byte[] flags = BROADCAST_FLAG;
		InetAddress clientIP = InetAddress.getByName("0.0.0.0");
		InetAddress yourClientIP = offeredIP;
		InetAddress serverIP = getServerIP();
		
		byte[] chaddr = msg.getChaddr();

		DHCPOptions options = new DHCPOptions();
		options.addOption(Options.MESSAGE_TYPE, MessageType.DHCPOFFER.getValue());
		options.addOption(Options.SERVER_ID, getServerID());
		options.addOption(Options.LEASE_TIME, 10);
		options.addOption(255);
		
		return new DHCPMessage(op, transactionID, flags, clientIP, yourClientIP, serverIP, chaddr, options);
	}

	@Override
	DHCPMessage getOfferAnswer(DHCPMessage msg) throws UnknownHostException {
		System.out.println("Server should not reply to DHCP OFFER!");
		return null;
	}
	
	@Override
	void processOffer(DHCPMessage msg){
		//TODO pas pool aan
		// Als argument krijgt deze methode de OFFER message dat de server nu gaat sturen (zie handler)		
	}
	
	// Request

	@Override
	DHCPMessage getNewIPRequestMsg(DHCPMessage msg) throws UnknownHostException {
		System.out.println("Server should not generate IP REQUEST message!");
		return null;
	}
	
	@Override
	DHCPMessage extendLeaseRequestMessage()
			throws UnknownHostException {
		System.out.println("Server should not generate LEASE EXTEND REQUEST message!");
		return null;
	}

	@Override
	DHCPMessage getRequestAnswer(DHCPMessage msg) throws UnknownHostException {
		
		if(isValidIPrequest(msg)){
			InetAddress requestedIP = InetAddress.getByAddress(msg.getOption(Options.REQUESTED_IP));
			
			System.out.println("Leased IP"+requestedIP);
			System.out.println(Utils.toHexString(msg.getChaddr()));
			
			getLeases().leaseIP(InetAddress.getByAddress(msg.getOptions().getOption(Options.REQUESTED_IP)), msg.getChaddr());
			
			return getAckMsg(msg);
		}
		
		if(isValidIPextend(msg)){
			InetAddress requestedIP = msg.getClientIP();
			
			System.out.println("Renewed IP"+requestedIP);
			System.out.println(Utils.toHexString(msg.getChaddr()));
			
		//	getLeases().put(InetAddress.getByAddress(msg.getOptions().getOption(Options.REQUESTED_IP)), new Leaser(msg.getChaddr(), 0, 0));
		//	System.out.println(leasesToString());
		//	printLeases();
			
			return getAckMsg(msg);
		}
		
		// OTHERWISE
		return getNakMsg(msg);
	}
		
public boolean isValidIPrequest(DHCPMessage msg) throws UnknownHostException{
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
		
		if(!msg.getOptions().isSet(Options.REQUESTED_IP)) // requested ip set TODO check of gelijk aan offered adress!
			return false;
		if(!msg.getOptions().isSet(Options.SERVER_ID)) // server identifier set
			return false;
		if(Utils.fromBytes(msg.getOption(Options.SERVER_ID)) != this.getServerID()) // server identifier is this server
			return false;
		
		return true;
	}
	
	public boolean isValidIPextend(DHCPMessage msg) throws UnknownHostException{
		// EXTEND IP LEASE
		
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
		if(msg.getClientIP().equals(InetAddress.getByName("0.0.0.0"))) // For an IP extend, the client IP must be set!
			return false;
		
		é
		// Check options
		
		if(msg.getOptions().isSet(Options.REQUESTED_IP)) // requested ip may not be set
			return false;
		if(msg.getOptions().isSet(Options.SERVER_ID)) // server identifier may not be set
			return false;
		
		return true;
	}



	@Override
	void processRequest(DHCPMessage msg) {
		// TODO Auto-generated method stub
		
	}

	
	// Acknowledge

	@Override
	DHCPMessage getAckMsg(DHCPMessage msg) throws UnknownHostException {
		Opcode op = Opcode.BOOTREPLY;
		Htype htype = Htype.ETHERNET;
		Hlen hlen = Hlen.INTERNET;
		byte hops = 0; 
		int transactionID = msg.getTransactionID();
		short num_of_seconds = 0; 
		byte[] flags = UNICAST_FLAG;
		InetAddress clientIP = InetAddress.getByName("0.0.0.0");
		InetAddress yourClientIP = InetAddress.getByName("99.99.99.99");
		InetAddress serverIP = getServerIP();
		InetAddress gatewayIP = InetAddress.getByName("0.0.0.0");
		
		byte[] chaddr = msg.getChaddr();
		byte[] sname = new byte[64]; 
		byte[] file = new byte[128];

		DHCPOptions options = new DHCPOptions();
		options.addOption(53, MessageType.DHCPACK.getValue());
		options.addOption(54, getServerID());
		options.addOption(51, 10);
		options.addOption(255);
		
		return new DHCPMessage(op, htype, hlen, hops, transactionID, num_of_seconds, flags, clientIP, yourClientIP, serverIP, gatewayIP, chaddr, sname, file, options);		
	}
	
	@Override
	DHCPMessage getAckAnswer(DHCPMessage msg) {
		System.out.println("Server should not answer ACK message!");
		return null;
	}
	
	@Override
	void processAck(DHCPMessage msg) {
		// TODO Aanpassen in pool
		// Als argument krijgt ACK Message dat nu gaat zenden!!
		
	}

	// Not acknowledge
	
	@Override
	DHCPMessage getNakMsg(DHCPMessage msg) throws UnknownHostException {
		// TODO fields voor NAK!!!! Best maandag doen!
		
		Opcode op = Opcode.BOOTREPLY;
		Htype htype = Htype.ETHERNET;
		Hlen hlen = Hlen.INTERNET;
		byte hops = 0; 
		int transactionID = msg.getTransactionID();
		short num_of_seconds = 0; 
		byte[] flags = UNICAST_FLAG;
		InetAddress clientIP = InetAddress.getByName("0.0.0.0");
		InetAddress yourClientIP = InetAddress.getByName("99.99.99.99");
		InetAddress serverIP = getServerIP();
		InetAddress gatewayIP = InetAddress.getByName("0.0.0.0");
		
		byte[] chaddr = msg.getChaddr();
		byte[] sname = new byte[64]; 
		byte[] file = new byte[128];

		DHCPOptions options = new DHCPOptions();
		options.addOption(53, MessageType.DHCPNAK.getValue());
		options.addOption(54, getServerID());
		options.addOption(51, 10);
		options.addOption(255);
		
		return new DHCPMessage(op, htype, hlen, hops, transactionID, num_of_seconds, flags, clientIP, yourClientIP, serverIP, gatewayIP, chaddr, sname, file, options);		
	}

	@Override
	DHCPMessage getNakAnswer(DHCPMessage msg) {
		System.out.println("Server should not answer ACK message!");
		return null;
	}
	
	@Override
	void processNak(){
		// do nothing
	}
	
	// RELEASE

	@Override
	DHCPMessage getReleaseMsg() throws UnknownHostException {
		System.out.println("Server should not send RELEASE message!");
		return null;
	}

	@Override
	DHCPMessage getReleaseAnswer(DHCPMessage msg) {		
		return null;
	}
	
	@Override
	void processRelease (DHCPMessage msg){
		// TODO: verwijderen uit pool
		// Argument: message dat ONTVANGT van client
	}

	
	/* GETTERS AND SETTERS */
	
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
	
	/* LEASING */
	
	Leases leases = new Leases();
	
	/* VARIABLES */

	public Leases getLeases() {
		return leases;
	}

	public void setLeases(Leases leases) {
		this.leases = leases;
	}

	DatagramSocket serverSocket;
	final int serverID;	
	
}