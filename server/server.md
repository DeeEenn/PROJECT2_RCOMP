## How to start a server? 
---- 

### Console commands in order

- 1. cd/server
- 2. mvn clean package (rather compile between each starts if u change code)
- 3. java -jar target/file-server-1.0-SNAPSHOT-jar-with-dependencies.jar

### Result

- Server should be running now accordingly with a configuration set in config.properties such as
- server port
- max slots
- file size