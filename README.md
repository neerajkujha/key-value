# key-value
key value store



#Pre-requisite
1. Maven
2. Java 8

#Steps to run the application

1. Run "mvn clean install -s settings.xml" in kv and sd folder to build the project
2. Run sd (service discovery) application "java -jar <path>/sd/target/sd-0.0.1-SNAPSHOT.jar"
3. Keep <path>/kv/target/kv-0.0.1-SNAPSHOT.jar in two different folders so that both can run seperately
4. Run kv application in both folder by command "java -jar kv-0.0.1-SNAPSHOT.jar java -jar kv-0.0.1-SNAPSHOT.jar --server.port=<port>"
5. Port should be different for both processes


