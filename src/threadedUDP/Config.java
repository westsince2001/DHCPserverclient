package threadedUDP;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Properties;

public class Config {
	private static String DEFAULT_CONFIG_FILE_PATH = "src/udpconfig.txt"; // Lease time in seconds
	private boolean USE_DEFAULTS = false;
	
	
	public Config(String file){
		if(file == null)
			USE_DEFAULTS = true;
		else
			setupProperties(file);
	}
	
	public Config(){
		this(DEFAULT_CONFIG_FILE_PATH);
	}
	
	private Properties pro = new Properties();;
	
	private void setupProperties(String file){
		try {
			FileInputStream in = new FileInputStream(file);
			pro.load(in);
		} catch (FileNotFoundException e) {
			Utils.printError("READING CONFIG FILE: File "+file+" not found! Using DEFAULT SETTINGS");
			USE_DEFAULTS = true;
		} catch(IOException e){
			Utils.printError("READING CONFIG FILE: IO EXCEPTION fir File "+file+" not found! Using DEFAULT SETTINGS");
			USE_DEFAULTS = true;
		}
		
	}
	private Properties getProperties(){
		return pro;
	}

	int getPort() {
		if(USE_DEFAULTS)
			return 1234;
		String port = getProperties().getProperty("port");
		assert port != null;
		return Integer.parseInt(port);
	}
	
	int getLeaseTime() {
		if(USE_DEFAULTS)
			return 10;
		String leasetime = getProperties().getProperty("leasetime");
		assert leasetime != null;
		return Integer.parseInt(leasetime);
	}
	
	ArrayList<InetAddress> getPool() {
		ArrayList<InetAddress> pool = new ArrayList<>();
		if(USE_DEFAULTS){
			return pool;
		}
		
		String str = getProperties().getProperty("pool");
		assert(str != null);
		
		String[] poolString = str.split(", ");
		for(String IPentry : poolString){
			try {
				if(IPentry.contains("-")){
					String[] splitted = IPentry.split("\\.");
					assert(splitted.length == 4);
					
					String endPart = splitted[3];
					String[] range = endPart.split("-");
					int startIP = Integer.parseInt(range[0]);
					int endIP = Integer.parseInt(range[1]);
					
					for(int i = startIP; i <= endIP; i++){
						String IPstr = splitted[0]+"."+splitted[1]+"."+splitted[2]+"."+i;
						pool.add(InetAddress.getByName(IPstr));
					}	
				}else{
					pool.add(InetAddress.getByName(IPentry));
				}
				
			} catch (UnknownHostException e) {
				Utils.printWarning("GETTING POOL: UnknownHostException!");
				e.printStackTrace();
			}
		}
		return pool;
	}

}