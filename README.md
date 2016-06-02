# check-tomcat

This is a small JAVA program, that connects to a Tomcat servlet container 
using JMX and waits, until all contexts are loaded.

This is done by gathering all beans with type NamingResources and 
resourcetype context, getting their host and path and checking the attribute 
"stateName" of the beans with j2eeType WebModule, J2EEApplication none,
J2EEServer none and name=//<host>/<path>.

Once all contexts have the stateName "STARTED", the program exits.

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

## Connection problems

When called directly after a server is started, the JMX service might not be 
up and running - which is an expected error. In this case, the script will 
return code 3, so that other scripts using it may react to it and restart it.
  
## Details

Use --help to get more information about available parameters.

## Building

To build the app, run

    mvn package

Afterwards, you can find a JAR with all packaged dependency in the 
target/-folder named check-tomcat-<version>-jar-with-dependencies.jar. 
