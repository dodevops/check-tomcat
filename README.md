# check-tomcat

This is a small JAVA program, that connects to a Tomcat servlet container 
using JMX and waits, until all contexts are loaded.

This is done by checking all beans with type NamingResources and
resourcetype context and comparing them to a list of known contexts, that
should start.

Once all contexts have the stateName "STARTED", the program exits.

## Usage

    java -jar check-tomcat.jar -j <jmx-URL> -r <Resources to check>

Use a valid jmx-URL like this:

    service:jmx:rmi:///jndi/rmi://<jmx host name>:<jmx port>/jmxrmi

Replace <jmx host name> and <jmx port> with the hostname and port of your jmx
 server.

Specify -r for each resource you want to check (i.e. myserver:some/context)
 
## Timeout

The default timeout for waiting for tomcat to load the resources is ten 
minutes. This can be configured using the parameter "-t". 

If the timeout is reached, the program exists with an error message and 
return code 2.

## Connection problems

When called directly after a server is started, the JMX service might not be 
up and running - which is an expected error. In this case, the script will 
return code 3, so that other scripts using it may react to it and restart it.

## Dealing with startup problems

To check, why check-tomcat doesn't return, run check-tomcat with "-d" and
it will output all contexts, that have not been started.

Example:

    TOMCAT_HOME=/usr/local/tomcat/conf/Catalina
    RESOURCES=`cd ${TOMCAT_HOME} && find * -type f | sed -re "s/ROOT//gi" | tr / : | tr \# \/ | sed -re "s/\.xml//gi" | paste -s -d "," | sed -re "s/,/ -r /gi"`
    java -jar check-tomcat.jar -j service:jmx:rmi:///jndi/rmi://localhost:9003/jmxrmi -r $RESOURCES -d
  
## Details

Use --help to get more information about available parameters.

## Building

To build the app, run

    mvn package

Afterwards, you can find a JAR with all packaged dependency in the 
target/-folder named check-tomcat-<version>-jar-with-dependencies.jar. 
