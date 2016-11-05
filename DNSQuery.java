import java.net.*;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * @author Gurjot & Sean
 *
 */
public class DNSQuery {
	
	private final int dnsPort = 53;
	
	private boolean tracingOn;  // TODO: might not need this
	private int timeouts; // Used to keep track of timeouts
	private int numQueries;
	private DatagramSocket datagramSocket;
	
	private int responseBufferSize;
	
	public DNSQuery(){
		this.tracingOn = false;
		this.responseBufferSize = 512;
		this.timeouts = 0;
		this.numQueries = 0;
	}
	
	/**
	 * @param args
	 */
	public void query(String hostServer, String fullyQualifiedDomainName) throws SocketException, Exception {
    
		this.numQueries++;
		InetAddress rootNameServer = InetAddress.getByName(hostServer);
		
		String fqdn = fullyQualifiedDomainName;
		int fqdnLength = fqdn.length();
		byte[] requestBuffer = new byte[fqdnLength + 18]; // 18 additional bytes for the hard-coded fields and random id. 
		long queryID = setupRequestBuffer(requestBuffer, fqdn);
		setupSocket();
		
		// Start printing output
		System.out.println("\n\nQuery ID     " + queryID + " " + fqdn + " --> " + rootNameServer.getHostAddress());
		
		// Send packet.
        DatagramPacket packet = new DatagramPacket(requestBuffer, requestBuffer.length, rootNameServer, dnsPort); //
        datagramSocket.send(packet);
        
        byte [] responseBuffer = new byte[responseBufferSize];
        packet = new DatagramPacket(responseBuffer, responseBuffer.length);
        
        try {
        datagramSocket.receive(packet);
        } catch (SocketTimeoutException timeoutException) {
        	timeouts++;
        	System.out.println("Query timed out.");
        	
        	if(timeouts == 2) {
        		// TODO
        		System.out.println("Second time out dected");
        	}
        }
        
        DNSResponse response = new DNSResponse(responseBuffer, responseBufferSize, fqdn, fqdnLength);
        response.printResponse();
        
        
        if(response.isAnswerCNAME()) {
        	// DNS resolved to a CNAME instead of an IP Address.
        	// Try to now resolve CNAME
        	this.query(hostServer, response.getCNAME());
        }
        
        String resolvedIP = "test.ip.addr";
        int finalTimeToLive = 12345;
        System.out.println(fullyQualifiedDomainName + " " + finalTimeToLive + " " + resolvedIP);
        datagramSocket.close();
		System.out.println("\nHey dude, it looks like it's working...");	
	}
	
	private void setupSocket() throws SocketException{
		datagramSocket = new DatagramSocket();
		// Set timeout for receive() to 5 seconds (5000 ms).
		datagramSocket.setSoTimeout(5000);
	}
	
	// Construct initial DNS Query, returns query ID
	private long setupRequestBuffer(byte[] requestBuffer, String fqdn) {
		// assign a random number as queryId
		// TODO look into a more complete implementation
		int index = 0;
		
		Random r = new Random();
		int id = r.nextInt(126); 
		requestBuffer[index++] = (byte) id;
		id = r.nextInt(255);
		requestBuffer[index++] = (byte) id;
		
		long queryID = getUInt16(0, requestBuffer);
		
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
		String[] fqdnArray = fqdn.split(Pattern.quote("."));
		
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
		
		return queryID;
	}
	
	// based on stack overflow post
	private static long byteAsULong(byte b) {
	    return ((long)b) & 0x00000000000000FFL; 
	}

	// based on stack overflow post
	private long getUInt16(int index, byte[] requestBuffer) {
		long value = byteAsULong(requestBuffer[index]) << 8 | (byteAsULong(requestBuffer[index + 1]));
		return value;
	}
}