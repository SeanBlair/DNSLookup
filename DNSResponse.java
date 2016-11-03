



// Lots of the action associated with handling a DNS query is processing 
// the response. Although not required you might find the following skeleton of
// a DNSreponse helpful. The class below has bunch of instance data that typically needs to be 
// parsed from the response. If you decide to use this class keep in mind that it is just a 
// suggestion and feel free to add or delete methods to better suit your implementation as 
// well as instance variables.



public class DNSResponse {
	
	private byte[] responseData;
	private int responseArrayLength;
    
	private long queryID;                  // this is for the response it must match the one in the request 
    private boolean decoded = false;      // Was this response successfully decoded
    private boolean authoritative = false;// Is this an authoritative record
    
    private String fullyQualifiedDomainName;
    private int fqdnLength = 0;
    
    private long answerCount = 0;          // number of answers  
    private String[] answers;
    
    private long nameServerCount = 0;              // number of nscount response records
    private String[] nameServers;
    
    private long additionalRecordCount = 0;      // number of additional (alternate) response records
    private String[] additionalRecords;
    
    private int index = 0;

    // Note you will almost certainly need some additional instance variables.

    // When in trace mode you probably want to dump out all the relevant information in a response

	void dumpResponse() {

		// probably simply store the text we need to display
		// alternatively, could simply store whole DNSResponse object??
		//		this would allow more options with more data...


	}

    // The constructor: you may want to add additional parameters, but the two shown are 
    // probably the minimum that you need.

	public DNSResponse (byte[] data, int len, String fqdn, int fqdnLen) {
		responseData = data;
		responseArrayLength = len;
	    fullyQualifiedDomainName = fqdn;
	    fqdnLength = fqdnLen;
	    
	    // The following are probably some of the things 
	    // you will need to do.
		
	    // Extract the query ID
		
		// int consisting of the first two bytes of response
		// has to be equal to query id.. (check??)
		queryID = getUInt16(0);
		
	    // Make sure the message is a query response and determine
	    // if it is an authoritative response or note
		
		boolean isResponse = responseData[2] < 0; // needs to be more robust. Works because java bytes are signed.

		if (!isResponse){
			//do something about it...
		}
		
		authoritative = ((responseData[2] >> 2) & 0x1) == 1;

		    // determine answer count
		answerCount = getUInt16(6); 
		
		// determine NS Count
		nameServerCount = getUInt16(8); 
		
		// determine additional record count
		additionalRecordCount = getUInt16(10);
		
		// start of QNAME
		index = 12; 
		
		// end of QNAME
		index += fqdnLength + 1; 
		
		// 0 byte?
		int end = responseData[index++];
		
		if (!(end == 0)) {
			// do something??
		}
		
		long qType = getUInt16(index++);
		index++;
		// check something??
		
		long qClass = getUInt16(index++);
		index++;
		// check something??
		
		
		// Answer starts at data[index]
		
	    // Extract list of answers, name server, and additional information response 
	    // records
		
		// parse ...everything?

		//should look in Answers before parsing name server records?
		
		parseAnswers();
		
		
		//parseNameServerRecords();
		
		// what the hell are these for??
		
		//parseAdditionalRecords();
		

		// dump records... (create entry for -t option)
		
		// implement call to next server???
	}


    // You will probably want a methods to extract a compressed FQDN, IP address
    // cname, authoritative DNS servers and other values like the query ID etc.
	
	
	// creates an array of strings representing each answer resource
	// increments responseData index to beginning of next section.
	private void parseAnswers() {
		if (answerCount > 0) {
			answers = new String[(int) answerCount];
			for (int i = 0; i < answerCount; i++) { 
				answers[i] = parseResource();
			}
		}
	}

	// returns a string representing a resource record
	// increments responseData index to beginning of next resource
	
	private String parseResource() {
		String resourceName = parseWord();
		
		long resourceType = getUInt16(index);
		index += 2;
		long resourceClass = getUInt16(index);
		index += 2;
		long resourceTTL = getUInt32(index);
		index += 4;
		long resourceDataLength = getUInt16(index);
		index += 2;
		//String resourceData = parseWord();
		
		StringBuilder resourceString = new StringBuilder();
		resourceString.append("Resource Name: " + resourceName);
		resourceString.append(" Resource Type: " + resourceType);
		resourceString.append(" Resource Class: " + resourceClass);
		resourceString.append(" Resource TTL: " + resourceTTL);
		resourceString.append(" Resource Data Length: " + resourceDataLength);
		//resourceString.append(" Resource Data: " + resourceData);
		
		return resourceString.toString();
	}

	// returns a string terminated by 0
	// increment responseData index to beginning of next section.
	private String parseWord() {
		String word = "";
		int size = responseData[index];
		if (size != 0) {
			if (size < 0) {
				long pointerValue = getUInt16(index); 
				index += 2;              				// increment past second pointer byte.
				
				long x = 1;
				long y = x & 0x1L;
				
				long offset = (pointerValue & 0x000000003fffL);  
				word = parsePointerWord((int) offset);
				
			} else {
				index++; 								// increment past size byte
				String label = parseLabel(index, size);
				word += label;
				index += size;							// increment past size of label
				word += parseWord();
			}
		}
		return word + " ";
	}

	// returns string terminated by 0
	// does not mutate responseData index
	private String parsePointerWord(int idx) {
		String word = "";
		int size = responseData[idx];
		if (size != 0) {
			if (size < 0) {
				long pointerValue = getUInt16(idx);
				long offset = (pointerValue & 0x3fff);  
				word = parsePointerWord((int) offset);
				
			} else {
				idx++;
				String label = parseLabel(idx, size);
				word += label;
				word += parsePointerWord(idx + size);
			}
		}
		return word + " ";
	}

	// returns label Ex: www.  ubc.)
	private String parseLabel(int idx, int size) {
		String label = "";
		for (int i = 0; i < size; i++) {
			char letter = (char) responseData[idx++];
			label += letter;
		}
		return label + ".";
	}


	// based on stack overflow post
	private long byteAsULong(byte b) {
	    return ((long)b) & 0x00000000000000FFL; 
	}

	// based on stack overflow post
	private long getUInt32(int index) {
	    long value = byteAsULong(responseData[index++]) << 24 | (byteAsULong(responseData[index++]) << 16) | 
	    		(byteAsULong(responseData[index++]) << 8) | (byteAsULong(responseData[index]));
	    return value;
	}
	
	private long getUInt16(int index ) {
		long value = byteAsULong(responseData[index]) << 8 | (byteAsULong(responseData[index + 1]));
		return value;
	}
	
    // You will also want methods to extract the response records and record
    // the important values they are returning. Note that an IPV6 reponse record
    // is of type 28. It probably wouldn't hurt to have a response record class to hold
    // these records. 
}


