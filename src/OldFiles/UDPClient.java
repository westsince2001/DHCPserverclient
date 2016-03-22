package OldFiles;


import java.io.*;
import java.net.*;



/* Ripped from 
 https://systembash.com/a-simple-java-udp-server-and-udp-client/
 */
class UDPClient {
	
	public static void main(String args[]) throws Exception {
		System.out.println("client started");
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(
				System.in));
		
		DatagramSocket clientSocket = new DatagramSocket();
		InetAddress IPAddress = InetAddress.getByName("localhost");

		byte[] sendData = new byte[1024];
		byte[] receiveData = new byte[1024];
		String sentence = inFromUser.readLine();
		sendData = sentence.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendData,
				sendData.length, IPAddress, 9888);
		clientSocket.send(sendPacket);
		DatagramPacket receivePacket = new DatagramPacket(receiveData,
				receiveData.length);
		clientSocket.receive(receivePacket);
		String modifiedSentence = new String(receivePacket.getData());
		System.out.println("FROM SERVER:" + modifiedSentence);
		clientSocket.close();
	}
}