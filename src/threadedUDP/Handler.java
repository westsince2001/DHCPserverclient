package threadedUDP;

import java.io.*;
import java.net.*;

import DHCPEnum.Hlen;
import DHCPEnum.Hops;
import DHCPEnum.Htype;
import DHCPEnum.MessageType;
import DHCPEnum.Opcode;

public class Handler implements Runnable {
	DatagramSocket serverSocket;
	DatagramPacket receivePacket;

	public Handler(DatagramSocket datagramSocket, DatagramPacket receivePacket) {
		System.out.println("server handler constructed");
		this.serverSocket = datagramSocket;
		this.receivePacket = receivePacket;
	}

	public void run() {
		System.out.println("server handler run");
		try {
			
			// Print received data
			// String sentence = new String(receivePacket.getData());
			// System.out.println("RECEIVED: " + sentence);
			
			byte[] byteMsg = receivePacket.getData();
			DHCPMessage msg = new DHCPMessage(byteMsg);

			handleMsg(msg);
		}

		catch (IOException e) {
			System.out.println("IOException on sending" + e);
			e.printStackTrace();
			// connectie sluiten of voert finally clause nog uit in ServerUdp?
		}
	}

	private void handleMsg(DHCPMessage msg) throws IOException {
		switch (msg.getType()) {
		case DHCPDISCOVER:
			sendOffer(msg);
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

	/**
	 * Sends an offer as answer to the given msg which should be a DISOVER type
	 * message
	 * @throws IOException 
	 **/
	private void sendOffer(DHCPMessage msg) throws IOException {
		
		/* Aangezien alle soorten DHCP berichten (Discover, offer, ack...) ongeveer dezelfde structuur hebben
		 * moet dit nog op een andere manier gebeuren
		 * 
		 */
		
		Opcode op = Opcode.BOOTREPLY;
		Htype htype = Htype.ETHERNET;
		Hlen hlen = Hlen.INTERNET;
		Hops hops = Hops.WHATEVER; /* TODO: hops (geen idee wat dat doet/is) */
		int transactionID = msg.getTransactionID();
		int num_of_seconds = 0; /* TODO: overnemen uit msg ? */
		byte[] flags = new byte[0]; /* TODO: flags */
		InetAddress clientIP = InetAddress.getByName("localhost"); /* TODO */
		InetAddress serverIP = InetAddress.getByName("localhost"); /* TODO */
		InetAddress gatewayIP = InetAddress.getByName("localhost"); /* TODO */
		byte[] chaddr = msg.getChaddr();
		byte[] sname = new byte[0]; // TODO
		byte[] file = new byte[0]; // TODO
		MessageType type = MessageType.DHCPOFFER;

		DHCPMessage answer = new DHCPMessage(op, htype, hlen, hops,
				transactionID, num_of_seconds, flags, clientIP, serverIP,
				gatewayIP, chaddr, sname, file, type);

		sendMsg(answer);
	}

	/**
	 * Sends a given DHCPMessage
	 * 
	 * @throws IOException
	 * */
	private void sendMsg(DHCPMessage msg) throws IOException {
		byte[] sendData = msg.encode();
		DatagramPacket sendPacket = new DatagramPacket(sendData,
				sendData.length, receivePacket.getAddress(),
				receivePacket.getPort());
		serverSocket.send(sendPacket);
	}
}
