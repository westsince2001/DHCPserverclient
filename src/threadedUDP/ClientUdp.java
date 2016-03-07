package threadedUDP;

import java.io.*;
import java.net.*;
import java.util.Properties;
import java.util.Random;

import com.sun.org.apache.bcel.internal.generic.NEW;

import DHCPEnum.Hlen;
import DHCPEnum.Htype;
import DHCPEnum.Opcode;

class ClientUdp extends Node{

	
	DatagramSocket clientSocket;
	
	public static void main(String args[]) {
		System.out.println(new String(new byte[4]));
		ClientUdp client = new ClientUdp();
		client.connectToServer();
	}

	public ClientUdp(){
	}
	
	public void connectToServer() {
		try {
			System.out.println("client started");
			DatagramSocket clientSocket = new DatagramSocket();
			
			// Send DHCP_Discover
			DHCPMessage msg = getDiscoverMsg();
			sendMsg(msg);
			
			// Answer incoming messages
			while(true){
				byte[] receiveData = new byte[1024];
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				clientSocket.receive(receivePacket);

				byte[] byteMsg = receivePacket.getData();
				msg = new DHCPMessage(byteMsg);
				msg.getType().getAnswer(msg, this);
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

	@Override
	DHCPMessage getDiscoverMsg() throws UnknownHostException {
		Opcode op = Opcode.BOOTREQUEST;
		Htype htype = Htype.ETHERNET;
		Hlen hlen = Hlen.INTERNET;
		byte hops = 0; /* TODO: hops */
		Random rand = new Random();
		int transactionID = rand.nextInt(2^32); // Random transaction id tussen 0 en 2^32
		short num_of_seconds = 0; /* TODO: overnemen uit msg ? */
		byte[] flags = new byte[] { 0x0, 0x0 }; /* TODO: flags moet nog naar broadcast */
		InetAddress clientIP = InetAddress.getByName("0.0.0.0"); /* TODO */
		InetAddress yourClientIP = InetAddress.getByName("0.0.0.0"); /* TODO */
		InetAddress serverIP = InetAddress.getByName("0.0.0.0"); /* TODO */
		InetAddress gatewayIP = InetAddress.getByName("0.0.0.0"); /* TODO */
		byte[] chaddr = new byte[16] ; // TODO MAC adres uitlezen via JAVA? -> http://stackoverflow.com/questions/6164167/get-mac-address-on-local-machine-with-java ?
		byte[] sname = new byte[64]; // TODO
		byte[] file = new byte[128]; // TODO
		MessageType type = MessageType.DHCPDISCOVER;

		DHCPMessage answer = new DHCPMessage(op, htype, hlen, hops,
				transactionID, num_of_seconds, flags, clientIP, yourClientIP, serverIP,
				gatewayIP, chaddr, sname, file, type);
		
		return answer;	
	}
	
	@Override
	public DHCPMessage getDiscoverAnswer(DHCPMessage msg) {
		System.out.println("Client received DHCP_DISCOVER but shouldn't process it.");
		return null;
	}
	
	@Override
	DHCPMessage getOfferMsg(DHCPMessage msg) throws UnknownHostException {	
		System.out.println("Clients cannot send DHCP_OFFER.");
		return null;
	}

	@Override
	DHCPMessage getOfferAnswer(DHCPMessage msg) throws UnknownHostException {
		DHCPMessage answer = getRequestMsg(msg); 
		return answer;
	}

	@Override
	DHCPMessage getRequestMsg(DHCPMessage msg) throws UnknownHostException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	DHCPMessage getRequestAnswer(DHCPMessage msg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	DHCPMessage getAckMsg(DHCPMessage msg) throws UnknownHostException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	DHCPMessage getAckAnswer(DHCPMessage msg) {
		// TODO Auto-generated method stub
		return null;
	}

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

	@Override
	DHCPMessage getReleaseMsg(DHCPMessage msg) throws UnknownHostException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	DHCPMessage getReleaseAnswer(DHCPMessage msg) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void sendMsg(DHCPMessage msg) throws IOException {
		byte[] sendData = msg.encode();
		DatagramPacket sendPacket = new DatagramPacket(sendData,
				sendData.length, InetAddress.getByName("10.33.14.246"),
				1234);
		clientSocket.send(sendPacket);
	}	
}