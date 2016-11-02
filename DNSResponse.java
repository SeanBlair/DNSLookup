
import java.net.InetAddress;



// Lots of the action associated with handling a DNS query is processing 
// the response. Although not required you might find the following skeleton of
// a DNSreponse helpful. The class below has bunch of instance data that typically needs to be 
// parsed from the response. If you decide to use this class keep in mind that it is just a 
// suggestion and feel free to add or delete methods to better suit your implementation as 
// well as instance variables.



public class DNSResponse {
    private int queryID;                  // this is for the response it must match the one in the request 
    private int answerCount = 0;          // number of answers  
    private boolean decoded = false;      // Was this response successfully decoded
    private int nsCount = 0;              // number of nscount response records
    private int additionalCount = 0;      // number of additional (alternate) response records
    private boolean authoritative = false;// Is this an authoritative record

    // Note you will almost certainly need some additional instance variables.

    // When in trace mode you probably want to dump out all the relevant information in a response

	void dumpResponse() {
		


	}

    // The constructor: you may want to add additional parameters, but the two shown are 
    // probably the minimum that you need.

	public DNSResponse (byte[] data, int len) {
	    
	    // The following are probably some of the things 
	    // you will need to do.
	    // Extract the query ID
		
		// int consisting of the first two bytes of response
		// has to be equal to query id.. (check??)
		queryID = (data[0] << 8) | data[1];
		
	    // Make sure the message is a query response and determine
	    // if it is an authoritative response or note
		
		
		boolean isResponse = data[2] < 0;

		if (isResponse) {
			authoritative = ((data[2] >> 2) & 0x1) == 1;

		    // determine answer count
			answerCount = (data[4] << 8) | data[5];
	
		    // determine NS Count
			nsCount = (data[6] << 8) | data[7];
	

		    // determine additional record count
			additionalCount = (data[8] << 8) | data[9];
			
		}

		



	

	    // Extract list of answers, name server, and additional information response 
	    // records
	}


    // You will probably want a methods to extract a compressed FQDN, IP address
    // cname, authoritative DNS servers and other values like the query ID etc.
	
	
	public int getQueryID() {
		return queryID;
	}


    // You will also want methods to extract the response records and record
    // the important values they are returning. Note that an IPV6 reponse record
    // is of type 28. It probably wouldn't hurt to have a response record class to hold
    // these records. 
}


