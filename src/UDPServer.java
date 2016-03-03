import java.io.*;
import java.net.*;

/* Ripped from 
 https://systembash.com/a-simple-java-udp-server-and-udp-client/
 
 " how to run:
in cmd, go into the directory where you save the file,
launch "javac UDPServer.java" and "javac UDPCliend.java" to compile the source file, 
when you see .class file then in cmd run "java UDPServer" and into another shell "Java UDPClient". that's all.
"

-> hievoor heb ik die client/bat/server.bat gemaakt maar da werkt blijkbaar niet ofzo. 't Is me wel al is gelukt om communicatie te krijgen toen de client in eclipse gerund was en de server manueel via CMD, geen idee hoe dat ging
Ook checken dat de poorten hieronder goed staan, en na iedere keer uitvoeren met ge de poort veranderen want die wordt nog nie vrijgegeven.






 */
class UDPServer {
	public static void main(String args[]) throws Exception {
		
		DatagramSocket serverSocket = new DatagramSocket(9881);
		
		/* Release connection resources on VM shutdown */
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
	        @Override
			public void run() {
	            System.out.println("In shutdown hook");
	            
	            serverSocket.close();
	            
	        }
	    }, "Shutdown-thread"));
		
		
		
		byte[] receiveData = new byte[1024];
		byte[] sendData = new byte[1024];
		while (true) {
			// receive packet
			DatagramPacket receivePacket = new DatagramPacket(receiveData,
					receiveData.length);
			serverSocket.receive(receivePacket);
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
		

		
	}
	

	
}