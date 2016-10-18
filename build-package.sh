mvn clean
launch4j src/main/assembly/emulator.xml
launch4j src/main/assembly/guest.xml
mvn package -DskipTests

