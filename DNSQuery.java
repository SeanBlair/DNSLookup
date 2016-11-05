import java.io.*;
import java.net.*;
import java.util.*;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Random;
import java.util.regex.Pattern;
/**
 * 
 */

/**
 * @author Donald Acton
 * This example is adapted from Kurose & Ross
 *
 */
public class DNSQuery {

	final int MIN_PERMITTED_ARGUMENT_COUNT = 2;
	boolean tracingOn = false;
	InetAddress rootNameServer;
	int tries = 0;
	DatagramSocket datagramSocket;
	DatagramPacket packet;
	
	DNSResponse response;
	long queryID;
	int fqdnLength;
	String fqdn;
	String[] fqdnArray;
	
	int dnsPort;
	
	static byte[] requestBuffer;
	int index = 0;
	
	public DNSQuery(){
		this.dnsPort = 53;
	}
	
	/**
	 * @param args
	 */
	public void query(String[] args) throws Exception {
		int argCount = args.length;
		
		
		
		int responseBufferSize = 512;
		
		byte[] responseBuffer;
		
		if (argCount < 2 || argCount > 3) {
			usage();
			return;
		}

		rootNameServer = InetAddress.getByName(args[0]);
		
		fqdn = args[1];
		fqdnLength = fqdn.length();
		
		if (argCount == 3 && args[2].equals("-t"))
				tracingOn = true;
		
		try {
		setupSocket();
		} catch (SocketException socketException) {
			// TODO: socket exception caught
		}
		requestBuffer = new byte[fqdnLength + 18]; // 18 additional bytes for the hard-coded fields and random id. 

// for comparing with given example query.
//		byte[] exampleQuery = new byte[bufferSize];
//		exampleQuery = Files.readAllBytes(Paths.get("DNSInitialQuery.bin"));
//      byteArrayBuffer = exampleQuery;
		
		// Construct initial DNS Query
		setupRequestBuffer();
		
		// send packet
		
		// start printing output
		System.out.println("\n\nQuery ID     " + queryID + " " + fqdn + " --> " + rootNameServer.getHostAddress());
		 
		
        packet = new DatagramPacket(requestBuffer, requestBuffer.length, rootNameServer, dnsPort); //
        datagramSocket.send(packet);
        
        // for looking at sent byte array
        //String sent = Arrays.toString(requestBuffer);
        //System.out.println("Sent this DNSQuery to the DNS server: \n" + sent);
     
        responseBuffer = new byte[responseBufferSize];
        packet = new DatagramPacket(responseBuffer, responseBuffer.length);
        
        try {
        datagramSocket.receive(packet);
        tries++;
        } catch (SocketTimeoutException timeoutException) {
        	System.out.println("Query timed out.");
        	
        	if(tries == 2) {
        		System.out.println("Second time out dected");
        	}
        }
        
        response = new DNSResponse(responseBuffer, responseBufferSize, fqdn, fqdnLength);
        response.printResponse();
        
        // for viewing resulting dns response byte array.
        //String received = Arrays.toString(responseBuffer);
        //System.out.println("Received this response from DNS server: \n" + received);
        
        datagramSocket.close();
		
		System.out.println("\nHey dude, it looks like it's working...");	
	}
	private void setupSocket() throws SocketException{
		datagramSocket = new DatagramSocket();
		// Set timeout for receive() to 5 seconds (5000 ms)
		datagramSocket.setSoTimeout(5000);
	}
	
	// Construct initial DNS Query
	private void setupRequestBuffer() {
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


