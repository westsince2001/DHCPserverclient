package threadedUDP;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;

public class ClientCommander {
	
	/**
	 * Main function
	 * 
	 * @param args
	 */
	public static void main(String args[]){		
		new ClientCommander();
		
	}
	
	/**
	 * Wait for the input of the client that will be the server IP.
	 * After this, start a thread for the client. If you type certain commands in the console, the client commander will execute these commands on the client.
	 * 
	 */
	public ClientCommander() {
		try{
			// Read from command line
			byte[] readData = new byte[1024];
			BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
			
			System.out.println("Server IP adress (10.33.14.246 for CN server):");
			readData =  inFromUser.readLine().getBytes();
			InetAddress serverAddress = InetAddress.getByName(new String(readData));		
			
			System.out.println("\nPossible commands: 'lease', 'release', 'illegalrenew' \n");

			DHCPClient client = new DHCPClient(serverAddress);
			Thread thread = new Thread(client);
			thread.start(); // run method run in handler
			
			while(true){
				inFromUser = new BufferedReader(new InputStreamReader(System.in));
				readData =  inFromUser.readLine().getBytes();
				String command = new String(readData);
				switch(command){
				case "lease":
					System.out.println("--> LEASE command given");
					client.startLease();
					break;
				case "release":
					System.out.println("--> RELEASE command given");
					client.releaseLease();
					break;
				case "illegalrenew":
					System.out.println("--> ILLEGAL RENEW command given");
					client.setCurrentClientIP(InetAddress.getByName("255.1.1.1")); // Set invalid IP
		    		client.renewLease();
		    		break;
				default:
					System.out.println("--> ERROR! Given command '"+command+"' not known!");
					System.out.println("--> Possible commands: 'lease', 'release', 'illegalrenew' ");
				}
			}			
			
		}catch(Exception e){
			Utils.printError("Exception!");
		}
		
	}
	
	
}