# SE-2_Group-E

### Building and running application
To build all modules of the application run the following command:
```$xslt
mvn clean package -DskipTests
```

Now, we can run separate modules with these (on Windows):
```$xslt
java -jar server\target\server-0.0.1-SNAPSHOT.jar
``` 
```$xslt
java -jar game-master\target\game-master-0.0.1-SNAPSHOT.jar
``` 
```$xslt
java -jar player\target\player-0.0.1-SNAPSHOT.jar
``` 
