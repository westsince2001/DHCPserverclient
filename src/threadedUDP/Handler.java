package threadedUDP;

import java.io.*;
import java.net.*;

import DHCPEnum.Hlen;
import DHCPEnum.Htype;
import DHCPEnum.Opcode;

public class Handler implements Runnable {
	UdpServer server;
	DatagramSocket serverSocket;
	DatagramPacket receivePacket;

	public Handler(UdpServer server, DatagramSocket datagramSocket, DatagramPacket receivePacket) {
		System.out.println("# server handler constructed");
		this.server = server;
		this.serverSocket = datagramSocket;
		this.receivePacket = receivePacket;
		
	}

	public void run() {
		System.out.println("##### Server handler run #####");
		try {			
			byte[] byteMsg = receivePacket.getData();
			
			try {
				DHCPMessage msg = new DHCPMessage(byteMsg);
				System.out.println("# server received message:");
				msg.print();
				DHCPMessage answer = msg.getType().getAnswer(msg, server);
				if(answer != null){
					System.out.println("# server generated answer");
					answer.print();
					sendMsg(answer);
				}else{
					System.out.println("WARNING: server could not answer received message!");
				}
				
			} catch (UnknownMessageTypeException e) {
				System.out.println("WARNING: received DHCP message without Type option (53)!");
			}
			
			
		}

		catch (IOException e) {
			System.out.println("IOException on sending" + e);
			e.printStackTrace();
			// connectie sluiten of voert finally clause nog uit in ServerUdp?
		}
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
		System.out.println("# server sent message");
	}
}
