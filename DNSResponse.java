import java.util.ArrayList;


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

    private boolean authoritative = false;// Is this an authoritative record

    private String fullyQualifiedDomainName;
    private int fqdnLength = 0;
    
    private long answerCount = 0;          // number of answers  
    private Resource[] answers;
    
    private long nameServerCount = 0;              // number of nscount response records
    private Resource[] nameServers;
    
    private long additionalRecordCount = 0;      // number of additional (alternate) response records
    private Resource[] additionalRecords;
    
    private int index = 0;
    
    char[] trace;

    // Note you will almost certainly need some additional instance variables.

    
//    // When in trace mode you probably want to dump out all the relevant information in a response
//	void dumpResponse() {
//
//		// not sure what this is supposed to do...
//		// probably simply store the text we need to display
//		// alternatively, could simply store whole DNSResponse object??
//		//		this would allow more options with more data...
//
//
//	}

    // The constructor: you may want to add additional parameters, but the two shown are 
    // probably the minimum that you need.

	public DNSResponse (byte[] data, int len, String fqdn, int fqdnLen) throws NonExistentNameException, GenericException {
		responseData = data;
		responseArrayLength = len;
	    fullyQualifiedDomainName = fqdn;
	    fqdnLength = fqdnLen;
	    
	    // Extract the query ID
		queryID = getUInt16(0);
		
	    // Make sure the message is a query response and determine
	    // if it is an authoritative response or note		
		boolean isResponse = responseData[2] < 0; // needs to be more robust. Works because java bytes are signed.
		if (!isResponse){
			// TODO: do something about it...
			throw new GenericException();
		}
		
		authoritative = ((responseData[2] >> 2) & 0x1) == 1;
		
		long rCode = byteAsULong(responseData[2]) & 0xf;
		if (rCode == 3) {
			throw new NonExistentNameException();
		} else if (rCode != 0) {
			throw new GenericException();
		}
		
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
		// 0 byte
		int end = responseData[index++];

		long qType = getUInt16(index++);
		index++;	
		long qClass = getUInt16(index++);
		index++;
		
		// Answer starts at data[index]	
	    // Extract list of answers, name server, and additional information response 
	    // records
		parseAnswers();
		parseNameServerRecords();
		parseAdditionalRecords();
	}


    // You will probably want a methods to extract a compressed FQDN, IP address
    // cname, authoritative DNS servers and other values like the query ID etc.
	
	
	// creates an array of Resource objects representing each Additional Record resource
	// increments responseData index to beginning of next section.
	private void parseAdditionalRecords() {
		if (additionalRecordCount > 0) {
			additionalRecords = new Resource[(int) additionalRecordCount];
			for (int i = 0; i < additionalRecordCount; i++) {
				additionalRecords[i] = parseResource();
			}
		}	
	}
	
	// creates an array of Resource objects representing each Nameserver resource
	// increments responseData index to beginning of next section.
	private void parseNameServerRecords() {
		if (nameServerCount > 0) {
			nameServers = new Resource[(int) nameServerCount];
			for (int i = 0; i < nameServerCount; i++) { 
				nameServers[i] = parseResource();	
			}
		}
	}

	// creates an array of Resource objects representing each Answer resource
	// increments responseData index to beginning of next section.
	private void parseAnswers() {
		if (answerCount > 0) {
			answers = new Resource[(int) answerCount];
			for (int i = 0; i < answerCount; i++) { 
				answers[i] = parseResource();			
			}
		}
	}

	// returns a Resource object representing a resource record
	// increments responseData index to beginning of next resource
	private Resource parseResource() {
		Resource resource;
		
		String resourceName = parseWord();
		
		long resourceType = getUInt16(index);
		index += 2;
		long resourceClass = getUInt16(index);
		index += 2;
		long resourceTTL = getUInt32(index);
		index += 4;
		long resourceDataLength = getUInt16(index);
		index += 2;

		String resourceData = getResourceData(resourceType);
		
		resource = new Resource(resourceName, resourceType, resourceClass, resourceTTL, resourceDataLength, resourceData);
		return resource;
	}

	private String getResourceData(long resourceType) {
		String resourceData = "";
		if (resourceType == 1) {  // type A : host ip address.
			resourceData = parseIpAddress();
		} else if (resourceType == 28) { // type AAAA ipv6 address
			resourceData = parseIpv6Address();
		} else if (resourceType == 2 || resourceType == 5){
			resourceData = parseWord();
		} else {
			resourceData = "----";
		}
		return resourceData;
	}


	// returns ipv6 address and increments the responseData index.
	private String parseIpv6Address() {
		String address = "";
		for (int i = 0; i < 8; i++) {
			String octet = "";
			for (int j = 0; j < 2; j++) {
				String nibble = "";
				long x = byteAsULong(responseData[index++]);
				nibble = Long.toHexString(x & 0xf);
				x = x >> 4;
				nibble = Long.toHexString(x & 0xf) + nibble;
				octet += nibble;
			}
			octet += ":";
			address += octet;
		}	
	address = address.substring(0, address.length() - 1);
	return address;
	}

	// returns ip address and increments responseData index
	private String parseIpAddress() {
		String address = "";
		for (int i = 0; i < 4; i++) {
			long x = byteAsULong(responseData[index++]);
			address += x + ".";
		}
		address = address.substring(0, address.length() - 1);
		return address;
	}

	// returns a string from responseData terminated by 0
	// increment responseData index to beginning of next section.
	private String parseWord() {
		String word = "";
		int size = responseData[index];
		
		if (size != 0) {
			
			if (size < 0) {
				long pointerValue = getUInt16(index); 
				index += 2;              				// increment past second pointer byte.
				long offset = (pointerValue & 0x000000003fffL);  
				word = parsePointerWord(offset);
				
			} else {
				index++; 								// increment past size byte
				String label = parseLabel(index, size);
				word += label;
				index += size;							// increment past size of label
				word += parseWord();
			}
		} else {
			index++;									// increment past size byte
			
		}
		
		// remove period from end of string
		if (!word.equals("")) {
			if (word.substring(word.length() - 1, word.length()).equals(".")) {
				word = word.substring(0, word.length() - 1);
			}			
		}
		return word;
	}

	// returns string terminated by 0
	// does not mutate responseData index
	private String parsePointerWord(long idx) {
		String word = "";
		int size = responseData[(int) idx];
		if (size != 0) {
			if (size < 0) {
				long pointerValue = getUInt16((int) idx);
				long offset = (pointerValue & 0x3fff);  
				word = parsePointerWord((int) offset);
				
			} else {
				idx++;
				String label = parseLabel((int) idx, size);
				word += label;
				word += parsePointerWord(idx + size);
			}
		}
		return word;
	}

	// returns label Ex: www.  dude.
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
	
	
	public boolean isAuthoritative() {
		return authoritative;
	}

	public ArrayList<String> getTrace() {
		ArrayList<String> list = new ArrayList<String>();
		list.add("Response ID: " + queryID + " Authoritative " + authoritative);
		
		list.add("  Answers (" + answerCount + ")");
		if (answerCount > 0) {
			list.addAll(getTraceSection(answers));
		}
		
		list.add("  Nameservers (" + nameServerCount + ")");
		if (nameServerCount > 0) {
			list.addAll(getTraceSection(nameServers));
		}
		
		list.add("  Additional Information (" + additionalRecordCount + ")");
		if (additionalRecordCount > 0) {
			list.addAll(getTraceSection(additionalRecords));
		}
		return list;
	}

	private ArrayList<String> getTraceSection(Resource[] resourceArray) {
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < resourceArray.length; i++) {
			Resource resource = resourceArray[i];
			list.add(resource.getString());
		}
		return list;
	}

	
	public String getNextServer() {
		return additionalRecords[0].getData();
	}
	
	public boolean isAnswerCNAME() {
		boolean isCNAME = false;
		if ((answerCount > 0) && authoritative) {
			isCNAME = (answers[0].getType() == 5); // CNAME = 5
		}
		return isCNAME;
	}
	
	public String getFirstNameServerName() {
		return nameServers[0].getData();
	}
	
	public String getAnswersFirstResourceName() {
		return answers[0].getName();
	}
	
	public String getAnswersFirstResourceData() {
		return answers[0].getData();
	}
	
	public int getAnswersFirstResourceType() {
		return (int) answers[0].getType();
	}
	

	public int getAnswersFirstResourceTTL() {
		return (int) answers[0].getTTL();
	}
	
	public boolean isAdditionalInformationEmpty() {
		return additionalRecordCount == 0;
	}
	
	// returns IP for NameServer or null if invalid;
	public String getValidNameServerIP() {
		String ip = null;		
		if (additionalRecordCount > 0) {
			
			for (int i = 0; i < nameServerCount; i++) {
				String nameServer = nameServers[i].getData();
				
				for (int j = 0; j < additionalRecordCount; j++) {
					Resource additionalRecord = additionalRecords[j]; 
					
					if (nameServer.equals(additionalRecord.getName())) {
						ip = additionalRecord.getData();
						return ip;
					}
				}
			}
		}
		return ip;
	}
}


