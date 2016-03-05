package threadedUDP;

import java.io.*;
import java.net.*;
import java.util.Properties;
import java.util.Random;

import DHCPEnum.Hlen;
import DHCPEnum.Hops;
import DHCPEnum.Htype;
import DHCPEnum.MessageType;
import DHCPEnum.Opcode;

class ClientUdp {

	
	DatagramSocket clientSocket;
	
	public static void main(String args[]) {
		new ClientUdp();
	}

	public ClientUdp(){
		connectToServer();
	}
	public void connectToServer() {
		try {
			System.out.println("client started");
			DatagramSocket clientSocket = new DatagramSocket();
			
			sendDiscover();
			
			/* Algemene idee;
			 * discover sturen, dan in een while(true) loop die altijd weer checkt of er berichten zijn en antwoord, dus structuur is gelijkaardig aan de server?
			 * Threads zijn niet nodig wss?
			 * 
			 */
			
			while(true){
				byte[] receiveData = new byte[1024];
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				clientSocket.receive(receivePacket);

				byte[] byteMsg = receivePacket.getData();
				DHCPMessage msg = new DHCPMessage(byteMsg);
				handleMsg(msg);
				
			}

//			Oude code:
			
//			// Read from user
//			BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
//			String sentence = inFromUser.readLine();
//
//			// Create datagram socket
//			DatagramSocket clientSocket = new DatagramSocket();
//
//			// Send data
//			InetAddress IPAddress = InetAddress.getByName("localhost");
//			byte[] sendData = new byte[1024];
//			sendData = sentence.getBytes();
//			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, getPort());
//			clientSocket.send(sendPacket);
//
//			// Receive data
//			byte[] receiveData = new byte[1024];
//			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
//			clientSocket.receive(receivePacket);
//			String modifiedSentence = new String(receivePacket.getData());
//			System.out.println("FROM SERVER:" + modifiedSentence);
//
//			// Close socket
//			clientSocket.close();
			
		} catch (IOException e) {
			System.out.println(e);
			// Moet hier clientsocket nog sluiten? Maar object is in try gemaakt..
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
	
	private void handleMsg(DHCPMessage msg) throws IOException {
		switch (msg.getType()) {
		case DHCPDISCOVER:
		case DHCPACK:
			break;
		case DHCPDECLINE:
			break;
		case DHCPINFORM:
			break;
		case DHCPNAK:
			break;
		case DHCPOFFER:
			break;
		case DHCPRELEASE:
			break;
		case DHCPREQUEST:
			break;
		}
	}
	
	public void sendDiscover() throws IOException{
		/* Aangezien alle soorten DHCP berichten (Discover, offer, ack...) ongeveer dezelfde structuur hebben
		 * moet dit nog op een andere manier gebeuren
		 * 
		 */
		
		Opcode op = Opcode.BOOTREPLY;
		Htype htype = Htype.ETHERNET;
		Hlen hlen = Hlen.INTERNET;
		Hops hops = Hops.WHATEVER; /* TODO: hops (geen idee wat dat doet/is) */
		Random rand = new Random();
		int transactionID = rand.nextInt(2^32); // Random transaction id tussen 0 en 2^32
		int num_of_seconds = 0; /* TODO: overnemen uit msg ? */
		byte[] flags = new byte[0]; /* TODO: flags */
		InetAddress clientIP = InetAddress.getByName("localhost"); /* TODO */
		InetAddress serverIP = InetAddress.getByName("localhost"); /* TODO */
		InetAddress gatewayIP = InetAddress.getByName("localhost"); /* TODO */
		byte[] chaddr = new byte[0]; // TODO MAC adres uitlezen via JAVA?
		byte[] sname = new byte[0]; // TODO
		byte[] file = new byte[0]; // TODO
		MessageType type = MessageType.DHCPDISCOVER;

		DHCPMessage answer = new DHCPMessage(op, htype, hlen, hops,
				transactionID, num_of_seconds, flags, clientIP, serverIP,
				gatewayIP, chaddr, sname, file, type);
		
		byte[] sendData = answer.encode();
		
		DatagramPacket sendPacket = new DatagramPacket(sendData,
				sendData.length,  InetAddress.getByName("localhost"), getPort());
		clientSocket.send(sendPacket);
	}
	
	
	
}