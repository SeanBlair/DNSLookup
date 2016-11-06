 
import java.net.DatagramSocket; 
import java.net.InetAddress; 
import java.net.SocketException;
import java.util.Random; 
 
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
   
  /** 
   * @param args 
   */ 
  public static void main(String[] args) throws Exception { 
    int argCount = args.length; 
     
    if (argCount < 2 || argCount > 3) { 
      usage(); 
      return; 
    } 
 
    String hostServer = args[0]; 
    String fqdn = args[1];
     
    if (argCount == 3 && args[2].equals("-t")) 
        tracingOn = true; 
     
    // Send the query
    DNSQuery queryHandler = new DNSQuery(hostServer, fqdn, tracingOn);
    try {
    	queryHandler.query(hostServer, fqdn);
    } catch (SocketException e) {
    	// TODO
    	System.out.println("Socket failed to create.");
    }
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