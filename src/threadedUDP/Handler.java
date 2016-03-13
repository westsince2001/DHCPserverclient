package threadedUDP;

import java.io.*;
import java.net.*;

import DHCPEnum.Hlen;
import DHCPEnum.Htype;
import DHCPEnum.Opcode;

public class Handler implements Runnable {
	
	/* CONSTRUCTOR */
	public Handler(UdpServer server, DatagramSocket datagramSocket, DatagramPacket receivePacket) {
		this.server = server;
		this.serverSocket = datagramSocket;
		this.receivePacket = receivePacket;
	}

	/* RUN METHOD */
	public void run() {
		System.out.println();
		System.out.println("##### SERVER IS SERVING CLIENT IN THREAD ##### (handler)");
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
			}
			
		}
		// Catch exception here and not in UdpServer --> other clients are still being served
		catch (Exception e) {
			System.out.println("WARNING: server could not correctly answer received message!");
			e.printStackTrace();
		}
		finally{
			System.out.println();
			System.out.println("///// Thread is executed //////");
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
		byte[] sendData = msg.encode(); // TODO: geeft error na ack na release van client
		
		// Make sending packet
		DatagramPacket sendPacket = new DatagramPacket(sendData,
				sendData.length, getReceivePacket().getAddress(),
				getReceivePacket().getPort());
		
		// Send message
		getServerSocket().send(sendPacket);
	}
	
	/* VARIABLES */
	final UdpServer server;
	final DatagramSocket serverSocket;
	final DatagramPacket receivePacket;

	/* GETTERS */
	public UdpServer getServer() {
		return server;
	}

	public DatagramSocket getServerSocket() {
		return serverSocket;
	}

	public DatagramPacket getReceivePacket() {
		return receivePacket;
	}

}