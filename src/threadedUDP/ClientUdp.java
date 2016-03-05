package threadedUDP;

import java.io.*;
import java.net.*;
import java.util.Properties;

class ClientUdp {

	public static void main(String args[]) {
		connectToServer();
	}

	public static void connectToServer() {
		try {
			System.out.println("client started");

			// Read from user
			BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
			String sentence = inFromUser.readLine();

			// Create datagram socket
			DatagramSocket clientSocket = new DatagramSocket();

			// Send data
			InetAddress IPAddress = InetAddress.getByName("localhost");
			byte[] sendData = new byte[1024];
			sendData = sentence.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, getPort());
			clientSocket.send(sendPacket);

			// Receive data
			byte[] receiveData = new byte[1024];
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			clientSocket.receive(receivePacket);
			String modifiedSentence = new String(receivePacket.getData());
			System.out.println("FROM SERVER:" + modifiedSentence);

			// Close socket
			clientSocket.close();
			
		} catch (IOException e) {
			System.out.println(e);
			// Moet hier clientsocket nog sluiten? Maar object is in try gemaakt..
;		}
	}
	
	private static int getPort() throws IOException {
		Properties pro = new Properties();
		FileInputStream in = new FileInputStream("src/udpconfig.txt");
		pro.load(in);
		String port = pro.getProperty("port");
		int result = Integer.parseInt(port);
		return result;
	}
}