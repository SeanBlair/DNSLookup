/**
 * @author Gurjot and Sean
 */
public class Resource {

	private String name, data;
	private long resourceType, resourceClass, TTL, dataLength;
	
	/**
	 * Represents a DNS resource entry
	 * 
	 * @param rName   	Resource Name
	 * @param rType	  	Resource Type
	 * @param rClass  	Resource Class
	 * @param ttl	  	Resource Time to live
	 * @param dLength 	Resource data length
	 * @param d       	Resource data
	 */
	public Resource(String rName, long rType, long rClass, long ttl, long dLength, String d) {
		this.name = rName;
		this.resourceType = rType;
		this.resourceClass = rClass;
		this.TTL = ttl;
		this.dataLength = dLength;
		this.data = d;
	}
	
	public String getName() {
		return name;
	}
	
	public long getType() {
		return resourceType;
	}

	public long getRClass() {
		return resourceClass;
	}

	public long getTTL() {
		return TTL;
	}
	
	public long getDataLength() {
		return dataLength;
	}
	
	public String getData() {
		return data;
	}

	/**
	 * @return A String representing this Resource
	 */
	public String getString() {
		String type = translateType((int) resourceType);
		return String.format("       %-30s %-10d %-4s %s", name, TTL, type, data);
	}

	/**
	 * @param resourceType  Integer representing a Type of Resource.
	 * @return  The text representation of resourceType.
	 */
	private String translateType(int resourceType) {
		switch (resourceType) {
			case 1 : return "A";
			case 2 : return "NS";
			case 5 : return "CN";
			case 28 : return "AAAA";
			
			default : return String.valueOf(resourceType);
		}
	}
}