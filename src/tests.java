import static org.junit.Assert.*;

import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import threadedUDP.DHCPOptions;
import threadedUDP.MessageType;


public class tests {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		//fail("Not yet implemented");
	}
	
	@Test
	public void testOptions() throws UnknownHostException{
//		DHCPOptions options = new DHCPOptions();
//		System.out.println( new String( new byte[]{6} , StandardCharsets.UTF_8)); // TODO dit werkt dus al niet, dus onderstaande zeker niet :/
//		System.out.println( new String( new byte[]{(byte) MessageType.DHCPDISCOVER.getValue()} , StandardCharsets.UTF_8));
//		options.addOption(53,  new byte[]{(byte) MessageType.DHCPDISCOVER.getValue()});
//		System.out.println(options.encode());
		
	}

}
