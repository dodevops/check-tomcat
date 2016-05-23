# check-tomcat

This is a small JAVA program, that connects to a Tomcat servlet container 
using JMX and waits, until all resources are loaded.

## Usage

    java -jar check-tomcat.jar -j <jmx-URL>

Use a valid jmx-URL like this:

    service:jmx:rmi:///jndi/rmi://<jmx host name>:<jmx port>/jmxrmi

Replace <jmx host name> and <jmx port> with the hostname and port of your jmx
 server.
 
## Timeout

The default timeout for waiting for tomcat to load the resources is ten 
minutes. This can be configured using the parameter "-t". 

If the timeout is reached, the program exists with an error message and 
return code 2.
  
## Details

Use --help to get more information about available parameters.