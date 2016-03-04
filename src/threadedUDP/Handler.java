package threadedUDP;

import java.io.*;
import java.net.*;

public class Handler implements Runnable {
	DatagramSocket serverSocket;
	DatagramPacket receivePacket;
	byte[] sendData = new byte[1024];

	public Handler(DatagramSocket datagramSocket, DatagramPacket receivePacket) { // dummy
		System.out.println("server handler constructed");																	// receivePacket
		this.serverSocket = datagramSocket;
		this.receivePacket = receivePacket;
	}

	public void run() {
		System.out.println("server handler run");
		try {
			String sentence = new String(receivePacket.getData());
			System.out.println("RECEIVED: " + sentence);

			// send packet
			InetAddress IPAddress = receivePacket.getAddress();
			int port = receivePacket.getPort();
			String capitalizedSentence = sentence.toUpperCase();
			sendData = capitalizedSentence.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData,
					sendData.length, IPAddress, port);
			serverSocket.send(sendPacket);
		}

		catch (IOException e) {
			System.out.println("IOException on socket listen " + e);
			e.printStackTrace();
		}
	}

}
