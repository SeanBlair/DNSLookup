import java.io.*;
import java.net.*;
import java.util.*;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;
import java.nio.file.Files;
import java.nio.file.Paths;
/**
 * 
 */

/**
 * @author Donald Acton
 * This example is adapted from Kurose & Ross
 *
 */
public class DNSlookup {


	static final int MIN_PERMITTED_ARGUMENT_COUNT = 2;
	static boolean tracingOn = false;
	static InetAddress rootNameServer;
	static int bufferSize = 512;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		String fqdn;
		DNSResponse response; // Just to force compilation
		int argCount = args.length;
		
		if (argCount < 2 || argCount > 3) {
			usage();
			return;
		}

		rootNameServer = InetAddress.getByName(args[0]);
		fqdn = args[1];
		
		if (argCount == 3 && args[2].equals("-t"))
				tracingOn = true;
		
		// Start adding code here to initiate the lookup
		
		
		DatagramSocket datagramSocket = new DatagramSocket();
		
		byte[] byteArrayBuffer = new byte[bufferSize];
		byteArrayBuffer = Files.readAllBytes(Paths.get("DNSInitialQuery.bin"));
		
        DatagramPacket packet = new DatagramPacket(byteArrayBuffer, byteArrayBuffer.length, rootNameServer, 53);
        datagramSocket.send(packet);
     
        
        
        packet = new DatagramPacket(byteArrayBuffer, byteArrayBuffer.length);
        datagramSocket.receive(packet);
 

        response = new DNSResponse(byteArrayBuffer, bufferSize);
        
        
        //will have to decode here if encoded...
        
        String received = Arrays.toString(byteArrayBuffer);
        System.out.println("Received this response from DNS server: \n" + received);
        
        datagramSocket.close();
		
		System.out.println("Hey dude, don't let me down..");
		
	}

	private static void usage() {
		System.out.println("Usage: java -jar DNSlookup.jar rootDNS name [-t]");
		System.out.println("   where");
		System.out.println("       rootDNS - the IP address (in dotted form) of the root");
		System.out.println("                 DNS server you are to start your search at");
		System.out.println("       name    - fully qualified domain name to lookup");
		System.out.println("       -t      -trace the queries made and responses received");
	}
}


