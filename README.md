DNS Lookup
=====
Program Description
--------
- Given a DNS name server and a domain name, returns the IP address that identifies the given domain and the number of seconds
that it can be cached for.
- In trace mode (-t) returns the full list of responses from the intermediate DNS servers.

Dependencies:
--------
Developed and tested with Java 1.8 on a Linux ubuntu 14.04 LTS machine 

Running instructions:
-------
- Open a terminal and clone the repo: git clone https://github.com/SeanBlair/DNSLookup.git
- Enter the folder: cd DNSLookup
- Use the makefile by typing "make", enter
- Run the program: java -jar DNSlookup.jar [DNS name server] [fully qualified domain name] [optional trace flag (-t)]

  Examples:
  
  java -jar DNSlookup.jar 192.197.97.190 www.cnn.com     
  
  java -jar DNSlookup.jar 192.197.97.190 www.cnn.com -t  
