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
	DatagramSocket serverSocket;
	InetAddress[] pool = {InetAddress.getByName("1.1.1.1")}; // Tijdelijk om te testen, later wordt het ingeladen via een file
	HashMap<InetAddress, byte[]> leasedIP; // HashMap houdt bij welke InetAdresses al uitgeleend zijn en aan welke client, wel efficient om te moeten zoeken welke client welk ip adres heeft... 
	
	int serverID;
	
	public int getServerID() {
		return serverID;
	}

	public void setServerID(int serverID) {
		this.serverID = serverID;
	}
	
	

	public static void main(String[] args) throws Exception {
		
		UdpServer server = new UdpServer();
		server.startServer();
	}

	public UdpServer() throws Exception {
		serverSocket = new DatagramSocket(getPort());
		setServerID(456);
	}

	public void startServer() {

		try {
			System.out.println("###########################");
			System.out.println("#                         #");
			System.out.println("#   DHCP SERVER STARTED   #");
			System.out.println("#      Tuur Van Daele     #");
			System.out.println("#      Thomas Verelst     #");
			System.out.println("#                         #");
			System.out.println("###########################");
			System.out.println();

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
		} finally {
			// Release resources
			System.out.println("##### CLOSING CONNECTION #####");
			exit(serverSocket);
		}
	}
	
	public DatagramSocket getServerSocket() {
		return serverSocket;
	}

	public void setServerSocket(DatagramSocket serverSocket) {
		this.serverSocket = serverSocket;
	}
	
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
	
	// Serve client (in thread)
	public void serveClient(DatagramPacket receivePacket){
		if (receivePacket != null){
			Handler h = new Handler(this, serverSocket, receivePacket);
			Thread thread = new Thread(h);
			thread.start(); // run method run in handler
		}
	}

	private static int getPort() throws IOException {
		return 1234;
		//		Properties pro = new Properties();
//		FileInputStream in = new FileInputStream("src/udpconfig.txt");
//		pro.load(in);
//		String port = pro.getProperty("port");
//		int result = Integer.parseInt(port);
//		return result;
	}

	public void exit(DatagramSocket serverSocket) {
		if (serverSocket != null) {
			serverSocket.close();
		}
	}

	@Override
	DHCPMessage getDiscoverMsg() throws UnknownHostException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	DHCPMessage getDiscoverAnswer(DHCPMessage msg) throws UnknownHostException {
		return getOfferMsg(msg);
	}

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
		System.out.println("get Request answer");
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
		if(msg.getOptions().getOption(50) != null // Requested IP TODO check of gelijk aan offered adress!
			&& msg.getOptions().getOption(54) != null // server identifier set
			&& Utils.fromBytes(msg.getOptions().getOption(54)) == this.getServerID() // server identifier same as this server ID
			&& msg.getClientIP().equals(InetAddress.getByName("0.0.0.0")) // Client IP adress set
				){
			// new IP lease
			
			return getAckMsg(msg);
		}
			
			
		if(msg.getOptions().getOption(50) == null  // Requested IP not set
			&& msg.getOptions().getOption(54) == null // server identifier not set
			&& !msg.getClientIP().equals(InetAddress.getByName("0.0.0.0")) // Client IP adress set
				){
			// extend IP lease
			
			return getAckMsg(msg);
		}
		
		return getNakMsg(msg);
	}

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

	private InetAddress getServerIP() throws UnknownHostException {
		// TODO Auto-generated method stub
		return InetAddress.getByName("localhost");
	}

	@Override
	DHCPMessage getAckAnswer(DHCPMessage msg) {
		System.out.println("Server should not answer ACK message!");
		return null;
	}

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
	void processAck(DHCPMessage msg) {
		// TODO Auto-generated method stub
		
	}



}
