package threadedUDP;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;

import DHCPEnum.Hlen;
import DHCPEnum.Htype;
import DHCPEnum.Opcode;

public class UdpServer extends Node {
	
	/* CONSTRUCTOR */
	
	public UdpServer() throws UnknownHostException {
		setServerSocket(null);
		this.serverID = 456;
		this.leasedIP = new HashMap<InetAddress, byte[]>();
		this.pool = new InetAddress[]{InetAddress.getByName("1.1.1.1")};
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
	
	// Offer
	
	@Override
	DHCPMessage getOfferMsg(DHCPMessage msg) throws UnknownHostException {
		Opcode op = Opcode.BOOTREPLY;
		int transactionID = msg.getTransactionID();
		byte[] flags = BROADCAST_FLAG;
		InetAddress clientIP = InetAddress.getByName("0.0.0.0");
		InetAddress yourClientIP = InetAddress.getByName("99.99.99.99");
		InetAddress serverIP = InetAddress.getByName("99.99.99.98");
		
		byte[] chaddr = msg.getChaddr();

		DHCPOptions options = new DHCPOptions();
		options.addOption(OptionsEnum.MessageType, MessageType.DHCPOFFER.getValue());
		options.addOption(OptionsEnum.ServerID, getServerID());
		options.addOption(OptionsEnum.LeaseTime, 10);
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
		
		// Fields not correct --> NAK
		if(msg.getOpcode() != Opcode.BOOTREQUEST)
		 	return getNakMsg(msg);
		if(msg.htype != Htype.ETHERNET)
			return getNakMsg(msg);
		if(!msg.getYourClientIP().equals(InetAddress.getByName("0.0.0.0")))
			return getNakMsg(msg);
		if(!msg.getServerIP().equals( InetAddress.getByName("0.0.0.0")))
			return getNakMsg(msg);
		if(!msg.getGatewayIP().equals(InetAddress.getByName("0.0.0.0")))
			return getNakMsg(msg);
		
		// NEW IP LEASE
		if(msg.getOptions().getOption(50) != null // Requested IP TODO check of gelijk aan offered adress!!!!!!!
			&& msg.getOptions().getOption(54) != null // server identifier set
			&& Utils.fromBytes(msg.getOptions().getOption(54)) == this.getServerID() // server identifier same as this server ID
			&& msg.getClientIP().equals(InetAddress.getByName("0.0.0.0")) // Client IP adress set
				){
			return getAckMsg(msg);
		}
			
		// EXTEND IP LEASE
		/// TODO Nakijken of client nu dit IP address en nog niet vervallen
		if(msg.getOptions().getOption(50) == null  // Requested IP not set
			&& msg.getOptions().getOption(54) == null // server identifier not set
			&& !msg.getClientIP().equals(InetAddress.getByName("0.0.0.0")) // Client IP adress set
				){
			return getAckMsg(msg);
		}
		
		// OTHERWISE
		return getNakMsg(msg);
	}
	
	// Acknowledge

	@Override
	DHCPMessage getAckMsg(DHCPMessage msg) throws UnknownHostException {
		System.out.println("- Generating Ack");
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
		System.out.println("-Generating Nak. Not implemented yet, will return NULL");
		return null;
	}

	@Override
	DHCPMessage getNakAnswer(DHCPMessage msg) {
		System.out.println("Server should not answer ACK message!");
		return null;
	}
	
	// RELEASE

	@Override
	DHCPMessage getReleaseMsg() throws UnknownHostException {
		System.out.println("Server should not send RELEASE message!");
		return null;
	}

	@Override
	DHCPMessage getReleaseAnswer(DHCPMessage msg) {
		System.out.println("Server got release from client");		
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
	
	public InetAddress[] getPool() {
		return pool;
	}


	public HashMap<InetAddress, byte[]> getLeasedIP() {
		return leasedIP;
	}

	
	public int getServerID() {
		return serverID;
	}
	
	private InetAddress getServerIP() throws UnknownHostException {
		return InetAddress.getByName("localhost");
	}
	
	
	/* VARIABLES */

	DatagramSocket serverSocket;

	final HashMap<InetAddress, byte[]> leasedIP; // HashMap houdt bij welke InetAdresses al uitgeleend zijn en aan welke client, wel efficient om te moeten zoeken welke client welk ip adres heeft... 
	
	final int serverID;
	
	final InetAddress[] pool; // Tijdelijk om te testen, later wordt het ingeladen via een file

}