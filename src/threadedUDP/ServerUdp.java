package threadedUDP;

import java.io.*;
import java.net.*;
import java.util.Properties;

public class ServerUdp {
	DatagramSocket serverSocket;

	public static void main(String[] args) throws Exception {
		ServerUdp server = new ServerUdp();
		server.startServer();
	}

	public ServerUdp() throws Exception {
		serverSocket = new DatagramSocket(getPort());
	}

	public void startServer() {

		try {
			System.out.println("server started");

			while (true) {
				// Receive data
				byte[] receiveData = new byte[1024];
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				System.out.println("server is listening");
				serverSocket.receive(receivePacket);
				System.out.println("server received packet");
				
				// Serve client (in thread)
				if (receivePacket != null) {
					System.out.println("server received packet not null");
					Handler h = new Handler(serverSocket, receivePacket);
					Thread thread = new Thread(h);
					thread.start(); // run method run in handler
				}
			}
		}
		catch(IOException e){
			System.out.println("IOException on receiving" + e);
		} finally {
			// Release resources on exception
			System.out.println("exit server");
			exit(serverSocket);
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

	public void exit(DatagramSocket serverSocket) {
		if (serverSocket != null) {
			serverSocket.close();
		}
	}

}
