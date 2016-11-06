all: 
	javac DNSlookup.java
	javac DNSResponse.java
	javac Resource.java
	javac DNSQuery.java
	javac NonExistentNameException.java
	javac GenericException.java
	
	jar cvfe DNSlookup.jar DNSlookup *.class

run: DNSlookup.jar
	java -jar DNSlookup.jar   199.7.83.42 www.cs.ubc.ca   -t
clean:
	rm -f *.class
	rm -f DNSlookup.jar
