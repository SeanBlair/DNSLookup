import java.io.*;
import java.net.*;
import java.util.*;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;
import java.util.regex.Pattern;
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
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		DNSResponse response;
		
		int argCount = args.length;
		
		String fqdn;
		String[] fqdnArray;
		
		DatagramSocket datagramSocket;
		DatagramPacket packet;
		int dnsPort = 53;
		
		int bufferSize = 512;
		byte[] byteArrayBuffer;
		int index = 0;
		
		if (argCount < 2 || argCount > 3) {
			usage();
			return;
		}

		rootNameServer = InetAddress.getByName(args[0]);
		fqdn = args[1];
		
		if (argCount == 3 && args[2].equals("-t"))
				tracingOn = true;
		
		// Start adding code here to initiate the lookup
		
		datagramSocket = new DatagramSocket();
		byteArrayBuffer = new byte[bufferSize];

// for comparing with given example query.
//		byte[] exampleQuery = new byte[bufferSize];
//		exampleQuery = Files.readAllBytes(Paths.get("DNSInitialQuery.bin"));
//      byteArrayBuffer = exampleQuery;
		
		// Construct initial DNS Query
		
		// assign a random number as queryId
		// TODO look into a more complete implementation
		Random r = new Random();
		int queryId = r.nextInt(126); 
		byteArrayBuffer[index++] = (byte) queryId;
		queryId = r.nextInt(255);
		byteArrayBuffer[index++] = (byte) queryId;
		
		// set next 16 bits to 0
		byteArrayBuffer[index++] = 0;
		byteArrayBuffer[index++] = 0;
		
		//set Query Count (QDCOUNT) to 1
		byteArrayBuffer[index++] = 0;
		byteArrayBuffer[index++] = 1;
		
		// set Answer Count (ANCOUNT) to 0
		byteArrayBuffer[index++] = 0;
		byteArrayBuffer[index++] = 0;
		
		// set Name Server Records (NSCOUNT) to 0
		byteArrayBuffer[index++] = 0;
		byteArrayBuffer[index++] = 0;
		
		// set Additional Record Count (ARCOUNT) to 0
		byteArrayBuffer[index++] = 0;
		byteArrayBuffer[index++] = 0;
		
		// Start of QNAME
		fqdnArray = fqdn.split(Pattern.quote("."));
		
		for (String part : fqdnArray) {
			byteArrayBuffer[index++] = (byte) part.length();
			for (char letter : part.toCharArray()) {
				byteArrayBuffer[index++] = (byte) letter;
			}
		}
		
		// end with 0 (1 byte only) (indicates end of QNAME	
		byteArrayBuffer[index++] = 0;
		
		// QTYPE
		byteArrayBuffer[index++] = 0;
		byteArrayBuffer[index++] = 1;
		// QCLASS
		byteArrayBuffer[index++] = 0;
		byteArrayBuffer[index++] = 1;
		
		// send packet
        packet = new DatagramPacket(byteArrayBuffer, byteArrayBuffer.length, rootNameServer, dnsPort); //
        datagramSocket.send(packet);
     
        // receive response
        // TODO implement time out check (5 seconds??)
        packet = new DatagramPacket(byteArrayBuffer, byteArrayBuffer.length);
        datagramSocket.receive(packet);
 
        // not using this yet...
        response = new DNSResponse(byteArrayBuffer, bufferSize);
        
        // for viewing resulting dns response byte array.
        String received = Arrays.toString(byteArrayBuffer);
        System.out.println("Received this response from DNS server: \n" + received);
        
        datagramSocket.close();
		
		System.out.println("Hey dude, it looks like it's working...");	
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


