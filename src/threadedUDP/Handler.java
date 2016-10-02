package threadedUDP;

import java.io.*;
import java.net.*;

public class Handler implements Runnable {
	
	/* CONSTRUCTOR */
	public Handler(DHCPServer server, DatagramSocket datagramSocket, DatagramPacket receivePacket) {
		this.server = server;
		this.serverSocket = datagramSocket;
		this.receivePacket = receivePacket;
	}

	/* RUN METHOD */
	@Override
	public void run() {
		System.out.println("\n##### SERVER IS SERVING CLIENT IN THREAD! #####");
		try {		
			
			// Decode received message
			byte[] byteMsg = getReceivePacket().getData();
			DHCPMessage receivedMessage = new DHCPMessage(byteMsg);
			
			// Print received message
			System.out.println("o Server received " + receivedMessage.getType());
			receivedMessage.print();
			
			// Process received message
			receivedMessage.getType().process(receivedMessage, server); // bv. release
			
			// Answer message
			DHCPMessage answer = receivedMessage.getType().getAnswer(receivedMessage, getServer());
			if (answer != null){ // e.g. release
				sendMsg(answer);
				
				// Process answer
				answer.getType().process(answer, server);
				
				// Print
				System.out.println("o Server sends " + answer.getType());
				answer.print();
				getServer().getLeases().print();
				System.out.println("  Available IP's: " + getServer().getLeases().availableIpToString());
			}
			
		}
		// Catch exception here and not in UdpServer --> other clients are still being served
		catch (Exception e) {
			System.out.println("WARNING: server could not correctly answer received message!");
			e.printStackTrace();
		}
	}
	
	/* SEND MESSAGE */

	/**
	 * Sends a given DHCPMessage
	 * 
	 * @throws IOException
	 * */
	private void sendMsg(DHCPMessage msg) throws IOException {
		// Message to bytes
		byte[] sendData = msg.encode();
		
		// Make sending packet
		DatagramPacket sendPacket = new DatagramPacket(sendData,
				sendData.length, getReceivePacket().getAddress(),
				getReceivePacket().getPort());
		
		// Send message
		getServerSocket().send(sendPacket);
	}
	
	/* VARIABLES */
	final DHCPServer server;
	final DatagramSocket serverSocket;
	final DatagramPacket receivePacket;

	/* GETTERS */
	public DHCPServer getServer() {
		return server;
	}

	public DatagramSocket getServerSocket() {
		return serverSocket;
	}

	public DatagramPacket getReceivePacket() {
		return receivePacket;
	}

}