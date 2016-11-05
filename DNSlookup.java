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
	
	static byte[] requestBuffer;
	int index = 0;
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		DNSResponse response;
		ArrayList<String> trace = new ArrayList<String>();
		long queryID;

		int argCount = args.length;
		
		int fqdnLength;
		String fqdn;
		String[] fqdnArray;
		
		DatagramSocket datagramSocket;
		DatagramPacket packet;
		int dnsPort = 53;
		
		int responseBufferSize = 512;
		
		byte[] responseBuffer;
		int index = 0;
		
		if (argCount < 2 || argCount > 3) {
			usage();
			return;
		}

		rootNameServer = InetAddress.getByName(args[0]);
		
		fqdn = args[1];
		fqdnLength = fqdn.length();
		
		if (argCount == 3 && args[2].equals("-t"))
				tracingOn = true;
		
		// Start adding code here to initiate the lookup
		
		datagramSocket = new DatagramSocket();
		requestBuffer = new byte[fqdnLength + 18]; // 18 additional bytes for the hard-coded fields and random id. 

// for comparing with given example query.
//		byte[] exampleQuery = new byte[bufferSize];
//		exampleQuery = Files.readAllBytes(Paths.get("DNSInitialQuery.bin"));
//      byteArrayBuffer = exampleQuery;
		
		// Construct initial DNS Query
		
		// assign a random number as queryId
		// TODO look into a more complete implementation
		Random r = new Random();
		int id = r.nextInt(126); 
		requestBuffer[index++] = (byte) id;
		id = r.nextInt(255);
		requestBuffer[index++] = (byte) id;
		
		queryID = getUInt16(0);
		
		// set next 16 bits to 0
		requestBuffer[index++] = 0;
		requestBuffer[index++] = 0;
		
		//set Query Count (QDCOUNT) to 1
		requestBuffer[index++] = 0;
		requestBuffer[index++] = 1;
		
		// set Answer Count (ANCOUNT) to 0
		requestBuffer[index++] = 0;
		requestBuffer[index++] = 0;
		
		// set Name Server Records (NSCOUNT) to 0
		requestBuffer[index++] = 0;
		requestBuffer[index++] = 0;
		
		// set Additional Record Count (ARCOUNT) to 0
		requestBuffer[index++] = 0;
		requestBuffer[index++] = 0;
		
		// Start of QNAME
		fqdnArray = fqdn.split(Pattern.quote("."));
		
		for (String part : fqdnArray) {
			requestBuffer[index++] = (byte) part.length();
			for (char letter : part.toCharArray()) {
				requestBuffer[index++] = (byte) letter;
			}
		}
		
		// end with 0 (1 byte only) (indicates end of QNAME	
		requestBuffer[index++] = 0;
		
		// QTYPE
		requestBuffer[index++] = 0;
		requestBuffer[index++] = 1;
		// QCLASS
		requestBuffer[index++] = 0;
		requestBuffer[index++] = 1;
		
		// send packet
		
		do {
			trace.add("\n\nQuery ID     " + queryID + " " + fqdn + " --> " + rootNameServer.getHostAddress());
			packet = new DatagramPacket(requestBuffer, requestBuffer.length, rootNameServer, dnsPort);
			datagramSocket.send(packet);
			
			responseBuffer = new byte[responseBufferSize];  // new byte array each time?? or use same one...??
	        packet = new DatagramPacket(responseBuffer, responseBuffer.length);
	        datagramSocket.receive(packet);
	        
	        response = new DNSResponse(responseBuffer, responseBufferSize, fqdn, fqdnLength);
	      
	        trace.addAll(response.getTrace());

	        // set new query...  new queryID and rootNameServer
		} 
		while (!response.isAuthoritative());
		
		if (tracingOn) {
			for (String line : trace) {
				System.out.println(line);
			}
    	}
		
    	System.out.println(fqdn + response.getAnswer());
    	
    	
		//trace.add("\n\nQuery ID     " + queryID + " " + fqdn + " --> " + rootNameServer.getHostAddress());
		
		//System.out.println("\n\nQuery ID     " + queryID + " " + fqdn + " --> " + rootNameServer.getHostAddress());
		 
		
//        packet = new DatagramPacket(requestBuffer, requestBuffer.length, rootNameServer, dnsPort); //
//        datagramSocket.send(packet);
//        
        // for looking at sent byte array
        //String sent = Arrays.toString(requestBuffer);
        //System.out.println("Sent this DNSQuery to the DNS server: \n" + sent);
     
        // receive response
        // TODO implement time out check (5 seconds??)
//        responseBuffer = new byte[responseBufferSize];
//        packet = new DatagramPacket(responseBuffer, responseBuffer.length);
//        datagramSocket.receive(packet);
 
//        response = new DNSResponse(responseBuffer, responseBufferSize, fqdn, fqdnLength);
//        
//        if (response.isAuthoritative()) {
//        	if (tracingOn) {
//        		System.out.println(response.getTrace());
//        	}
//        	System.out.println(response.getAnswer());
//        } else {
//        	callRecursion();
//        }
        
        
        
        // for viewing resulting dns response byte array.
        //String received = Arrays.toString(responseBuffer);
        //System.out.println("Received this response from DNS server: \n" + received);
        
        datagramSocket.close();
		
		System.out.println("\nHey dude, it looks like it's working...");	
	}
	// based on stack overflow post
	private static long byteAsULong(byte b) {
	    return ((long)b) & 0x00000000000000FFL; 
	}

	// based on stack overflow post
	private static long getUInt16(int index ) {
		long value = byteAsULong(requestBuffer[index]) << 8 | (byteAsULong(requestBuffer[index + 1]));
		return value;
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


