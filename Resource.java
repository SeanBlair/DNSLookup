public class Resource {

	private String name;
	private long resourceType;
	private long resourceClass;
	private long TTL;
	private long dataLength;
	private String data;
	
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

	public void print() {
		System.out.format("       %-30s %-10d %-4s %s\n", name, TTL, resourceType, data);
	}

	public String getString() {
		String type = translateType((int) resourceType);
		return String.format("       %-30s %-10d %-4s %s", name, TTL, type, data);
	}

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