import java.net.*;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * @author Gurjot & Sean
 *
 */
public class DNSQuery {
	
	private final int dnsPort = 53;
	
	private boolean resolvingNameServer = false;
	private boolean tracingOn;
	private int timeouts, numQueries;
	private DatagramSocket datagramSocket;
	private String originalHostServer, originalFQDN;
	private int responseBufferSize;
	private ArrayList<String> trace;
	private ArrayList<Integer> ttlValues;
	
	public DNSQuery(String originalHostServer, String originalFQDN, boolean tracingOn) throws SocketException{
		this.tracingOn = tracingOn;
		this.responseBufferSize = 512;
		this.timeouts = 0;
		this.numQueries = 0;
		this.originalHostServer = originalHostServer;
		this.originalFQDN = originalFQDN;
		this.trace = new ArrayList<String>();
		this.ttlValues = new ArrayList<Integer>();
		
		setupSocket();  // Create socket with 5 second timeout set.
	}
	
	/**
	 * @param nameServerIP					The IP of the root name server.
	 * @param fullyQualifiedDomainName		The fully qualified domain name.
	 * 
	 * @return		The resolved IP if query is for name server. Null otherwise.
	 * @throws Exception		
	 */
	public String query(String nameServerIP, String fullyQualifiedDomainName) throws Exception{
		this.numQueries++;
		if(numQueries >= 30) {
			exitProgram(originalFQDN + " -3 0.0.0.0");
		}
		
		InetAddress rootNameServer = InetAddress.getByName(nameServerIP);
		
		String fqdn = fullyQualifiedDomainName;
		int fqdnLength = fqdn.length();
		byte[] requestBuffer = new byte[fqdnLength + 18]; // 18 additional bytes for the hard-coded fields and random id. 
		long queryID = setupRequestBuffer(requestBuffer, fqdn); 
		
		trace.add("\n\nQuery ID     " + queryID + " " + fqdn + " --> " + rootNameServer.getHostAddress());
		
		// Send packet.
        DatagramPacket packet = new DatagramPacket(requestBuffer, requestBuffer.length, rootNameServer, dnsPort); //
        datagramSocket.send(packet);
        
        byte [] responseBuffer = new byte[responseBufferSize];
        packet = new DatagramPacket(responseBuffer, responseBuffer.length);
        
        try {
        datagramSocket.receive(packet);
        } catch (SocketTimeoutException timeoutException) {
        	timeouts++;
        	if(timeouts == 2) {  
        		exitProgram(originalFQDN + " -2 0.0.0.0");
        	}
        	this.query(nameServerIP, fqdn);
        }
        
        DNSResponse response = new DNSResponse(responseBuffer, responseBufferSize, fqdn, fqdnLength);
        //response.printResponse();
        trace.addAll(response.getTrace());  // might cause exceptions indicating invalid response that should be caught and dealt with.
        
        if(response.isAuthoritative()) {
        	if(resolvingNameServer) {
        		return response.getAnswersFirstResourceData();
        	}
        	if(response.isAnswerCNAME()) {
        		// DNS resolved to a CNAME instead of an IP Address.
        		ttlValues.add(response.getAnswersFirstResourceTTL()); 	// Save TTL.
            	return this.query(originalHostServer, response.getAnswersFirstResourceData());
        	} else {
	    		// Auth true && not CNAME: DONE - print results.
	    		String resolvedIP = response.getAnswersFirstResourceData();
	    		ttlValues.add(response.getAnswersFirstResourceTTL());	// Save TTL.
	        	String answer = originalFQDN + " " + getSmallestTTL() + " " + resolvedIP;
	        	printProgramOutput(answer);
	        	datagramSocket.close();
        	}
        } 
        else {
        	if(response.getValidNameServerIP() != null) {
        		// Matching name server and additional info entry.
        		return this.query(response.getValidNameServerIP(), fullyQualifiedDomainName);
        	} else {
        		// No match, need to resolve name server.
        		resolvingNameServer = true;
        		String resolvedNameServerIP = this.query(originalHostServer, response.getFirstNameServerName());
        		resolvingNameServer = false;
        		this.query(resolvedNameServerIP, originalFQDN);
        	}
        }
        
        return null;
	}
	
	/**
	 * @return The smallest TTL value in the ttlValues array.
	 */
	private int getSmallestTTL() {
		int minTTL = Integer.MAX_VALUE;
		for(Integer i : ttlValues) {
			if(i < minTTL) {
				minTTL = i;
			}
		}
		return minTTL;
	}

	private void exitProgram(String string) {
		printProgramOutput(string);
		System.exit(0);	
	}

	private void printProgramOutput(String string) {
		if (tracingOn) {
			for (String line : trace) {
				System.out.println(line);
			}
		}
		System.out.println(string);
	}

	private void setupSocket() throws SocketException{
		datagramSocket = new DatagramSocket();
		// Set timeout for receive() to 5 seconds (5000 ms).
		datagramSocket.setSoTimeout(5000);
	}
	
	/**
	 * @param requestBuffer		Request buffer that needs to be constructed.
	 * @param fqdn				The fully qualified domain name.
	 * @return The query ID.
	 * 
	 * Constructs DNS query.
	 */
	private long setupRequestBuffer(byte[] requestBuffer, String fqdn) {
		int index = 0;
		
		Random r = new Random();
		int id = r.nextInt(126); // Assign a random number as query id. 
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
	
	private static long byteAsULong(byte b) {
	    return ((long)b) & 0x00000000000000FFL; 
	}

	private long getUInt16(int index, byte[] requestBuffer) {
		long value = byteAsULong(requestBuffer[index]) << 8 | (byteAsULong(requestBuffer[index + 1]));
		return value;
	}
}